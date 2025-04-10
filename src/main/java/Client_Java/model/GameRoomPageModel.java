package Client_Java.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class GameRoomPageModel {
    private IntegerProperty lifeCount = new SimpleIntegerProperty(5);
    private String currentWord;
    private int roundCount = 0;
    private int playerWins = 0;
    private int opponentWins = 0;

    public IntegerProperty lifeCountProperty() {
        return lifeCount;
    }

    public void guessLetter(char letter) {
        //TODO: Logic for guessing a letter
        // Update UI and check if the letter is correct
    }

    public void submitWord(String word) {
        //TODO: Logic for submitting a word
        // Check if the word matches the currentWord
    }

    public void returnToHome() {
        //TODO: Logic to return to the home screen
    }

    public void startNewRound() {
        //TODO: Logic to start a new round
        // Select a new word and reset attempts
    }

    public void endRound(boolean playerWon) {
        // Logic to end the round
        if (playerWon) {
            playerWins++;
        } else {
            opponentWins++;
        }
        if (playerWins == 3 || opponentWins == 3) {
            endGame();
        } else {
            startNewRound();
        }
    }

    private void endGame() {
        //TODO: Logic to end the game
        // Return to the home screen
    }
}