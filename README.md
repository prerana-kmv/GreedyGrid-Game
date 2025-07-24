# 🎮 GreedyGrid: Outsmart the Grid!

Welcome to **GreedyGrid** — a fun little Java game that blends strategy, luck, and pathfinding into a clean and clickable 5×5 grid. Your goal? Get from the top-left corner to the bottom-right… *without racking up a huge score*. Sounds easy? Think again.

---

## 🧩 What's the Challenge?

You start at `(0,0)` on a 5×5 board filled with numbers.  
Each move adds the cell’s number to your score.  
You can only move **up, down, left, or right** — no diagonals, no cheats.

The twist?  
The game uses **Dijkstra’s Algorithm** behind the scenes to calculate the best possible score. Your job is to **beat it — or at least match it.**

---

## 🕹️ Features You’ll Love

- ✅ Slick Java Swing interface
- ✅ Easy, Medium, and Hard difficulty modes
- ✅ “Show Best Path” option powered by Dijkstra’s algorithm
- ✅ Highlight your valid next moves
- ✅ Save and load your game with a single click
- ✅ Colorful animations and a visual win/loss feedback system

---

## 🛠️ Getting Started

> You’ll need **Java JDK 8 or above** to run this game.

### 🔧 To compile:
```bash
javac src/GreedyGridGameGUI.java
