import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

public class GreedyGridGameGUI extends JFrame {
    private static final int N = 5;
    private static final int CELL_SIZE = 80;
    private static final Color GRID_COLOR = new Color(50, 50, 50);
    private static final Color HIGHLIGHT_COLOR = new Color(255, 255, 150);
    private static final Color PATH_COLOR = new Color(150, 200, 255, 100);
    private static final Color START_COLOR = new Color(100, 255, 100);
    private static final Color END_COLOR = new Color(255, 150, 150);
    private static final Color CURRENT_COLOR = new Color(255, 200, 100);
    
    private int[][] grid = new int[N][N];
    private int playerX = 0, playerY = 0, playerScore = 0;
    private int bestScore = 0;
    private ArrayList<Point> playerPath = new ArrayList<>();
    private ArrayList<Point> algorithmPath = new ArrayList<>();
    private boolean gameStarted = false;
    private boolean gameOver = false;
    private JPanel gridPanel;
    private JLabel scoreLabel, messageLabel;
    private JButton newGameButton, showPathButton, saveGameButton, loadGameButton;
    private JCheckBox highlightValidMovesCheckbox;
    private boolean showAlgorithmPath = false;
    private boolean highlightValidMoves = true;
    private int difficulty = 1; // 1-Easy, 2-Medium, 3-Hard
    private javax.swing.Timer animationTimer;  // Explicitly using javax.swing.Timer
    private int moveCount = 0;
    
    public GreedyGridGameGUI() {
        setTitle("Greedy Grid Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        initComponents();
        showInstructions();
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void initComponents() {
        // Grid Panel
        gridPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGrid(g);
            }
        };
        gridPanel.setPreferredSize(new Dimension(N * CELL_SIZE, N * CELL_SIZE));
        gridPanel.addMouseListener(new GridClickListener());
        
        // Control Panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        
        // Score Display
        scoreLabel = new JLabel("Score: 0 | Best Possible: 0");
        scoreLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Message Display
        messageLabel = new JLabel("Welcome to Greedy Grid Game!");
        messageLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Buttons Panel
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        
        newGameButton = new JButton("New Game");
        newGameButton.addActionListener(e -> startNewGame());
        
        showPathButton = new JButton("Show Best Path");
        showPathButton.addActionListener(e -> toggleAlgorithmPath());
        
        saveGameButton = new JButton("Save Game");
        saveGameButton.addActionListener(e -> saveGame());
        
        loadGameButton = new JButton("Load Game");
        loadGameButton.addActionListener(e -> loadGame());
        
        highlightValidMovesCheckbox = new JCheckBox("Highlight Valid Moves", highlightValidMoves);
        highlightValidMovesCheckbox.addActionListener(e -> {
            highlightValidMoves = highlightValidMovesCheckbox.isSelected();
            gridPanel.repaint();
        });
        
        // Difficulty selector
        JPanel difficultyPanel = new JPanel(new FlowLayout());
        JLabel difficultyLabel = new JLabel("Difficulty:");
        ButtonGroup difficultyGroup = new ButtonGroup();
        
        JRadioButton easyButton = new JRadioButton("Easy", true);
        JRadioButton mediumButton = new JRadioButton("Medium");
        JRadioButton hardButton = new JRadioButton("Hard");
        
        easyButton.addActionListener(e -> difficulty = 1);
        mediumButton.addActionListener(e -> difficulty = 2);
        hardButton.addActionListener(e -> difficulty = 3);
        
        difficultyGroup.add(easyButton);
        difficultyGroup.add(mediumButton);
        difficultyGroup.add(hardButton);
        
        difficultyPanel.add(difficultyLabel);
        difficultyPanel.add(easyButton);
        difficultyPanel.add(mediumButton);
        difficultyPanel.add(hardButton);
        
        // Add all components to control panel
        buttonsPanel.add(newGameButton);
        buttonsPanel.add(showPathButton);
        buttonsPanel.add(saveGameButton);
        buttonsPanel.add(loadGameButton);
        buttonsPanel.add(highlightValidMovesCheckbox);
        
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(scoreLabel);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(messageLabel);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(buttonsPanel);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(difficultyPanel);
        controlPanel.add(Box.createVerticalStrut(10));
        
        // Add panels to the frame
        add(gridPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
    }
    
    private void showInstructions() {
        JOptionPane.showMessageDialog(this,
            "~~~ How to Play: ~~~\n" +
            "1. You are on a 5x5 grid with numbers (0–9).\n" +
            "2. Start at top-left (0,0) and reach bottom-right (4,4).\n" +
            "3. Click on adjacent cells to move to them.\n" +
            "4. Each move adds the cell's number to your total score.\n" +
            "5. Beat or match the algorithm's best score to win!\n\n" +
            "Features:\n" +
            "- Set difficulty level to adjust the challenge\n" +
            "- See the best possible path with the 'Show Best Path' button\n" +
            "- Save and load games\n" +
            "- Highlight valid moves to help you plan",
            "Greedy Grid - Instructions",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void startNewGame() {
        playerX = 0;
        playerY = 0;
        playerScore = 0;
        moveCount = 0;
        gameStarted = true;
        gameOver = false;
        playerPath.clear();
        algorithmPath.clear();
        playerPath.add(new Point(0, 0));
        
        generateGrid();
        bestScore = findBestPath();
        
        updateScoreLabel();
        messageLabel.setText("Game started! Try to reach (4,4) with score <= " + bestScore);
        showAlgorithmPath = false;
        
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        
        gridPanel.repaint();
    }
    
    private void generateGrid() {
        Random rand = new Random();
        
        // Set difficulty-based number ranges
        int maxNumber;
        switch (difficulty) {
            case 1: // Easy
                maxNumber = 5; // 0-4
                break;
            case 2: // Medium
                maxNumber = 7; // 0-6
                break;
            case 3: // Hard
                maxNumber = 10; // 0-9
                break;
            default:
                maxNumber = 5;
        }
        
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                grid[i][j] = rand.nextInt(maxNumber);
            }
        }
        
        // Ensure start and end are always 0
        grid[0][0] = 0;
        grid[N-1][N-1] = 0;
    }
    
    private void drawGrid(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw the cells
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                int x = j * CELL_SIZE;
                int y = i * CELL_SIZE;
                
                // Cell background
                if (i == 0 && j == 0) {
                    g2d.setColor(START_COLOR);
                } else if (i == N-1 && j == N-1) {
                    g2d.setColor(END_COLOR);
                } else if (i == playerY && j == playerX && gameStarted) {
                    g2d.setColor(CURRENT_COLOR);
                } else if (isValidMove(j, i) && highlightValidMoves && gameStarted && !gameOver) {
                    g2d.setColor(HIGHLIGHT_COLOR);
                } else {
                    g2d.setColor(Color.WHITE);
                }
                g2d.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                
                // Draw path
                boolean onPlayerPath = false;
                for (Point p : playerPath) {
                    if (p.x == j && p.y == i) {
                        onPlayerPath = true;
                        break;
                    }
                }
                
                if (onPlayerPath && !(i == playerY && j == playerX)) {
                    g2d.setColor(PATH_COLOR);
                    g2d.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                }
                
                // Draw algorithm path
                if (showAlgorithmPath) {
                    boolean onAlgoPath = false;
                    for (Point p : algorithmPath) {
                        if (p.x == j && p.y == i) {
                            onAlgoPath = true;
                            break;
                        }
                    }
                    
                    if (onAlgoPath) {
                        g2d.setColor(new Color(100, 200, 100, 70));
                        g2d.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                    }
                }
                
                // Cell border
                g2d.setColor(GRID_COLOR);
                g2d.drawRect(x, y, CELL_SIZE, CELL_SIZE);
                
                // Cell value
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.BOLD, 24));
                String value = String.valueOf(grid[i][j]);
                FontMetrics metrics = g2d.getFontMetrics();
                int textX = x + (CELL_SIZE - metrics.stringWidth(value)) / 2;
                int textY = y + ((CELL_SIZE - metrics.getHeight()) / 2) + metrics.getAscent();
                g2d.drawString(value, textX, textY);
            }
        }
    }
    
    private boolean isValidMove(int x, int y) {
        if (!gameStarted || gameOver) return false;
        
        // Check if adjacent to current position
        return (Math.abs(x - playerX) + Math.abs(y - playerY) == 1) &&
               (x >= 0 && x < N && y >= 0 && y < N);
    }
    
    private int findBestPath() {
        // Dijkstra's algorithm for finding shortest path
        int[][] dist = new int[N][N];
        for (int[] row : dist) Arrays.fill(row, Integer.MAX_VALUE);
        dist[0][0] = 0;
        
        // For reconstructing the path
        int[][] prev = new int[N][N];
        for (int[] row : prev) Arrays.fill(row, -1);
        
        // Priority queue for Dijkstra's algorithm
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[2]));
        pq.add(new int[]{0, 0, 0});
        
        int[][] directions = {{0,1},{1,0},{0,-1},{-1,0}};
        
        while (!pq.isEmpty()) {
            int[] curr = pq.poll();
            int x = curr[0], y = curr[1], cost = curr[2];
            
            if (x == N-1 && y == N-1) {
                // Reconstruct the path
                reconstructPath(prev);
                return cost;
            }
            
            if (cost > dist[y][x]) continue;
            
            for (int[] dir : directions) {
                int nx = x + dir[0];
                int ny = y + dir[1];
                if (nx >= 0 && nx < N && ny >= 0 && ny < N) {
                    int newCost = cost + grid[ny][nx];
                    if (newCost < dist[ny][nx]) {
                        dist[ny][nx] = newCost;
                        pq.add(new int[]{nx, ny, newCost});
                        prev[ny][nx] = (y * N + x);
                    }
                }
            }
        }
        
        return -1; // Should never happen with our grid
    }
    
    private void reconstructPath(int[][] prev) {
        algorithmPath.clear();
        
        int x = N-1, y = N-1;
        ArrayList<Point> reversePath = new ArrayList<>();
        reversePath.add(new Point(x, y));
        
        while (x != 0 || y != 0) {
            int prevPos = prev[y][x];
            int prevY = prevPos / N;
            int prevX = prevPos % N;
            
            x = prevX;
            y = prevY;
            reversePath.add(new Point(x, y));
        }
        
        // Reverse the path
        for (int i = reversePath.size() - 1; i >= 0; i--) {
            algorithmPath.add(reversePath.get(i));
        }
    }
    
    private void updateScoreLabel() {
        scoreLabel.setText("Score: " + playerScore + " | Best Possible: " + bestScore + 
                          " | Moves: " + moveCount);
    }
    
    private void toggleAlgorithmPath() {
        showAlgorithmPath = !showAlgorithmPath;
        if (showAlgorithmPath) {
            showPathButton.setText("Hide Best Path");
        } else {
            showPathButton.setText("Show Best Path");
        }
        gridPanel.repaint();
    }
    
    private void saveGame() {
        if (!gameStarted) {
            JOptionPane.showMessageDialog(this, "No active game to save!", 
                                         "Save Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Game");
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().endsWith(".grid")) {
                file = new File(file.getAbsolutePath() + ".grid");
            }
            
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                // Save game state
                oos.writeObject(grid);
                oos.writeInt(playerX);
                oos.writeInt(playerY);
                oos.writeInt(playerScore);
                oos.writeInt(bestScore);
                oos.writeInt(moveCount);
                oos.writeObject(playerPath);
                oos.writeObject(algorithmPath);
                oos.writeBoolean(gameStarted);
                oos.writeBoolean(gameOver);
                oos.writeInt(difficulty);
                
                JOptionPane.showMessageDialog(this, "Game saved successfully!", 
                                             "Save Game", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving game: " + e.getMessage(), 
                                             "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadGame() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Load Game");
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                // Load game state
                grid = (int[][]) ois.readObject();
                playerX = ois.readInt();
                playerY = ois.readInt();
                playerScore = ois.readInt();
                bestScore = ois.readInt();
                moveCount = ois.readInt();
                playerPath = (ArrayList<Point>) ois.readObject();
                algorithmPath = (ArrayList<Point>) ois.readObject();
                gameStarted = ois.readBoolean();
                gameOver = ois.readBoolean();
                difficulty = ois.readInt();
                
                updateScoreLabel();
                if (gameOver) {
                    messageLabel.setText(playerScore <= bestScore ? 
                                        "You won with a score of " + playerScore + "!" : 
                                        "You lost with a score of " + playerScore + ".");
                } else {
                    messageLabel.setText("Game loaded! Continue to reach (4,4).");
                }
                
                gridPanel.repaint();
                JOptionPane.showMessageDialog(this, "Game loaded successfully!", 
                                             "Load Game", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException | ClassNotFoundException e) {
                JOptionPane.showMessageDialog(this, "Error loading game: " + e.getMessage(), 
                                             "Load Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void gameWon() {
        gameOver = true;
        boolean didWin = playerScore <= bestScore;
        
        String message = didWin ? 
            "Congratulations! You won with a score of " + playerScore + "!" : 
            "You lost with a score of " + playerScore + ". The best score was " + bestScore + ".";
        
        messageLabel.setText(message);
        
        // Animate the win
        animationTimer = new javax.swing.Timer(100, new ActionListener() {  // Explicitly using javax.swing.Timer
            int count = 0;
            boolean highlight = true;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (count > 10) {
                    ((javax.swing.Timer)e.getSource()).stop();  // Explicitly casting to javax.swing.Timer
                    
                    // Show dialog after animation
                    JOptionPane.showMessageDialog(GreedyGridGameGUI.this, 
                        message + "\n\nClick 'New Game' to play again!", 
                        didWin ? "You Won!" : "You Lost!", 
                        didWin ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
                    
                    return;
                }
                
                highlight = !highlight;
                if (highlight) {
                    messageLabel.setForeground(didWin ? Color.GREEN.darker() : Color.RED);
                } else {
                    messageLabel.setForeground(Color.BLACK);
                }
                
                count++;
            }
        });
        animationTimer.start();
    }
    
    private class GridClickListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (!gameStarted || gameOver) return;
            
            int cellX = e.getX() / CELL_SIZE;
            int cellY = e.getY() / CELL_SIZE;
            
            if (isValidMove(cellX, cellY)) {
                // Move player
                playerX = cellX;
                playerY = cellY;
                playerScore += grid[cellY][cellX];
                moveCount++;
                
                // Add to path
                playerPath.add(new Point(cellX, cellY));
                
                // Update score and check if game is over
                updateScoreLabel();
                
                if (playerX == N-1 && playerY == N-1) {
                    gameWon();
                }
                
                gridPanel.repaint();
            }
        }
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> new GreedyGridGameGUI());
    }
}