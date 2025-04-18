package Server_Java.implementation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Game {
    private String currentWord;
    private int roundCount = 0;
    private int playerWins = 0;
    private int opponentWins = 0;
    private int remainingAttempts = 5;
    private boolean roundActive = false;
    private Map<Character, List<Integer>> letterPositions = new HashMap<>();
    private List<String> usedWords = new ArrayList<>();
    private long roundStartTime;
    private List<String> words;

    public Game() {
        words = loadWords();
    }

    public void startRound() {
        currentWord = getRandomWord();
        roundCount++;
        remainingAttempts = 5;
        roundActive = true;
        letterPositions.clear();
        roundStartTime = System.currentTimeMillis();
    }

    public String getRandomWord() {
        Random random = new Random();
        String word;
        do {
            word = words.get(random.nextInt(words.size()));
        } while (usedWords.contains(word));
        usedWords.add(word);
        return word;
    }

    public String guessLetter(int playerID, char letter) {
        if (!roundActive) {
            return "Round is not active.";
        }

        if (!currentWord.contains(String.valueOf(letter))) {
            remainingAttempts--;
            return "Incorrect! Remaining attempts: " + remainingAttempts;
        }

        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < currentWord.length(); i++) {
            if (currentWord.charAt(i) == letter) {
                positions.add(i);
            }
        }
        letterPositions.put(letter, positions);
        return "Correct! Positions: " + positions;
    }

    public boolean submitWord(int playerID, String word) {
        if (!roundActive) {
            return false;
        }

        if (word.equalsIgnoreCase(currentWord)) {
            endRound(true);
            return true;
        } else {
            remainingAttempts--;
            return false;
        }
    }

    public void endRound(boolean playerWon) {
        if (playerWon) {
            playerWins++;
        } else {
            opponentWins++;
        }

        if (playerWins >= 3 || opponentWins >= 3) {
            endGame();
        } else {
            startRound();
        }
    }

    private void endGame() {
        // Logic to end the game and notify players
        System.out.println("Game over! Player wins: " + playerWins + ", Opponent wins: " + opponentWins);
    }

    private List<String> loadWords() {
        List<String> wordList = new ArrayList<>();
        File file = new File("src/main/java/Client_Java/admin/res/words.txt");

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String word = scanner.nextLine().trim();
                if (!word.isEmpty()) {
                    wordList.add(word);
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: words.txt file not found at " + file.getAbsolutePath());
            e.printStackTrace();
        }

        if (wordList.isEmpty()) {
            System.err.println("Warning: No words loaded from words.txt.");
        }

        return wordList;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    public String getCurrentLetterState() {
        StringBuilder state = new StringBuilder();
        for (int i = 0; i < currentWord.length(); i++) {
            char c = currentWord.charAt(i);
            if (letterPositions.containsKey(c)) {
                state.append(c);
            } else {
                state.append('_'); // Placeholder for unguessed letters
            }
        }
        return state.toString();
    }

    public long getRoundStartTime() {
        return roundStartTime;
    }

    public int getRemainingRoundTime() {
        long elapsedTime = System.currentTimeMillis() - roundStartTime;
        return Math.max(0, 30000 - (int) elapsedTime); // 30 seconds per round
    }

}