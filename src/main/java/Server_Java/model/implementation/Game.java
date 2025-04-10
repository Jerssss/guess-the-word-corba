package Server_Java.model.implementation;

import java.util.*;

public class Game {
    private String currentWord;
    private int roundCount = 0;
    private int playerWins = 0;
    private int opponentWins = 0;
    private int remainingAttempts = 5;
    private boolean roundActive = false;
    private Map<Character, List<Integer>> letterPositions = new HashMap<>();

    public Game() {
        // Initialize game state
    }

    public void startRound() {
        currentWord = getRandomWord();
        roundCount++;
        remainingAttempts = 5;
        roundActive = true;
    }

    public String getRandomWord() {
        List<String> words = loadWords();
        Random random = new Random();
        return words.get(random.nextInt(words.size()));
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
    }

    private List<String> loadWords() {
        //TODO: Logic to load words from "words.txt
        return Arrays.asList("sample", "placeholder", "wohohohoho");
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
}