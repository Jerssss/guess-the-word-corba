package Server_Java.implementation;

import Server_Java.database.DatabaseConnection;
import Server_Java.idls.GameIDL.GameServicePOA;
import Server_Java.idls.GameIDL.NotEnoughPlayersException;
import Server_Java.idls.GameIDL.NotLoggedInException;
import Server_Java.idls.GameIDL.GameTimeOutException;
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

    // Minimum players to start (could fetch from settings dynamically)
    private static final int DEFAULT_MIN_PLAYERS = 2;

    private static class Lobby {
        final String token;
        final List<Integer> players = new CopyOnWriteArrayList<>();
        final List<GameCallBackService> callbacks = new CopyOnWriteArrayList<>();
        Lobby(String token) { this.token = token; }
    }

    @Override
    public String joinLobby(int playerID, String sessionToken) throws NotLoggedInException {
        if (sessionToken == null) throw new NotLoggedInException();
        // Create or reuse a lobby
        String gameToken = sessionToGame.computeIfAbsent(sessionToken, st -> {
            String newToken = UUID.randomUUID().toString();
            lobbies.put(newToken, new Lobby(newToken));
            return newToken;
        });
        Lobby lobby = lobbies.get(gameToken);
        boolean added = false;
        if (!lobby.players.contains(playerID)) {
            lobby.players.add(playerID);
            added = true;
        }
        // Debug: print online vs lobby vs in-game counts
        int onlineCount = sessionToGame.size();
        int totalLobbyPlayers = lobbies.values().stream().mapToInt(l -> l.players.size()).sum();
        int inGameCount = onlineCount - totalLobbyPlayers;
        System.out.println("[GameService DEBUG] joinLobby: added=" + added + ", online=" + onlineCount +
                ", inLobby=" + totalLobbyPlayers + ", inGame=" + inGameCount);
        return gameToken;
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
    public void registerCallBack(int playerID, String gameToken, String sessionToken, GameCallBackService cb)
            throws NotLoggedInException {
        Lobby lobby = lobbies.get(gameToken);
        if (lobby == null) throw new NotLoggedInException();
        lobby.callbacks.add(cb);
        System.out.println("[GameService DEBUG] registerCallBack: playerID=" + playerID + ", gameToken=" + gameToken +
                ", callbacksCount=" + lobby.callbacks.size());
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
