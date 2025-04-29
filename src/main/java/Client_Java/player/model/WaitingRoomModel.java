
package Client_Java.player.model;


import Server_Java.idls.GameIDL.GameService;
import Server_Java.idls.PlayerCallBackIDL.GameCallBackService;
import org.omg.CORBA.StringHolder;

/**
 * Model for waiting room: handles joining the lobby, fetching settings,
 * polling player counts, and registering callbacks.
 */
public class WaitingRoomModel {
    private final GameService gameService;
    public void leaveLobby(int playerId, String gameToken, String sessionToken) {
        try {
            gameService.leaveLobby(playerId, gameToken, sessionToken);
        } catch (Exception e) {
            System.err.println("[WaitingRoomModel] leaveLobby failed: " + e.getMessage());
        }
    }

    public WaitingRoomModel(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Joins the game lobby on the server and returns a game token.
     */
    public String joinLobby(int playerId, String sessionToken) {
        try {
            return gameService.joinLobby(playerId, sessionToken);
        } catch (Exception e) {
            System.err.println("[WaitingRoomModel] Error joining lobby: " + e.getMessage());
            return null;
        }
    }

    /**
     * Registers a callback with the server to receive game start notifications.
     */
    public void registerCallback(int playerId, String gameToken,
                                 String sessionToken,
                                 GameCallBackService callbackStub) {
        try {
            gameService.registerCallBack(playerId, gameToken, sessionToken, callbackStub);
        } catch (Exception e) {
            System.err.println("[WaitingRoomModel] Error registering callback: " + e.getMessage());
        }
    }

    /**
     * Retrieves a named integer setting from the server.
     */
    public int getSetting(String key, String sessionToken) {
        StringHolder holder = new StringHolder();
        try {
            gameService.getSetting(key, holder, sessionToken);
            return Integer.parseInt(holder.value);
        } catch (Exception e) {
            System.err.println("[WaitingRoomModel] Error fetching setting '" + key + "': " + e.getMessage());
            return 0;
        }
    }

    /**
     * Retrieves the current number of players joined from the server.
     */
    public int getNumberOfPlayersJoined(int playerId, String sessionToken) {
        try {
            return gameService.getNumberOfPlayersJoined(playerId, sessionToken);
        } catch (Exception e) {
            System.err.println("[WaitingRoomModel] Error fetching joined count: " + e.getMessage());
            return 0;
        }
    }
}