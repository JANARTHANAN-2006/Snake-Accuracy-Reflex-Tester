import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

public class SimpleScoreManager {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Difficulty selection
            String[] diffs = {"Easy", "Medium", "Hard"};
            String diff = (String) JOptionPane.showInputDialog(null, "Choose difficulty:", "Difficulty",
                    JOptionPane.PLAIN_MESSAGE, null, diffs, diffs[1]);
            if (diff == null) diff = "Medium";

            // Snake color
            String[] colors = {"Green", "Red", "Blue", "White", "Orange"};
            String chosen = (String) JOptionPane.showInputDialog(null, "Choose your snake color:", "Color",
                    JOptionPane.PLAIN_MESSAGE, null, colors, colors[0]);
            Color snakeColor = switch (chosen == null ? "Green" : chosen) {
                case "Red" -> Color.RED;
                case "Blue" -> Color.BLUE;
                case "White" -> Color.WHITE;
                case "Orange" -> Color.ORANGE;
                default -> Color.GREEN;
            };

            JFrame frame = new JFrame("Cute Snake — Accuracy Tester (SimpleScoreManager)");
            GamePanel game = new GamePanel(diff, snakeColor);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.add(game);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            String instr = """
                    Arrow keys to move.
                    Collect colored points (showing value).
                    Power-ups appear occasionally: Slow Time, Shield, Multiplier.
                    Each score increases obstacles and repositions points.
                    Time limit: 2 minutes. P to pause, R to restart.
                    Your score is saved to scores.csv on death.
                    """;
            JOptionPane.showMessageDialog(frame, instr, "Instructions", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    // ---------------- Score Manager ----------------
    static class ScoreManager {
        private static final Path SCORE_FILE = Paths.get("scores.csv");

        public static void save(String name, int score) {
            try {
                Files.createDirectories(SCORE_FILE.getParent() == null ? Paths.get(".") : SCORE_FILE.getParent());
                try (BufferedWriter bw = Files.newBufferedWriter(SCORE_FILE, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                    bw.write(System.currentTimeMillis() + "," + escape(name) + "," + score);
                    bw.newLine();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        private static String escape(String s) {
            return s.replaceAll(",", " ");
        }

        public static List<String[]> loadAll() {
            List<String[]> list = new ArrayList<>();
            if (Files.exists(SCORE_FILE)) {
                try {
                    for (String line : Files.readAllLines(SCORE_FILE)) {
                        String[] parts = line.split(",");
                        if (parts.length >= 3) {
                            String ts = parts[0];
                            String name = parts[1];
                            String sc = parts[2];
                            list.add(new String[]{name, sc, ts});
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return list;
        }

        public static void showScoreboard(Component parent) {
            List<String[]> all = loadAll();
            all.sort((a, b) -> Integer.compare(Integer.parseInt(b[1]), Integer.parseInt(a[1])));
            String[] cols = {"Rank", "Name", "Score"};
            DefaultTableModel model = new DefaultTableModel(cols, 0);
            for (int i = 0; i < Math.min(50, all.size()); i++) {
                String[] r = all.get(i);
                model.addRow(new Object[]{i + 1, r[0], r[1]});
            }
            JTable table = new JTable(model);
            JScrollPane sp = new JScrollPane(table);
            sp.setPreferredSize(new Dimension(400, 300));
            JOptionPane.showMessageDialog(parent, sp, "Scoreboard", JOptionPane.PLAIN_MESSAGE);
        }
    }

    // ---------------- Game Panel ----------------
    static class GamePanel extends JPanel implements ActionListener, KeyListener {
        static final int WIDTH = 900;
        static final int HEIGHT = 600;
        static final int UNIT = 20;
        static final int FPS = 60;

        LinkedList<Point> snake = new LinkedList<>();
        Direction dir = Direction.RIGHT;
        boolean running = false;
        javax.swing.Timer timer; // fixed ambiguous Timer
        int score = 0;
        long startTime;
        final long timeLimit = 2 * 60 * 1000;
        Color snakeColor;
        Random rnd = new Random();

        List<Rectangle> obstacles = new ArrayList<>();
        List<Collectable> collects = new ArrayList<>();
        PowerUp activePower = null;
        List<PowerUp> powerUps = new ArrayList<>();

        int initialObs = 4;
        int maxObs = 80;
        int obsIncrease = 1;

        double speedMultiplier = 1.0;
        int extraObstacles = 0;

        public GamePanel(String difficulty, Color snakeColor) {
            this.snakeColor = snakeColor;
            setPreferredSize(new Dimension(WIDTH, HEIGHT));
            setBackground(new Color(12, 12, 12));
            setFocusable(true);
            addKeyListener(this);

            switch (difficulty) {
                case "Easy" -> {
                    speedMultiplier = 0.9;
                    extraObstacles = -2;
                }
                case "Hard" -> {
                    speedMultiplier = 1.15;
                    extraObstacles = 4;
                }
                default -> {
                    speedMultiplier = 1.0;
                    extraObstacles = 0;
                }
            }

            initGame();
        }

        private void initGame() {
            snake.clear();
            snake.add(new Point(UNIT * 5, UNIT * 5));
            snake.add(new Point(UNIT * 4, UNIT * 5));
            snake.add(new Point(UNIT * 3, UNIT * 5));
            dir = Direction.RIGHT;
            obstacles.clear();
            collects.clear();
            powerUps.clear();

            for (int i = 0; i < initialObs + extraObstacles; i++) placeObstacle();

            collects.add(new Collectable(10, Color.YELLOW));
            collects.add(new Collectable(25, Color.CYAN));
            collects.add(new Collectable(50, Color.MAGENTA));
            for (Collectable c : collects) placeCollectable(c);

            score = 0;
            running = true;
            startTime = System.currentTimeMillis();

            int delay = (int) Math.max(6, 1000.0 / FPS / speedMultiplier);
            timer = new javax.swing.Timer(delay, this); // fixed ambiguous Timer
            timer.start();
        }

        private void placeObstacle() {
            Rectangle r;
            int tries = 0;
            do {
                int x = rnd.nextInt(WIDTH / UNIT) * UNIT;
                int y = rnd.nextInt(HEIGHT / UNIT) * UNIT;
                r = new Rectangle(x, y, UNIT, UNIT);
                tries++;
                if (tries > 300) break;
            } while (collidesWithAnything(r));
            obstacles.add(r);
        }

        private boolean collidesWithAnything(Rectangle r) {
            for (Point p : snake) if (r.contains(p)) return true;
            for (Rectangle o : obstacles) if (o.intersects(r)) return true;
            for (Collectable c : collects) if (c.pos != null && r.contains(c.pos)) return true;
            return false;
        }

        private void placeCollectable(Collectable c) {
            Point p;
            int tries = 0;
            do {
                int x = rnd.nextInt(WIDTH / UNIT) * UNIT;
                int y = rnd.nextInt(HEIGHT / UNIT) * UNIT;
                p = new Point(x, y);
                tries++;
                if (tries > 300) break;
            } while (isOccupied(p));
            c.pos = p;
        }

        private boolean isOccupied(Point p) {
            for (Point s : snake) if (s.equals(p)) return true;
            for (Rectangle r : obstacles) if (r.contains(p)) return true;
            for (Collectable c : collects) if (c.pos != null && c.pos.equals(p)) return true;
            return false;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            draw((Graphics2D) g);
        }

        private void draw(Graphics2D g) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setColor(new Color(30, 30, 30));
            for (int x = 0; x < WIDTH; x += UNIT) g.drawLine(x, 0, x, HEIGHT);
            for (int y = 0; y < HEIGHT; y += UNIT) g.drawLine(0, y, WIDTH, y);

            g.setColor(snakeColor);
            for (int i = 0; i < snake.size(); i++) {
                Point p = snake.get(i);
                if (i == 0) g.fill(new RoundRectangle2D.Float(p.x, p.y, UNIT, UNIT, 8, 8));
                else g.fillRect(p.x, p.y, UNIT, UNIT);
            }

            for (Collectable c : collects) {
                if (c.pos == null) continue;
                g.setColor(c.color);
                int pad = 4;
                g.fillOval(c.pos.x + pad / 2, c.pos.y + pad / 2, UNIT - pad, UNIT - pad);
                g.setColor(Color.BLACK);
                g.setFont(new Font("SansSerif", Font.BOLD, 11));
                FontMetrics fm = g.getFontMetrics();
                String s = String.valueOf(c.value);
                int tx = c.pos.x + (UNIT - fm.stringWidth(s)) / 2;
                int ty = c.pos.y + ((UNIT - fm.getHeight()) / 2) + fm.getAscent();
                g.drawString(s, tx, ty);
            }

            g.setColor(new Color(80, 80, 80));
            for (Rectangle r : obstacles) g.fillRect(r.x, r.y, r.width, r.height);

            for (PowerUp p : powerUps) {
                if (p.pos == null) continue;
                g.setColor(p.color);
                g.fillOval(p.pos.x + 2, p.pos.y + 2, UNIT - 4, UNIT - 4);
            }

            g.setColor(Color.WHITE);
            g.setFont(new Font("Consolas", Font.BOLD, 16));
            g.drawString("Score: " + score, 10, 20);
            long rem = Math.max(0, timeLimit - (System.currentTimeMillis() - startTime));
            g.drawString(String.format("Time: %02d:%02d", rem / 60000, (rem / 1000) % 60), WIDTH - 160, 20);

            if (!running) {
                g.setColor(new Color(0, 0, 0, 170));
                g.fillRect(0, HEIGHT / 2 - 80, WIDTH, 160);
                g.setColor(Color.WHITE);
                g.setFont(new Font("SansSerif", Font.BOLD, 36));
                drawCentered(g, "Game Over", HEIGHT / 2 - 20);
                g.setFont(new Font("SansSerif", Font.PLAIN, 20));
                drawCentered(g, "Final Score: " + score, HEIGHT / 2 + 20);
                drawCentered(g, "Press ENTER to play again", HEIGHT / 2 + 50);
            }
        }

        private void drawCentered(Graphics2D g, String text, int y) {
            FontMetrics fm = g.getFontMetrics();
            int x = (WIDTH - fm.stringWidth(text)) / 2;
            g.drawString(text, x, y);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!running) return;
            long now = System.currentTimeMillis();
            if (now - startTime >= timeLimit) {
                endGame();
                return;
            }

            if (rnd.nextDouble() < 0.005 && powerUps.size() < 2) spawnPowerUp();

            move();
            checkCollects();
            checkPowerUps();
            checkCollisions();
            repaint();
        }

        private void spawnPowerUp() {
            PowerUp.Type[] types = PowerUp.Type.values();
            PowerUp.Type t = types[rnd.nextInt(types.length)];
            PowerUp p = new PowerUp(t);
            int tries = 0;
            Point pos;
            do {
                pos = new Point(rnd.nextInt(WIDTH / UNIT) * UNIT, rnd.nextInt(HEIGHT / UNIT) * UNIT);
                tries++;
                if (tries > 200) break;
            } while (isOccupied(pos));
            p.pos = pos;
            powerUps.add(p);
        }

        private void move() {
            Point head = snake.getFirst();
            Point nh = new Point(head.x, head.y);
            switch (dir) {
                case UP -> nh.y -= UNIT;
                case DOWN -> nh.y += UNIT;
                case LEFT -> nh.x -= UNIT;
                case RIGHT -> nh.x += UNIT;
            }
            if (nh.x < 0) nh.x = WIDTH - UNIT;
            if (nh.x >= WIDTH) nh.x = 0;
            if (nh.y < 0) nh.y = HEIGHT - UNIT;
            if (nh.y >= HEIGHT) nh.y = 0;

            snake.addFirst(nh);
            snake.removeLast();
        }

        private void checkCollects() {
            Point head = snake.getFirst();
            for (Collectable c : collects) {
                if (c.pos != null && c.pos.equals(head)) {
                    int gained = c.value;
                    if (activePower != null && activePower.type == PowerUp.Type.MULTIPLIER && activePower.isActive())
                        gained *= 2;
                    score += gained;
                    Toolkit.getDefaultToolkit().beep();
                    snake.addLast(new Point(snake.getLast()));
                    for (int i = 0; i < obsIncrease; i++)
                        if (obstacles.size() < maxObs) placeObstacle();
                    for (Collectable cc : collects) placeCollectable(cc);
                    return;
                }
            }
        }

        private void checkPowerUps() {
            Point head = snake.getFirst();
            Iterator<PowerUp> it = powerUps.iterator();
            while (it.hasNext()) {
                PowerUp p = it.next();
                if (p.pos != null && p.pos.equals(head)) {
                    activatePowerUp(p);
                    it.remove();
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
            }
            if (activePower != null && !activePower.isActive()) activePower = null;
        }

        private void activatePowerUp(PowerUp p) {
            activePower = p;
            activePower.activate();
            if (p.type == PowerUp.Type.SLOW) {
                timer.setDelay((int) (timer.getDelay() * 1.6));
                new javax.swing.Timer((int) p.duration, ev -> timer.setDelay((int) (1000.0 / FPS / speedMultiplier))).start();
            }
        }

        private void checkCollisions() {
            Point head = snake.getFirst();
            for (int i = 1; i < snake.size(); i++)
                if (head.equals(snake.get(i))) {
                    if (activePower != null && activePower.type == PowerUp.Type.SHIELD && activePower.isActive()) {
                        Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                    endGame();
                    return;
                }
            for (Rectangle r : obstacles)
                if (r.contains(head)) {
                    if (activePower != null && activePower.type == PowerUp.Type.SHIELD && activePower.isActive()) {
                        Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                    endGame();
                    return;
                }
        }

        private void endGame() {
            running = false;
            timer.stop();
            SwingUtilities.invokeLater(() -> {
                String name = JOptionPane.showInputDialog(this, "Enter name for scoreboard:", "Player");
                if (name == null || name.trim().isEmpty()) name = "Player";
                ScoreManager.save(name.trim(), score);
                ScoreManager.showScoreboard(this);
            });
        }

        @Override public void keyTyped(KeyEvent e) {}
        @Override public void keyReleased(KeyEvent e) {}

        @Override
        public void keyPressed(KeyEvent e) {
            int k = e.getKeyCode();
            if (!running) {
                if (k == KeyEvent.VK_ENTER) initGame();
                return;
            }
            switch (k) {
                case KeyEvent.VK_LEFT -> { if (dir != Direction.RIGHT) dir = Direction.LEFT; }
                case KeyEvent.VK_RIGHT -> { if (dir != Direction.LEFT) dir = Direction.RIGHT; }
                case KeyEvent.VK_UP -> { if (dir != Direction.DOWN) dir = Direction.UP; }
                case KeyEvent.VK_DOWN -> { if (dir != Direction.UP) dir = Direction.DOWN; }
                case KeyEvent.VK_P -> { if (timer.isRunning()) timer.stop(); else timer.start(); }
                case KeyEvent.VK_R -> initGame();
                case KeyEvent.VK_S -> ScoreManager.showScoreboard(this);
            }
        }

        enum Direction {UP, DOWN, LEFT, RIGHT}

        static class Collectable {
            int value;
            Color color;
            Point pos;

            Collectable(int v, Color c) {
                value = v;
                color = c;
            }
        }

        static class PowerUp {
            enum Type {SLOW, SHIELD, MULTIPLIER}

            Type type;
            Point pos;
            Color color;
            int duration;
            long activatedAt = 0;

            PowerUp(Type t) {
                type = t;
                switch (t) {
                    case SLOW -> color = Color.BLUE;
                    case SHIELD -> color = Color.GREEN;
                    case MULTIPLIER -> color = Color.ORANGE;
                }
                duration = 10000;
            }

            void activate() {
                activatedAt = System.currentTimeMillis();
            }

            boolean isActive() {
                return System.currentTimeMillis() - activatedAt < duration;
            }
        }
    }
}
