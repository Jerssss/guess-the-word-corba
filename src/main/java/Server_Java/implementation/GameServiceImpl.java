package Server_Java.implementation;

import Server_Java.database.DatabaseConnection;
import Server_Java.idls.GameIDL.GameServicePOA;
import Server_Java.idls.GameIDL.NotEnoughPlayersException;
import Server_Java.idls.GameIDL.NotLoggedInException;
import Server_Java.idls.PlayerCallBackIDL.GameCallBackService;
import org.omg.CORBA.StringHolder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Server-side implementation for GameService, handling lobby and callbacks.
 */
public class GameServiceImpl extends GameServicePOA {

    // In-memory storage of lobbies: token -> Lobby
    private static final Map<String, Lobby> lobbies = new ConcurrentHashMap<>();
    // Map sessionToken to gameToken
    private static final Map<String, String> sessionToGame = new ConcurrentHashMap<>();
    // NEW: map sessionToken → callback stub
    private static final Map<String, GameCallBackService> sessionToCallback = new ConcurrentHashMap<>();

    // Minimum players to start (could fetch from settings dynamically)
    private static final int DEFAULT_MIN_PLAYERS = 2;

    private static class Lobby {
        final String token;
        final List<Integer> players = new CopyOnWriteArrayList<>();
        final List<GameCallBackService> callbacks = new CopyOnWriteArrayList<>();
        Lobby(String token) { this.token = token; }
    }

    @Override
    public synchronized String joinLobby(int playerID, String sessionToken)
            throws NotLoggedInException
    {
        if (sessionToken == null) throw new NotLoggedInException();

        // 1) Already joined?
        if (sessionToGame.containsKey(sessionToken)) {
            System.out.println("[GameService DEBUG] joinLobby: player=" + playerID +
                    " already in lobby=" + sessionToGame.get(sessionToken) +
                    " (count=" + lobbies.get(sessionToGame.get(sessionToken)).players.size() + ")");
            debugPrintAllLobbies("joinLobby (already in)");
            return sessionToGame.get(sessionToken);
        }

        // 2) Find or create a waiting lobby
        Lobby target = null;
        for (Lobby l : lobbies.values()) {
            if (l.players.size() < DEFAULT_MIN_PLAYERS) {
                target = l;
                break;
            }
        }
        if (target == null) {
            target = new Lobby(UUID.randomUUID().toString());
            lobbies.put(target.token, target);
            System.out.println("[GameService DEBUG] Created new lobby: " + target.token);
        }

        // 3) Add player if not already present
        if (!target.players.contains(playerID)) {
            target.players.add(playerID);
            sessionToGame.put(sessionToken, target.token);
            System.out.println("[GameService DEBUG] joinLobby: player=" + playerID +
                    " joined lobby=" + target.token +
                    " (count=" + target.players.size() + ")");
        } else {
            System.out.println("[GameService DEBUG] joinLobby: player=" + playerID +
                    " was already in lobby=" + target.token +
                    " (count=" + target.players.size() + ")");
        }

        debugPrintAllLobbies("joinLobby");
        return target.token;
    }

    /** Helper to dump all lobbies & their player counts */
    private void debugPrintAllLobbies(String context) {
        System.out.println(">>> [GameService DEBUG][" + context + "] Current lobbies:");
        for (Lobby l : lobbies.values()) {
            System.out.println("    - " + l.token + ": players=" +
                    l.players + " (count=" + l.players.size() + ")");
        }
        if (lobbies.isEmpty()) {
            System.out.println("    (none)");
        }
    }

    @Override
    public synchronized void leaveLobby(int playerID, String gameToken, String sessionToken)
            throws NotLoggedInException
    {
        if (sessionToken == null || !sessionToGame.containsKey(sessionToken))
            throw new NotLoggedInException();

        Lobby lobby = lobbies.get(gameToken);
        if (lobby != null) {
            // Remove *every* occurrence of this player
            boolean removedAny = lobby.players.removeIf(pid -> pid == playerID);

            // Remove their callback stub (if any)
            GameCallBackService cb = sessionToCallback.remove(sessionToken);
            if (cb != null) lobby.callbacks.remove(cb);

            System.out.println("[GameService DEBUG] leaveLobby: player=" + playerID +
                    " left lobby=" + gameToken +
                    " removedAny=" + removedAny +
                    " (new count=" + lobby.players.size() + ")");

            // If nobody left, destroy the lobby
            if (lobby.players.isEmpty()) {
                lobbies.remove(gameToken);
                System.out.println("[GameService DEBUG] Lobby " + gameToken + " is now empty → removed");
            }
        }

        // Finally drop the session→lobby mapping
        sessionToGame.remove(sessionToken);
        debugPrintAllLobbies("leaveLobby");
    }


    @Override
    public int getNumberOfPlayersJoined(int playerID, String sessionToken) throws NotLoggedInException {
        String gameToken = sessionToGame.get(sessionToken);
        if (gameToken == null) throw new NotLoggedInException();
        Lobby lobby = lobbies.get(gameToken);
        int size = (lobby != null) ? lobby.players.size() : 0;
        System.out.println("[GameService DEBUG] getNumberOfPlayersJoined for gameToken=" + gameToken + ": " + size);
        return size;
    }


    @Override
    public synchronized void registerCallBack(
            int playerID,
            String gameToken,
            String sessionToken,
            GameCallBackService cb
    ) throws NotLoggedInException {
        Lobby lobby = lobbies.get(gameToken);
        if (lobby == null) throw new NotLoggedInException();

        lobby.callbacks.add(cb);
        sessionToCallback.put(sessionToken, cb);
    }

    @Override
    public String getLobbyStatus(String sessionToken) {
        // Optional: return serialized lobby info
        return "";
    }

    @Override
    public int startGame(int playerID, String sessionToken) throws NotEnoughPlayersException {
        String gameToken = sessionToGame.get(sessionToken);
        Lobby lobby = lobbies.get(gameToken);
        if (lobby == null || lobby.players.size() < DEFAULT_MIN_PLAYERS) {
            throw new NotEnoughPlayersException();
        }
        // Debug before starting game
        int onlineCount = sessionToGame.size();
        int totalLobbyPlayers = lobby.players.size();
        int inGameCount = onlineCount - totalLobbyPlayers;
        System.out.println("[GameService DEBUG] startGame: online=" + onlineCount +
                ", inLobby=" + totalLobbyPlayers + ", inGame(before)=" + inGameCount);
        // Notify all players via callback
        for (GameCallBackService cb : lobby.callbacks) {
            try { cb.notifyGameStart(gameToken, sessionToken); } catch (Exception ignored) {}
        }
        // Cleanup lobby
        lobbies.remove(gameToken);
        sessionToGame.values().removeIf(token -> token.equals(gameToken));
        // Debug after cleanup
        onlineCount = sessionToGame.size();
        totalLobbyPlayers = lobbies.values().stream().mapToInt(l -> l.players.size()).sum();
        inGameCount = onlineCount - totalLobbyPlayers;
        System.out.println("[GameService DEBUG] startGame completed: online=" + onlineCount +
                ", inLobby=" + totalLobbyPlayers + ", inGame(after)=" + inGameCount);
        return lobby.players.size();
    }



    // Existing setting fetch remains unchanged
    @Override
    public void getSetting(String key, StringHolder value, String sessionToken) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT " + key + " FROM settings WHERE id = 1";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) value.value = rs.getString(1);
            else value.value = "";
        } catch (SQLException e) {
            System.err.println("[GameService] Error fetching setting '" + key + "': " + e.getMessage());
            value.value = "";
        }
    }

    // Unused methods stubbed
    @Override public String getDisplayName(int playerID, String sessionToken) { return null; }
    @Override public String[] getLeaderboards(int playerID, String sessionToken) { return new String[0]; }
    @Override public String getRandomWord(String gameToken, int roundNumber, int playerID, String sessionToken) { return null; }
    @Override public int startRound(String gameToken, int roundNumber, int playerID, String sessionToken) { return 0; }
    @Override public int[] guessLetter(String gameToken, int playerID, String sessionToken, char letter) { return new int[0]; }
    @Override public String getRoundWinner(String gameToken, int playerID, String sessionToken) { return null; }
    @Override public String getGameWinner(String gameToken, int playerID, String sessionToken) { return null; }
    @Override public int getPlayerWins(String gameToken, int playerID, String sessionToken) { return 0; }
}
