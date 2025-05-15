

package Client_Java.player.model;


import Client_Java.player.SessionManager;
import GameIDL.GameService;
import GameIDL.NotEnoughPlayersException;
import GameIDL.GameTimeOutException;
import GameIDL.NotLoggedInException;
import PlayerCallBackIDL.GameCallBackService;
import PlayerCallBackIDL.WaitingRoomGameCallbackService;
import org.omg.CORBA.StringHolder;


public class WaitingRoomModel {
    private final GameService gameService;


    public WaitingRoomModel(GameService gameService) {
        this.gameService = gameService;
    }


    public String joinLobby(int playerId) {
        try {
            String sessionToken = SessionManager.getSessionToken();
            String gameToken = gameService.joinLobby(playerId, sessionToken);
            SessionManager.setGameToken(gameToken);
            return gameToken;
        } catch (NotLoggedInException e) {
            System.err.println("[WaitingRoomModel] joinLobby failed: Not logged in");
            return null;
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
        } catch (NotLoggedInException e) {
            System.err.println("[WaitingRoomModel] leaveLobby failed: Not logged in");
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
        } catch (NotLoggedInException e) {
            System.err.println("[WaitingRoomModel] registerWaitingRoomCallback failed: Not logged in");
        } catch (Exception e) {
            System.err.println("[WaitingRoomModel] registerWaitingRoomCallback failed: " + e.getMessage());
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
        } catch (NotLoggedInException e) {
            System.err.println("[WaitingRoomModel] registerGameStartCallback failed: Not logged in");
        } catch (Exception e) {
            System.err.println("[WaitingRoomModel] registerGameStartCallback failed: " + e.getMessage());
        }
    }


    public int getSetting(String key) {
        StringHolder holder = new StringHolder();
        try {
            gameService.getSetting(key, holder, SessionManager.getSessionToken());
            return Integer.parseInt(holder.value);
        } catch (NumberFormatException e) {
            System.err.println("[WaitingRoomModel] getSetting('" + key + "') failed: Invalid number format");
            return 0;
        } catch (NotLoggedInException e) {
            System.err.println("[WaitingRoomModel] getSetting('" + key + "') failed: Not logged in");
            return 0;
        } catch (Exception e) {
            System.err.println("[WaitingRoomModel] getSetting('" + key + "') failed: " + e.getMessage());
            return 0;
        }
    }


    public int getNumberOfPlayersJoined() {
        try {
            return gameService.getNumberOfPlayersJoined(
                    SessionManager.getLoggedInPlayer().getPlayerId(),
                    SessionManager.getSessionToken()
            );
        } catch (NotLoggedInException e) {
            System.err.println("[WaitingRoomModel] getNumberOfPlayersJoined failed: Not logged in");
            return 0;
        } catch (Exception e) {
            System.err.println("[WaitingRoomModel] getNumberOfPlayersJoined failed: " + e.getMessage());
            return 0;
        }
    }


    public int startGame(int playerId) throws NotEnoughPlayersException, GameTimeOutException, NotLoggedInException {
        try {
            return gameService.startGame(playerId, SessionManager.getSessionToken());
        } catch (NotEnoughPlayersException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("[WaitingRoomModel] startGame failed: " + e.getMessage());
            throw new RuntimeException("Failed to start game", e);
        }
    }


    public String getLobbyStatus() throws NotLoggedInException, GameTimeOutException, NotEnoughPlayersException {
        try {
            return gameService.getLobbyStatus(SessionManager.getSessionToken());
        } catch (NotLoggedInException | GameTimeOutException | NotEnoughPlayersException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("[WaitingRoomModel] getLobbyStatus failed: " + e.getMessage());
            throw new RuntimeException("Failed to get lobby status", e);
        }
    }


    public int getRemainingCountdownTime() {
        try {
            String status = getLobbyStatus();
            // Parse status, e.g., "Lobby <token>: waiting, Players: 2/4, Countdown: 8 seconds"
            String[] parts = status.split(", Countdown: ");
            if (parts.length > 1) {
                String countdownPart = parts[1];
                if (countdownPart.contains("waiting for players")) {
                    return -1; // Indicates waiting state
                }
                String secondsStr = countdownPart.replace(" seconds", "").trim();
                return Integer.parseInt(secondsStr);
            }
            return -1; // Default to waiting state if no countdown info
        } catch (NumberFormatException e) {
            System.err.println("[WaitingRoomModel] getRemainingCountdownTime failed: Invalid countdown format");
            return -1;
        } catch (NotLoggedInException | GameTimeOutException | NotEnoughPlayersException e) {
            System.err.println("[WaitingRoomModel] getRemainingCountdownTime failed: " + e.getMessage());
            return -1;
        } catch (Exception e) {
            System.err.println("[WaitingRoomModel] getRemainingCountdownTime failed: " + e.getMessage());
            return -1;
        }
    }
}
