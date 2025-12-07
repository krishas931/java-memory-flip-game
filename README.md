# java-memory-flip-game
# Memory Flip Game – Java Swing 🎮🧠

A desktop-based memory card flip game built using Java Swing. Match pairs of tiles before the timer runs out across 3 increasing difficulty levels. Includes a real-time leaderboard stored using CSV file handling.

---

## ✨ Features

- 3 Game Levels (Alphabets → Numbers → Fruits)
- Timer-based gameplay ⏱️
- Score calculation (Match = +10, Wrong = -2)
- Pause & Resume functionality
- Leaderboard with Top 3 scores 🏆 stored in CSV
- Simple and friendly UI using Java Swing
- Encourages concentration and memory skills

---

## 🧠 Game Rules

- Click 2 tiles → If same → they stay flipped
- Wrong match → they flip back after a short delay
- Complete 8 pairs to finish the level
- Level time gets harder:
  - Level 1 → 60 seconds
  - Level 2 → 50 seconds
  - Level 3 → 40 seconds
- Score saved automatically after game ends

---

## 🔧 Tech Stack

| Component | Details |
|----------|---------|
| Language | Java |
| GUI | Swing (JFrame, JButton, JLabel, Timer) |
| File Handling | `leaderboard.csv` |
| Game Design | Random shuffle + event handling |

---

## 📂 Project Structure

