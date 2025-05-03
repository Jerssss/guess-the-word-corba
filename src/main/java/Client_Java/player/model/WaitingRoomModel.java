// File: Client_Java/player/model/WaitingRoomModel.java
package Client_Java.player.model;

import Client_Java.player.SessionManager;
import Server_Java.idls.GameIDL.GameService;
import Server_Java.idls.PlayerCallBackIDL.GameCallBackService;
import Server_Java.idls.PlayerCallBackIDL.WaitingRoomGameCallbackService;
import org.omg.CORBA.StringHolder;

/**
 * Model for the waiting room:
 * - joins and leaves the lobby
 * - fetches settings
 * - queries player counts
 * - registers waiting-room and game-start callbacks
 */
public class WaitingRoomModel {
    private final GameService gameService;

    public WaitingRoomModel(GameService gameService) {
        this.gameService = gameService;
    }

    public String joinLobby(int playerId) {
        try {
            String sessionToken = SessionManager.getSessionToken();
            String gameToken = gameService.joinLobby(playerId, sessionToken);
            SessionManager.setGameToken(gameToken); // Store token centrally
            return gameToken;
        } catch (Exception e) {
            System.err.println("[WaitingRoomModel] joinLobby failed: " + e.getMessage());
            return null;
        }
    }

    public void leaveLobby(int playerId) {
        try {
            gameService.leaveLobby(
                    playerId,
                    SessionManager.getGameToken(),
                    SessionManager.getSessionToken()
            );
        } catch (Exception e) {
            System.err.println("[WaitingRoomModel] leaveLobby failed: " + e.getMessage());
        }
    }

    public void registerWaitingRoomCallback(int playerId, WaitingRoomGameCallbackService cb) {
        try {
            gameService.registerWaitingRoomCallback(
                    playerId,
                    SessionManager.getGameToken(),
                    SessionManager.getSessionToken(),
                    cb
            );
        } catch (Exception e) {
            System.err.println("[WaitingRoomModel] registerWaitingRoomCallback failed: "
                    + e.getMessage());
        }
    }

    public void registerGameStartCallback(int playerId, GameCallBackService cb) {
        try {
            gameService.registerCallBack(
                    playerId,
                    SessionManager.getGameToken(),
                    SessionManager.getSessionToken(),
                    cb
            );
        } catch (Exception e) {
            System.err.println("[WaitingRoomModel] registerGameStartCallback failed: "
                    + e.getMessage());
        }
    }

    public int getSetting(String key) {
        StringHolder holder = new StringHolder();
        try {
            gameService.getSetting(key, holder, SessionManager.getSessionToken());
            return Integer.parseInt(holder.value);
        } catch (Exception e) {
            System.err.println("[WaitingRoomModel] getSetting('" + key + "') failed: "
                    + e.getMessage());
            return 0;
        }
    }

    public int getNumberOfPlayersJoined() {
        try {
            return (int) gameService.getNumberOfPlayersJoined(
                    SessionManager.getLoggedInPlayer().getPlayerId(),
                    SessionManager.getSessionToken()
            );
        } catch (Exception e) {
            System.err.println("[WaitingRoomModel] getNumberOfPlayersJoined failed: "
                    + e.getMessage());
            return 0;
        }
    }

    public int startGame(int playerId) throws Exception {
        return gameService.startGame(playerId, SessionManager.getSessionToken());
    }
}
