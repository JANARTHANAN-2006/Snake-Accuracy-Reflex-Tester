# 🐍 Snake Accuracy & Reflex Tester

Welcome to **Snake Accuracy & Reflex Tester**, the ultimate game to measure your reflexes, precision, and strategic planning while having tons of fun! Unlike traditional snake games, this project isn’t just about growing the snake — it’s about **testing your accuracy, reaction speed, and adaptability** in a visually appealing and dynamic environment. 🎯✨

---

## 🧩 Problem Statement

Many players enjoy snake games, but few have tools to **analyze performance, reflexes, and accuracy** in real-time. Traditional games simply track score, missing out on insightful metrics like efficiency, food collection vs moves, and reaction under pressure.  

The goal of this project is to create a **standalone desktop snake game** that not only entertains but also provides **analytics for accuracy and reflex skills**:

- Track **moves vs food collected** to determine precision.  
- Dynamically **increase difficulty** as players perform better.  
- Offer **instant visual feedback** and a modern, smooth graphical interface.  
- Present a **professional and interactive Game Over screen** with detailed performance metrics.  

This project is **offline, standalone**, and runs locally with all data saved on your computer — no cloud or account needed. 🖥️💡

---

## ⚙️ Tools & Features

| Component | Functionality |
|-----------|---------------|
| **Java Swing GUI** | Smooth, modern, and colorful interface with rounded snake graphics and dynamic food points. |
| **Real-Time Analytics** | Tracks total moves, food collected, and calculates accuracy in real-time. |
| **Dynamic Difficulty** | Snake speed and obstacle count increase with every collected point for challenging gameplay. |
| **Obstacles & Food Points** | Obstacles stay static; only food points change place after collection. |
| **Enhanced Game Over UI** | Shows **score, accuracy table, and Try Again/Exit buttons** in a visually striking layout. |
| **Offline-First Gameplay** | Fully functional offline, with scores optionally saved locally in a file. |
| **Randomized Food & Color** | Each point appears in a random color and value, enhancing engagement and reflex testing. |
| **Responsive Controls** | Arrow key navigation with instant reaction detection. |

---

## 💻 Tech Stack

| Layer | Technology Used | Purpose |
|-------|----------------|---------|
| **Frontend UI** | Java Swing (Graphics2D + Anti-aliasing) | Smooth and visually appealing snake and obstacle rendering |
| **Backend Logic** | Core Java | Game mechanics, collision detection, speed scaling, accuracy calculations |
| **Local Storage** | Plain Text File (Optional) | Stores player name and score for future reference |
| **Visual Effects** | Swing + Color Randomization | Dynamic and attractive food points and snake movement |
| **Styling & UX** | Rounded shapes, modern fonts, and contrasting colors | Creates a “hotter” and more arcade-style feel |

---

## 🎮 Gameplay Highlights

1. **Move the snake** with arrow keys.  
2. **Collect points** — each point has a value and random color.  
3. **Avoid obstacles**, which grow in number as the game progresses.  
4. **Observe your accuracy**: Total moves vs points collected is tracked.  
5. **Dynamic speed**: Snake accelerates after each point, testing reflexes.  
6. **Game Over** presents a professional analytics table with metrics:
   - Total Moves  
   - Food Collected  
   - Accuracy (%)  
7. **Try Again button** allows instant replay.  

---

## 🚀 Future Enhancements

- 🔥 **Particle Effects & Smooth Trails**: Make snake movement more visually stunning.  
- 📊 **Advanced Analytics**: Add charts for accuracy trends over multiple games.  
- 🖥️ **High Score Leaderboard**: Persist top scores locally and show historical progress.  
- 🎨 **Custom Skins & Themes**: Choose from different snake/food designs and UI themes.  

---

## 🕹️ How to Run

1. Clone the repo:  
   ```bash
   git clone <https://github.com/JANARTHANAN-2006/Snake-Accuracy-Reflex-Tester>

Compile and run the game using Java 17+:

javac SnakeAccuracyTester.java
java SnakeAccuracyTester