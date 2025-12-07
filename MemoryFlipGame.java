import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.Timer;

public class MemoryFlipGame {

    private JFrame frame;
    private JPanel panel, buttonPanel;
    private JButton[] buttons;
    private String[] values;
    private Color[] colors;
    private int firstSelection = -1, secondSelection = -1, matchedPairs = 0, level = 1, timeLeft, moves;
    private Timer gameTimer;
    private JLabel timerLabel;
    private boolean isPaused = false;
    private JButton pauseButton, resumeButton, newGameButton, exitButton, leaderboardButton;
    private long levelStartTime;
    private int[] levelScores = {0, 0, 0};
    private String csvFile = "leaderboard.csv";
    private String playerName;
    private static final int NUM_TILES = 16;
    private static final int NUM_COLORS = 8;
    private static final int CORRECT_MATCH_SCORE = 10;
    private static final int INCORRECT_FLIP_PENALTY = -2;
    private long levelTimeLimit;
    private long pauseStartTime;
    private int totalScore = 0;
    private boolean gameFailed = false;

    private static final Color[] LIGHT_COLORS = {
            new Color(255, 204, 204),
            new Color(204, 255, 204),
            new Color(204, 204, 255),
            new Color(255, 255, 204),
            new Color(255, 204, 255),
            new Color(255, 229, 204),
            new Color(229, 204, 255),
            new Color(204, 255, 255)
    };

    public MemoryFlipGame() {

        playerName = JOptionPane.showInputDialog("Enter your name:");
        if (playerName == null || playerName.trim().isEmpty()) System.exit(0);

        frame = new JFrame("Memory Flip Game");
        frame.setSize(600, 750);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        timerLabel = new JLabel("Level: " + level + " | Time: --s", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 30));
        frame.add(timerLabel, BorderLayout.NORTH);

        panel = new JPanel(new GridLayout(4, 4, 10, 10));
        frame.add(panel, BorderLayout.CENTER);

        buttonPanel = new JPanel(new GridLayout(1, 5, 10, 10));
        frame.add(buttonPanel, BorderLayout.SOUTH);

        createControlButtons();
        loadLevel();
        frame.setVisible(true);
    }

    private void createControlButtons() {
        String[] buttonNames = {"Pause", "Resume", "New Game", "Exit", "Leaderboard"};
        Color[] buttonColors = {Color.CYAN, Color.GREEN, Color.GREEN, Color.RED, Color.ORANGE};

        for (int i = 0; i < buttonNames.length; i++) {
            JButton button = new JButton(buttonNames[i]);
            button.setBackground(buttonColors[i]);
            button.addActionListener(e -> handleButtonAction(button.getText()));
            buttonPanel.add(button);

            if (buttonNames[i].equals("Pause")) pauseButton = button;
            if (buttonNames[i].equals("Resume")) {
                resumeButton = button;
                resumeButton.setEnabled(false);
            }
            if (buttonNames[i].equals("New Game")) newGameButton = button;
            if (buttonNames[i].equals("Exit")) exitButton = button;
            if (buttonNames[i].equals("Leaderboard")) leaderboardButton = button;
        }
    }

    private void handleButtonAction(String action) {
        switch (action) {
            case "Pause" -> pauseGame();
            case "Resume" -> resumeGame();
            case "New Game" -> resetGame();
            case "Exit" -> frame.dispose();
            case "Leaderboard" -> showLeaderboard();
        }
    }

    private void pauseGame() {
        gameTimer.stop();
        pauseButton.setEnabled(false);
        resumeButton.setEnabled(true);
        pauseStartTime = System.currentTimeMillis();
        isPaused = true;
        setCardsEnabled(false);
    }

    private void resumeGame() {
        levelStartTime += System.currentTimeMillis() - pauseStartTime;
        gameTimer.start();
        pauseButton.setEnabled(true);
        resumeButton.setEnabled(false);
        isPaused = false;
        setCardsEnabled(true);
    }

    private void resetGame() {
        level = 1;
        moves = 0;
        totalScore = 0;
        levelScores = new int[]{0, 0, 0};
        gameFailed = false;
        loadLevel();
    }

    private void loadLevel() {
        setTimerForLevel();
        levelStartTime = System.currentTimeMillis();

        String[][] levelValues = {
                {"A","A","B","B","C","C","D","D","E","E","F","F","G","G","H","H"},
                {"1","1","2","2","3","3","4","4","5","5","6","6","7","7","8","8"},
                {"Apple","Apple","Banana","Banana","Cherry","Cherry","Date","Date","Elder","Elder","Fig","Fig","Grape","Grape","Melon","Melon"}
        };

        values = levelValues[level - 1];
        shuffleArray();
        assignColors();

        panel.removeAll();
        buttons = new JButton[NUM_TILES];

        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = new JButton();
            buttons[i].setBackground(Color.BLACK);
            buttons[i].setFont(new Font("Arial", Font.BOLD, 18));
            buttons[i].addActionListener(new TileClickListener(i));
            panel.add(buttons[i]);
        }

        panel.revalidate();
        panel.repaint();
        matchedPairs = 0;
        firstSelection = secondSelection = -1;
        startTimer();
    }

    private void setTimerForLevel() {
        levelTimeLimit = switch (level) {
            case 1 -> 60000;
            case 2 -> 50000;
            default -> 40000;
        };
    }

    private void startTimer() {
        gameTimer = new Timer(1000, e -> {
            if (!isPaused) {
                long elapsed = System.currentTimeMillis() - levelStartTime;
                timeLeft = (int)((levelTimeLimit - elapsed) / 1000);
                if (timeLeft <= 0) {
                    timeLeft = 0;
                    gameTimer.stop();
                    gameFailed = true;
                    JOptionPane.showMessageDialog(frame,"Time's up! Level Failed.");
                    saveProgress(false);
                    frame.dispose();
                }
                timerLabel.setText("Level: " + level + " | Time: " + timeLeft + "s");
            }
        });
        gameTimer.start();
    }

    private void shuffleArray() {
        java.util.List<String> list = Arrays.asList(values);
        Collections.shuffle(list);
        list.toArray(values);
    }

    private void assignColors() {
        colors = new Color[NUM_TILES];
        Map<String, Color> colorMap = new HashMap<>();
        int colorIndex = 0;
        for (int i = 0; i < values.length; i++) {
            colorMap.putIfAbsent(values[i], LIGHT_COLORS[colorIndex++ % LIGHT_COLORS.length]);
            colors[i] = colorMap.get(values[i]);
        }
    }

    private void setCardsEnabled(boolean enable) {
        for (JButton b : buttons) b.setEnabled(enable);
    }

    private void saveProgress(boolean completed) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile, true))) {
            File file = new File(csvFile);
            if (file.length() == 0) {
                writer.write("Name,Score");
                writer.newLine();
            }
            writer.write(playerName + "," + totalScore);
            writer.newLine();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Error saving score!");
        }
    }

    private void showLeaderboard() {
        try {
            java.util.List<String> lines = Files.readAllLines(Paths.get(csvFile));
            if (lines.size() <= 1) {
                JOptionPane.showMessageDialog(frame,"No scores yet!");
                return;
            }

            java.util.List<String> scores = lines.subList(1, lines.size());
            scores.sort((a,b) -> Integer.parseInt(b.split(",")[1]) -
                    Integer.parseInt(a.split(",")[1]));

            StringBuilder sb = new StringBuilder("<html><h2>Leaderboard</h2><ol>");
            for (int i = 0; i < Math.min(3, scores.size()); i++) {
                String[] parts = scores.get(i).split(",");
                sb.append("<li>").append(parts[0]).append(" - ").append(parts[1]).append("</li>");
            }
            sb.append("</ol></html>");
            JOptionPane.showMessageDialog(frame, sb.toString());

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Error reading leaderboard!");
        }
    }

    private class TileClickListener implements ActionListener {
        private final int tileIndex;

        public TileClickListener(int index) { this.tileIndex = index; }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (firstSelection == -1) {
                firstSelection = tileIndex;
                revealCard(tileIndex);
            } else if (secondSelection == -1 && tileIndex != firstSelection) {
                secondSelection = tileIndex;
                revealCard(tileIndex);

                Timer flipBackTimer = new Timer(500, evt -> {
                    checkMatch();
                    ((Timer)evt.getSource()).stop();
                });
                flipBackTimer.start();
            }
        }
    }

    private void revealCard(int i) {
        buttons[i].setText(values[i]);
        buttons[i].setBackground(colors[i]);
    }

    private void hideCard(int i) {
        buttons[i].setText("");
        buttons[i].setBackground(Color.BLACK);
    }

    private void checkMatch() {
        if (values[firstSelection].equals(values[secondSelection])) {
            buttons[firstSelection].setEnabled(false);
            buttons[secondSelection].setEnabled(false);
            matchedPairs++;
            totalScore += CORRECT_MATCH_SCORE;

            if (matchedPairs == NUM_COLORS) {
                gameTimer.stop();
                if (level < 3) {
                    JOptionPane.showMessageDialog(frame, "Level " + level + " Completed!");
                    level++;
                    loadLevel();
                } else {
                    JOptionPane.showMessageDialog(frame, "Game Complete! Final Score: " + totalScore);
                    saveProgress(true);
                    showLeaderboard();
                    frame.dispose();
                }
            }
        } else {
            hideCard(firstSelection);
            hideCard(secondSelection);
            totalScore += INCORRECT_FLIP_PENALTY;
        }
        firstSelection = secondSelection = -1;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MemoryFlipGame::new);
    }
}
