package Client_Java.player.model;

import Server_Java.idls.GameIDL.GameService;
import Server_Java.idls.PlayerCallBackIDL.GameCallBackService;
import org.omg.CORBA.StringHolder;

import java.util.ArrayList;
import java.util.List;

public class GameRoomModel {
    private final GameService gameService;

    public GameRoomModel(GameService gameService) {
        this.gameService = gameService;
    }

    public void registerCallback(int playerId, String gameToken, String sessionToken, GameCallBackService callbackStub) {
        try {
            gameService.registerCallBack(playerId, gameToken, sessionToken, callbackStub);
            System.out.println("[GameRoomModel] Game callback registered for session=" + sessionToken);
        } catch (Exception e) {
            System.err.println("[GameRoomModel] registerCallback failed: " + e.getMessage());
        }
    }
    public int startRound(String gameToken, int roundNumber, int playerId, String sessionToken) {
        try {
            return gameService.startRound(gameToken, roundNumber, playerId, sessionToken);
        } catch (Exception e) {
            System.err.println("[GameRoomModel] startRound failed: " + e.getMessage());
            return 0;
        }

    }

    public String getRandomWord(String gameToken, int roundNumber, int playerId, String sessionToken) {
        try {
            return gameService.getRandomWord(gameToken, roundNumber, playerId, sessionToken);
        } catch (Exception e) {
            System.err.println("[GameRoomModel] getRandomWord failed: " + e.getMessage());
            return "";
        }
    }

    public List<Integer> guessLetter(String gameToken,
                                     int playerId,
                                     String sessionToken,
                                     char letter) {
        try {
            int[] positions = gameService.guessLetter(gameToken, playerId, sessionToken, letter);
            List<Integer> result = new ArrayList<>();
            for (long p : positions) {
                result.add((int)p);
            }
            return result;
        } catch (Exception e) {
            System.err.println("[GameRoomModel] guessLetter failed: " + e.getMessage());
            return new ArrayList<>();                // or Collections.emptyList()
        }
    }

    public int getNumberOfLives(String sessionToken) {
        try {
            StringHolder h = new StringHolder();
            gameService.getSetting("number_of_lives", h, sessionToken);
            return Integer.parseInt(h.value);
        } catch (Exception e) {
            System.err.println("[GameRoomModel] getNumberOfLives failed: " + e.getMessage());
            return 0;
        }
    }
    public String getRoundWinner(String gameToken, int playerId, String sessionToken) {
        try {
            return gameService.getRoundWinner(gameToken, playerId, sessionToken);
        } catch (Exception e) {
            System.err.println("[GameRoomModel] getRoundWinner failed: " + e.getMessage());
            return "Unknown";
        }
    }
    public String getPlayerDisplayName(int playerId, String sessionToken) {
        try {
            return gameService.getDisplayName(playerId, sessionToken);
        } catch (Exception e) {
            System.err.println("[GameRoomModel] getPlayerDisplayName failed: " + e.getMessage());
            return "";
        }
    }
    /**
     * Fetches the configured delay (in seconds) before the next round starts.
     */
    public int getNextRoundDelay(String sessionToken) {
        try {
            StringHolder h = new StringHolder();
            gameService.getSetting("next_round_delay", h, sessionToken);
            return Integer.parseInt(h.value);
        } catch (Exception e) {
            System.err.println("[GameRoomModel] getNextRoundDelay failed: " + e.getMessage());
            return 0;
        }
    }

}
