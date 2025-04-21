package Client_Java.player.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameRoomPageModel {
    private int remainingAttempts;
    private String currentWord;
    private Map<Character, List<Integer>> letterPositions;

    public GameRoomPageModel(String currentWord) {
        this.currentWord = currentWord;
        this.remainingAttempts = 5; // Set initial attempts
        this.letterPositions = new HashMap<>();
        // Initialize letter positions based on the current word
        initializeLetterPositions();
    }

    private void initializeLetterPositions() {
        for (int i = 0; i < currentWord.length(); i++) {
            char letter = currentWord.charAt(i);
            letterPositions.computeIfAbsent(letter, k -> new ArrayList<>()).add(i);
        }
    }

    public String guessLetter(int playerID, String sessionToken, char letter) {
        if (remainingAttempts <= 0) {
            return "No remaining attempts!";
        }

        if (letterPositions.containsKey(letter)) {
            return "Correct!"; // Letter is in the word
        } else {
            decrementAttempts();
            return "Incorrect!"; // Letter is not in the word
        }
    }

    public void decrementAttempts() {
        remainingAttempts--;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    public String getCurrentWord() {
        return currentWord;
    }

    public List<Integer> getLetterPositions(char letter) {
        return letterPositions.getOrDefault(letter, new ArrayList<>());
    }

    public void submitWord(int playerID, String sessionToken, String submittedWord) {
        if (submittedWord.equals(currentWord)) {
            // Logic for correct word submission
            System.out.println("Word submitted correctly!");
        } else {
            // Logic for incorrect word submission
            System.out.println("Incorrect word submitted.");
        }
    }

    public Map<Character, List<Integer>> getAllLetterPositions() {
        return letterPositions; // Fixed the recursive call
    }
}