import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class SnakeAccuracyTester extends JPanel implements ActionListener, KeyListener {
    private final int WIDTH = 900;
    private final int HEIGHT = 600;
    private final int UNIT_SIZE = 25;
    private final int BASE_DELAY = 80;
    private final int GAME_DURATION = 120000; // 2 min

    private int snakeLength = 5;
    private int score = 0;
    private int obstaclesCount = 4;
    private long startTime;
    private boolean running = false;

    private char direction = 'R';
    private javax.swing.Timer timer;
    private Random random;
    private Point food;
    private Color foodColor;
    private int foodValue;

    private List<Rectangle> obstacles;
    private Color snakeColor;

    private LinkedList<Point> snake = new LinkedList<>();
    private int delay;

    // Accuracy tracking
    private int totalMoves = 0;
    private int foodHits = 0;

    public SnakeAccuracyTester() {
        random = new Random();
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        snakeColor = chooseSnakeColor();
        startGame();
    }

    private Color chooseSnakeColor() {
        String[] colors = {"Blue", "Green", "Red", "Yellow", "Cyan", "Pink"};
        String choice = (String) JOptionPane.showInputDialog(null,
                "Choose your snake color:",
                "Snake Color",
                JOptionPane.QUESTION_MESSAGE,
                null, colors, colors[0]);

        if (choice == null) return Color.BLUE;
        return switch (choice) {
            case "Green" -> Color.GREEN;
            case "Red" -> Color.RED;
            case "Yellow" -> Color.YELLOW;
            case "Cyan" -> Color.CYAN;
            case "Pink" -> Color.PINK;
            default -> Color.BLUE;
        };
    }

    private void startGame() {
        snake.clear();
        for (int i = 0; i < snakeLength; i++)
            snake.add(new Point(UNIT_SIZE * (snakeLength - i), UNIT_SIZE * 5));

        direction = 'R';
        obstaclesCount = 4;
        spawnObstacles();
        spawnFood();
        running = true;
        startTime = System.currentTimeMillis();
        delay = BASE_DELAY;
        timer = new javax.swing.Timer(delay, this);
        timer.start();

        totalMoves = 0;
        foodHits = 0;
    }

    private void spawnFood() {
        food = new Point(random.nextInt(WIDTH / UNIT_SIZE) * UNIT_SIZE,
                random.nextInt(HEIGHT / UNIT_SIZE) * UNIT_SIZE);
        foodColor = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        foodValue = 10 + random.nextInt(41); // 10 to 50
    }

    private void spawnObstacles() {
        obstacles = new ArrayList<>();
        for (int i = 0; i < obstaclesCount; i++) {
            obstacles.add(new Rectangle(random.nextInt(WIDTH / UNIT_SIZE) * UNIT_SIZE,
                    random.nextInt(HEIGHT / UNIT_SIZE) * UNIT_SIZE, UNIT_SIZE, UNIT_SIZE));
        }
    }

    private void move() {
        Point head = snake.getFirst();
        Point newHead = new Point(head);
        switch (direction) {
            case 'U' -> newHead.y -= UNIT_SIZE;
            case 'D' -> newHead.y += UNIT_SIZE;
            case 'L' -> newHead.x -= UNIT_SIZE;
            case 'R' -> newHead.x += UNIT_SIZE;
        }

        // Wrap around edges
        if (newHead.x < 0) newHead.x = WIDTH - UNIT_SIZE;
        if (newHead.x >= WIDTH) newHead.x = 0;
        if (newHead.y < 0) newHead.y = HEIGHT - UNIT_SIZE;
        if (newHead.y >= HEIGHT) newHead.y = 0;

        snake.addFirst(newHead);
        if (!checkFoodCollision()) snake.removeLast();
        totalMoves++;
    }

    private boolean checkFoodCollision() {
        Point head = snake.getFirst();
        if (head.equals(food)) {
            score += foodValue;
            snakeLength++;
            foodHits++;
            // Increase difficulty
            delay = Math.max(30, delay - 3);
            timer.setDelay(delay);
            obstaclesCount++;
            spawnFood();
            spawnObstacles();
            return true;
        }
        return false;
    }

    private void checkCollision() {
        Point head = snake.getFirst();
        // Collide with self
        for (int i = 1; i < snake.size(); i++)
            if (head.equals(snake.get(i))) running = false;

        // Collide with obstacles
        for (Rectangle obs : obstacles)
            if (obs.contains(head)) running = false;

        if (!running) timer.stop();
    }

    private void drawTimer(Graphics g) {
        long elapsed = System.currentTimeMillis() - startTime;
        long remaining = GAME_DURATION - elapsed;
        if (remaining <= 0) {
            running = false;
            timer.stop();
            showGameOverUI();
            return;
        }

        int seconds = (int) (remaining / 1000);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.BOLD, 22));
        g.drawString("Time: " + seconds + "s", WIDTH - 180, 30);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (running) {
            // Draw food
            g2.setColor(foodColor);
            g2.fillOval(food.x + 2, food.y + 2, UNIT_SIZE - 4, UNIT_SIZE - 4);

            // Draw snake
            g2.setColor(snakeColor);
            for (int i = 0; i < snake.size(); i++) {
                Point p = snake.get(i);
                if (i == 0) g2.fillRoundRect(p.x, p.y, UNIT_SIZE, UNIT_SIZE, 8, 8);
                else g2.fillRect(p.x, p.y, UNIT_SIZE, UNIT_SIZE);
            }

            // Draw obstacles
            g2.setColor(Color.DARK_GRAY);
            for (Rectangle obs : obstacles)
                g2.fillRoundRect(obs.x, obs.y, obs.width, obs.height, 6, 6);

            // Score
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Consolas", Font.BOLD, 22));
            g2.drawString("Score: " + score, 10, 30);

            // Timer
            drawTimer(g2);
        } else {
            showGameOverUI();
        }
    }

    private void showGameOverUI() {
        SwingUtilities.invokeLater(() -> {
            // Accuracy calculation
            double accuracy = totalMoves == 0 ? 0 : (foodHits * 100.0 / totalMoves);

            String[] options = {"Try Again", "Exit"};
            JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
            panel.setBackground(Color.BLACK);

            JLabel lblGameOver = new JLabel("GAME OVER");
            lblGameOver.setFont(new Font("Arial Black", Font.BOLD, 36));
            lblGameOver.setForeground(Color.RED);
            lblGameOver.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(lblGameOver);

            JLabel lblScore = new JLabel("Score: " + score);
            lblScore.setFont(new Font("Consolas", Font.BOLD, 26));
            lblScore.setForeground(Color.WHITE);
            lblScore.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(lblScore);

            // Accuracy table
            String[] col = {"Metric", "Value"};
            Object[][] data = {
                    {"Total Moves", totalMoves},
                    {"Food Collected", foodHits},
                    {"Accuracy (%)", String.format("%.2f", accuracy)}
            };
            JTable table = new JTable(data, col);
            table.setEnabled(false);
            table.setRowHeight(25);
            table.setForeground(Color.WHITE);
            table.setBackground(Color.DARK_GRAY);
            table.setFont(new Font("Consolas", Font.PLAIN, 18));
            panel.add(table);

            int choice = JOptionPane.showOptionDialog(this, panel, "Results",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, options, options[0]);

            if (choice == 0) startGame();
            else System.exit(0);
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkCollision();
        }
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT -> { if (direction != 'R') direction = 'L'; }
            case KeyEvent.VK_RIGHT -> { if (direction != 'L') direction = 'R'; }
            case KeyEvent.VK_UP -> { if (direction != 'D') direction = 'U'; }
            case KeyEvent.VK_DOWN -> { if (direction != 'U') direction = 'D'; }
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Snake Accuracy & Reflex Tester");
        SnakeAccuracyTester game = new SnakeAccuracyTester();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
