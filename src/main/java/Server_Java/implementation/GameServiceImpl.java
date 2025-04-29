package Server_Java.implementation;

import Server_Java.database.DatabaseConnection;
import Server_Java.idls.GameIDL.GameServicePOA;
import Server_Java.idls.GameIDL.AlreadyGuessedLetterException;
import Server_Java.idls.GameIDL.GameNotFoundException;
import Server_Java.idls.GameIDL.GameTimeOutException;
import Server_Java.idls.GameIDL.MaxAttemptsReachedException;
import Server_Java.idls.GameIDL.NotEnoughPlayersException;
import Server_Java.idls.GameIDL.NotLoggedInException;
import Server_Java.idls.PlayerCallBackIDL.GameCallBackService;
import Server_Java.idls.PlayerCallBackIDL.WaitingRoomGameCallbackService;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Server-side implementation for GameService, handling lobby,
 * waiting-room callbacks, game start callbacks, and settings.
 */
public class GameServiceImpl extends GameServicePOA {

    private static final int DEFAULT_MIN_PLAYERS       = 2;
    private static final int DEFAULT_COUNTDOWN_SECONDS = 10; // or load via settings

    // Maps gameToken → Lobby
    private static final Map<String, Lobby> lobbies = new ConcurrentHashMap<>();
    // Maps sessionToken → gameToken
    private static final Map<String, String> sessionToGame = new ConcurrentHashMap<>();
    // Maps sessionToken → game-start callback stub
    private static final Map<String, GameCallBackService> sessionToCallback = new ConcurrentHashMap<>();
    // Maps sessionToken → waiting-room callback stub
    private static final Map<String, WaitingRoomGameCallbackService> sessionToWaitingCallback = new ConcurrentHashMap<>();

    // — NEW: scheduler to delay countdown notifications —
    private static final ScheduledExecutorService countdownScheduler =
            Executors.newSingleThreadScheduledExecutor();
    // Tracks pending countdown tasks by lobby token
    private static final Map<String, ScheduledFuture<?>> pendingCountdowns =
            new ConcurrentHashMap<>();

    /**
     * Represents a lobby waiting to start:
     * - players: the playerIDs joined
     * - callbacks: stubs to notify when game really starts
     * - waitingCallbacks: stubs to notify on join/countdown/reset
     */
    private static class Lobby {
        final String token;
        final List<Integer> players           = new CopyOnWriteArrayList<>();
        final List<GameCallBackService> callbacks         = new CopyOnWriteArrayList<>();
        final List<WaitingRoomGameCallbackService> waitingCallbacks = new CopyOnWriteArrayList<>();

        Lobby(String token) { this.token = token; }
    }

    @Override
    public synchronized String joinLobby(int playerID, String sessionToken)
            throws NotLoggedInException
    {
        if (sessionToken == null)
            throw new NotLoggedInException();

        // Already in a lobby?
        if (sessionToGame.containsKey(sessionToken)) {
            String existingToken = sessionToGame.get(sessionToken);
            debugPrintAllLobbies("joinLobby (already in)");
            return existingToken;
        }

        // Find a lobby that still needs players
        Lobby target = null;
        for (Lobby l : lobbies.values()) {
            if (l.players.size() < DEFAULT_MIN_PLAYERS) {
                target = l;
                break;
            }
        }
        // Or create a fresh one
        if (target == null) {
            String newToken = UUID.randomUUID().toString();
            target = new Lobby(newToken);
            lobbies.put(newToken, target);
            System.out.println("[GameService DEBUG] Created new lobby: " + newToken);
        }

        // Add player
        if (!target.players.contains(playerID)) {
            target.players.add(playerID);
            sessionToGame.put(sessionToken, target.token);
            System.out.println("[GameService DEBUG] joinLobby: player=" + playerID +
                    " joined lobby=" + target.token +
                    " (count=" + target.players.size() + ")");
        }

        // Notify waiting-room callbacks of new count
        for (WaitingRoomGameCallbackService cb : target.waitingCallbacks) {
            try {
                cb.notifyPlayerJoined(
                        target.token,
                        target.players.size(),
                        sessionToken
                );
            } catch (Exception ignored) {}
        }

        // If threshold reached, schedule countdown after a short delay
        if (target.players.size() == DEFAULT_MIN_PLAYERS) {
            // Cancel any existing pending countdown
            ScheduledFuture<?> old = pendingCountdowns.remove(target.token);
            if (old != null) {
                old.cancel(false);
            }

            // Schedule new countdown 100ms later
            Lobby finalTarget = target;
            ScheduledFuture<?> future = countdownScheduler.schedule(() -> {
                for (WaitingRoomGameCallbackService cb : finalTarget.waitingCallbacks) {
                    try {
                        cb.notifyCountdownStart(
                                finalTarget.token,
                                DEFAULT_COUNTDOWN_SECONDS,
                                sessionToken
                        );
                    } catch (Exception ignored) {}
                }
                System.out.println("[GameService DEBUG] scheduled notifyCountdownStart for lobby="
                        + finalTarget.token);
            }, 100, TimeUnit.MILLISECONDS);

            pendingCountdowns.put(target.token, future);
        }

        debugPrintAllLobbies("joinLobby");
        return target.token;
    }

    @Override
    public synchronized void leaveLobby(int playerID,
                                        String gameToken,
                                        String sessionToken)
            throws NotLoggedInException
    {
        if (sessionToken == null || !sessionToGame.containsKey(sessionToken))
            throw new NotLoggedInException();

        Lobby lobby = lobbies.get(gameToken);
        if (lobby != null) {
            // Remove all instances of this player
            boolean removedAny = lobby.players.removeIf(pid -> pid == playerID);
            // Remove their waiting-room callback stub
            WaitingRoomGameCallbackService wcb = sessionToWaitingCallback.remove(sessionToken);
            if (wcb != null) lobby.waitingCallbacks.remove(wcb);

            System.out.println("[GameService DEBUG] leaveLobby: player=" + playerID +
                    " left lobby=" + gameToken +
                    " removedAny=" + removedAny +
                    " (new count=" + lobby.players.size() + ")");

            // Notify remaining callbacks of new count
            for (WaitingRoomGameCallbackService cb : lobby.waitingCallbacks) {
                try {
                    cb.notifyPlayerJoined(gameToken, lobby.players.size(), sessionToken);
                } catch (Exception ignored) {}
            }

            // If dropped below threshold, cancel pending countdown and reset
            if (lobby.players.size() < DEFAULT_MIN_PLAYERS) {
                ScheduledFuture<?> future = pendingCountdowns.remove(gameToken);
                if (future != null) {
                    future.cancel(false);
                }

                for (WaitingRoomGameCallbackService cb : lobby.waitingCallbacks) {
                    try {
                        cb.notifyCountdownReset(gameToken, sessionToken);
                    } catch (Exception ignored) {}
                }
                System.out.println("[GameService DEBUG] notifyCountdownReset for lobby="
                        + gameToken);
            }

            // If the lobby is empty, remove it entirely
            if (lobby.players.isEmpty()) {
                lobbies.remove(gameToken);
                System.out.println("[GameService DEBUG] Lobby " + gameToken + " removed (empty)");
            }
        }

        // Clean up mappings
        sessionToGame.remove(sessionToken);
        sessionToWaitingCallback.remove(sessionToken);

        debugPrintAllLobbies("leaveLobby");
    }

    @Override
    public synchronized void registerWaitingRoomCallback(
            int playerID,
            String gameToken,
            String sessionToken,
            WaitingRoomGameCallbackService cb
    ) throws NotLoggedInException {
        if (sessionToken == null
                || !sessionToGame.containsKey(sessionToken)
                || !sessionToGame.get(sessionToken).equals(gameToken)
        ) throw new NotLoggedInException();

        Lobby lobby = lobbies.get(gameToken);
        if (lobby == null)
            throw new NotLoggedInException();

        lobby.waitingCallbacks.add(cb);
        sessionToWaitingCallback.put(sessionToken, cb);

        System.out.println("[GameService DEBUG] registerWaitingRoomCallback: player="
                + playerID + " in lobby=" + gameToken
                + " (waitingCallbacks=" + lobby.waitingCallbacks.size() + ")");
    }

    @Override
    public String getLobbyStatus(String sessionToken)
            throws NotLoggedInException, GameTimeOutException, NotEnoughPlayersException
    {
        return "";
    }

    @Override
    public int getNumberOfPlayersJoined(int playerID, String sessionToken)
            throws NotLoggedInException
    {
        String token = sessionToGame.get(sessionToken);
        if (token == null) throw new NotLoggedInException();
        Lobby lobby = lobbies.get(token);
        int size = (lobby != null) ? lobby.players.size() : 0;
        System.out.println("[GameService DEBUG] getNumberOfPlayersJoined for lobby="
                + token + ": " + size);
        return size;
    }

    @Override
    public int startGame(int playerID, String sessionToken) throws NotEnoughPlayersException {
        String token = sessionToGame.get(sessionToken);
        Lobby lobby = (token == null ? null : lobbies.get(token));
        if (lobby == null || lobby.players.size() < DEFAULT_MIN_PLAYERS)
            throw new NotEnoughPlayersException();

        System.out.println("[GameService DEBUG] startGame: lobby=" + token
                + " (players=" + lobby.players.size() + ")");
        // Notify game-start callbacks
        for (GameCallBackService cb : lobby.callbacks) {
            try { cb.notifyGameStart(token, sessionToken); }
            catch (Exception ignored) {}
        }

        // Clean up
        lobbies.remove(token);
        sessionToGame.values().removeIf(t -> t.equals(token));
        sessionToCallback.keySet().removeIf(st -> !sessionToGame.containsKey(st));
        sessionToWaitingCallback.keySet().removeIf(st -> !sessionToGame.containsKey(st));

        System.out.println("[GameService DEBUG] startGame completed: active lobbies="
                + lobbies.size());
        return lobby.players.size();
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
        System.out.println("[GameService DEBUG] registerCallBack: player="
                + playerID + ", lobby=" + gameToken
                + " (gameCallbacks=" + lobby.callbacks.size() + ")");
    }

    @Override
    public int startRound(String gameToken, int roundNumber, int playerID, String sessionToken)
            throws GameNotFoundException, NotLoggedInException
    {
        return 0;
    }

    @Override
    public String getRandomWord(String gameToken, int roundNumber, int playerID, String sessionToken)
            throws GameNotFoundException, NotLoggedInException
    {
        return "";
    }

    @Override
    public int[] guessLetter(String gameToken, int playerID, String sessionToken, char letter)
            throws MaxAttemptsReachedException, GameNotFoundException, AlreadyGuessedLetterException, NotLoggedInException
    {
        return new int[0];
    }

    @Override
    public String getRoundWinner(String gameToken, int playerID, String sessionToken)
            throws NotLoggedInException
    {
        return "";
    }

    @Override
    public String getGameWinner(String gameToken, int playerID, String sessionToken)
            throws NotLoggedInException
    {
        return "";
    }

    @Override
    public int getPlayerWins(String gameToken, int playerID, String sessionToken)
            throws NotLoggedInException
    {
        return 0;
    }

    @Override
    public String getDisplayName(int playerID, String sessionToken)
            throws NotLoggedInException
    {
        return "";
    }

    @Override
    public String[] getLeaderboards(int playerID, String sessionToken)
            throws NotLoggedInException
    {
        return new String[0];
    }

    @Override
    public void getSetting(String key, StringHolder value, String sessionToken) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT " + key + " FROM settings WHERE id=1";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) value.value = rs.getString(1);
            else            value.value = "";
        } catch (SQLException e) {
            value.value = "";
            System.err.println("[GameService] Error fetching setting '" + key + "': " + e);
        }
    }

    /** Helper to log all lobby contents for debug. */
    private void debugPrintAllLobbies(String context) {
        System.out.println(">>> [GameService DEBUG][" + context + "] Active lobbies:");
        if (lobbies.isEmpty()) {
            System.out.println("    (none)");
        } else {
            for (Lobby l : lobbies.values()) {
                System.out.println("    - " + l.token
                        + ": players=" + l.players
                        + " callbacks=" + l.waitingCallbacks.size()
                        + "/" + l.callbacks.size());
            }
        }
    }
}
