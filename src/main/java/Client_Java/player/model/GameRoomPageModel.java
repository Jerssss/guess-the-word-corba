package Client_Java.player.model;

import GameApp.MaxAttemptsReachedException;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import GameApp.GameService;
import GameApp.GameNotFoundException;

public class GameRoomPageModel {
    private IntegerProperty remainingAttempts = new SimpleIntegerProperty(5);
    private int playerWins = 0;
    private int opponentWins = 0;
    private GameService gameService;

    public GameRoomPageModel(GameService gameService) {
        this.gameService = gameService; // Initialize the game service
    }

    public IntegerProperty remainingAttemptsProperty() {
        return remainingAttempts;
    }

    public void decrementAttempts() {
        remainingAttempts.set(remainingAttempts.get() - 1);
    }

    public void incrementPlayerWins() {
        playerWins++;
    }

    public String guessLetter(int playerID, char letter) {
        try {
            int result = gameService.guessLetter(playerID, letter); // Call the server method
            return result == 1 ? "Correct!" : "Incorrect!";
        } catch (GameNotFoundException e) {
            e.printStackTrace();
            return "Game not found.";
        } catch (MaxAttemptsReachedException e) {
            e.printStackTrace();
            return "Max attempts have been reached.";
        }
    }
}