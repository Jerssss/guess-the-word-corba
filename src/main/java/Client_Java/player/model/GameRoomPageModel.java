package Client_Java.player.model;

import PlayerGame.*;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

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

    public String guessLetter(int playerId, String sessionToken, char letter) throws MaxAttemptsReachedException, GameNotFoundException, AlreadyGuessedLetterException, NotLoggedInException {
        try {
            int[] result = gameService.guessLetter(playerId, sessionToken, letter); // Call the server method
//            return result == 1 ? "Correct!" : "Incorrect!";
        } catch (GameNotFoundException e) {
            e.printStackTrace();
            return "Game not found.";
        } catch (MaxAttemptsReachedException e) {
            e.printStackTrace();
            return "Max attempts have been reached.";
        }
        return null; //RETURNS NULL FOR NOW FOR THE SAKE OF RUNNIGN THE CODE
    }
}