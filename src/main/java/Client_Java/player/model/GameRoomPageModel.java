package Client_Java.player.model;

import PlayerGame.MaxAttemptsReachedException;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import PlayerGame.GameService;
import PlayerGame.GameNotFoundException;

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

    public String guessLetter(int playerID, String sessionToken, char letter) {
        try {
            // CORBA returns IndexList, which is a sequence<long>
            int[] result = gameService.guessLetter(playerID, sessionToken, letter);

            return result.length > 0 ? "Correct!" : "Incorrect!";
        } catch (GameNotFoundException e) {
            e.printStackTrace();
            return "Game not found.";
        } catch (MaxAttemptsReachedException e) {
            e.printStackTrace();
            return "Max attempts have been reached.";
        } catch (Exception e) {
            e.printStackTrace();
            return "An unexpected error occurred.";
        }
    }
}