package Client_Java.player.model;

import Server_Java.idls.GameIDL.GameService;
import Server_Java.idls.PlayerCallBackIDL.GameCallBackService;
import Server_Java.idls.PlayerCallBackIDL.WaitingRoomGameCallbackService;
import org.omg.CORBA.StringHolder;

/**
 * Model for the waiting room: 
 * - joins and leaves the lobby
 * - fetches settings
 * - queries player counts
 * - registers both game-start and waiting-room callbacks
 */
public class WaitingRoomModel {
    private final GameService gameService;

    /**
     * @param gameService the CORBA stub for GameService
     */
    public WaitingRoomModel(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Joins the game lobby on the server and returns a gameToken.
     */
    public String joinLobby(int playerId, String sessionToken) {
        try {
            return gameService.joinLobby(playerId, sessionToken);
        } catch (Exception e) {
            System.err.println("[WaitingRoomModel] joinLobby failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Leaves the waiting lobby (called on Cancel).
     */
    public void leaveLobby(int playerId, String gameToken, String sessionToken) {
        try {
            gameService.leaveLobby(playerId, gameToken, sessionToken);
        } catch (Exception e) {
            System.err.println("[WaitingRoomModel] leaveLobby failed: " + e.getMessage());
        }
    }

    /**
     * Registers the client's waiting-room callback servant with the server.
     */
    public void registerWaitingRoomCallback(
            int playerId,
            String gameToken,
            String sessionToken,
            WaitingRoomGameCallbackService cb
    ) {
        try {
            gameService.registerWaitingRoomCallback(
                    playerId, gameToken, sessionToken, cb
            );
        } catch (Exception e) {
            System.err.println("[WaitingRoomModel] registerWaitingRoomCallback failed: "
                    + e.getMessage());
        }
    }

    /**
     * Registers the client's game-start callback servant with the server.
     */
    public void registerCallback(
            int playerId,
            String gameToken,
            String sessionToken,
            GameCallBackService callbackStub
    ) {
        try {
            gameService.registerCallBack(playerId, gameToken, sessionToken, callbackStub);
        } catch (Exception e) {
            System.err.println("[WaitingRoomModel] registerCallback failed: " + e.getMessage());
        }
    }

    /**
     * Retrieves a named integer setting (e.g. "minimum_players") from the server.
     */
    public int getSetting(String key, String sessionToken) {
        StringHolder holder = new StringHolder();
        try {
            gameService.getSetting(key, holder, sessionToken);
            return Integer.parseInt(holder.value);
        } catch (Exception e) {
            System.err.println("[WaitingRoomModel] getSetting('" + key + "') failed: "
                    + e.getMessage());
            return 0;
        }
    }

    /**
     * Retrieves the current number of players in the lobby.
     */
    public int getNumberOfPlayersJoined(int playerId, String sessionToken) {
        try {
            return (int)gameService.getNumberOfPlayersJoined(playerId, sessionToken);
        } catch (Exception e) {
            System.err.println("[WaitingRoomModel] getNumberOfPlayersJoined failed: "
                    + e.getMessage());
            return 0;
        }
    }
}
