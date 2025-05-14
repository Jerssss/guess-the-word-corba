package Server_Java.implementation;

import GameIDL.*;
import Server_Java.database.DatabaseConnection;
import PlayerCallBackIDL.GameCallBackService;
import PlayerCallBackIDL.WaitingRoomGameCallbackService;
import org.omg.CORBA.StringHolder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * Server-side implementation for GameService, handling lobby,
 * waiting-room callbacks, game start callbacks, and settings.
 */
public class GameServiceImpl extends GameServicePOA {

    private static final int DEFAULT_MIN_PLAYERS       = 2;
    private static final int DEFAULT_COUNTDOWN_SECONDS = 10;

    // Maps gameToken → Lobby
    private static final Map<String, Lobby> lobbies = new ConcurrentHashMap<>();
    // Maps sessionToken → gameToken
    private static final Map<String, String> sessionToGame = new ConcurrentHashMap<>();
    // Maps sessionToken → game-start callback stub
    private static final Map<String, GameCallBackService> sessionToCallback = new ConcurrentHashMap<>();
    // Maps sessionToken → waiting-room callback stub
    private static final Map<String, WaitingRoomGameCallbackService> sessionToWaitingCallback = new ConcurrentHashMap<>();
    private static final Map<String, String> roundWords = new ConcurrentHashMap<>();
    private static final Map<String, Map<Integer, RoundState>> roundStates = new ConcurrentHashMap<>();
    private static final Map<String, Integer> currentRoundNumbers = new ConcurrentHashMap<>();
    private static final Map<String, String> roundWinners = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Future<?>> roundTimeoutTasks = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> usedWordsPerGame = new ConcurrentHashMap<>();

    // Scheduler for countdowns & timeouts
    private static final ScheduledExecutorService countdownScheduler =
            Executors.newSingleThreadScheduledExecutor();
    private static final Map<String, ScheduledFuture<?>> pendingCountdowns =
            new ConcurrentHashMap<>();
    private static final Random RAND = new Random();

    /**
     * Represents a lobby waiting to start.
     */
    private static class Lobby {
        final String token;
        final int gameId;
        final int    lobbyWaitingTime;
        final int    roundDuration;
        final int    nextRoundDelay;
        final int    countdownSeconds;
        final int    totalRounds;
        final int    minimumPlayers;
        final int    numberOfLives;

        final List<Integer> players               = new CopyOnWriteArrayList<>();
        final Map<String, GameCallBackService> callbacks         = new ConcurrentHashMap<>();
        final List<WaitingRoomGameCallbackService> waitingCallbacks = new CopyOnWriteArrayList<>();

        Lobby(
                String token,
                int gameId,
                int lobbyWaitingTime,
                int roundDuration,
                int nextRoundDelay,
                int countdownSeconds,
                int totalRounds,
                int minimumPlayers,
                int numberOfLives
        ) {
            this.token = token;
            this.gameId = gameId;
            this.lobbyWaitingTime = lobbyWaitingTime;
            this.roundDuration    = roundDuration;
            this.nextRoundDelay   = nextRoundDelay;
            this.countdownSeconds = countdownSeconds;
            this.totalRounds      = totalRounds;
            this.minimumPlayers   = minimumPlayers;
            this.numberOfLives    = numberOfLives;
        }
    }

    private static class RoundState {
        final String word;
        final Set<Character> guessed = ConcurrentHashMap.newKeySet();
        int wrongCount = 0;

        RoundState(String word) {
            this.word = word;
        }
    }

    private static final List<String> WORDS;
    static {
        try {
            WORDS = Files.readAllLines(
                    Paths.get("src/main/java/Server_Java/word/words.txt")
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // Allows a player to join a game lobby and returns the lobby's unique token.
    @Override
    public synchronized String joinLobby(int playerID, String sessionToken)
            throws NotLoggedInException {

        // Check if the player is logged in (valid session token).
        if (sessionToken == null) {
            System.err.println("[GameService ERROR] joinLobby: Null session token for playerID=" + playerID);
            throw new NotLoggedInException();
        }

        // Check if the player is already in a lobby.
        if (sessionToGame.containsKey(sessionToken)) {
            System.out.println("[GameService DEBUG] joinLobby: playerID=" + playerID + " already in lobby=" + sessionToGame.get(sessionToken));
            return sessionToGame.get(sessionToken);
        }

        // 1) Find an existing lobby with space or create a new one.
        Lobby target = null;
        for (Lobby l : lobbies.values()) {
            if (l.players.size() < l.minimumPlayers) {
                target = l;
                break;
            }
        }
        if (target == null) {
            // Helper to fetch game settings from the database.
            Function<String,Integer> fetchInt = key -> {
                StringHolder sh = new StringHolder();
                getSetting(key, sh, sessionToken);
                return Integer.parseInt(sh.value);
            };
            String newToken = UUID.randomUUID().toString();

            // Create a new game record in the database.
            int gameId = -1;
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO games (total_rounds, game_status, start_time) VALUES (?, 'waiting', NOW())",
                         PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, fetchInt.apply("total_rounds"));
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        gameId = rs.getInt(1);
                    }
                }
            } catch (SQLException e) {
                System.err.println("[GameService ERROR] Error creating game record: " + e.getMessage());
                throw new RuntimeException("Failed to create game");
            }

            // Create a new lobby with settings from the database.
            target = new Lobby(
                    newToken,
                    gameId,
                    fetchInt.apply("lobby_waiting_time"),
                    fetchInt.apply("round_duration"),
                    fetchInt.apply("next_round_delay"),
                    fetchInt.apply("countdown_to_game_start"),
                    fetchInt.apply("total_rounds"),
                    fetchInt.apply("minimum_players"),
                    fetchInt.apply("number_of_lives")
            );

            lobbies.put(newToken, target);
            System.out.println("[GameService DEBUG] Created lobby=" + newToken +
                    " with gameId=" + gameId);
        }

        // 2) Add the player to the lobby if not already present.
        if (!target.players.contains(playerID)) {
            target.players.add(playerID);
            sessionToGame.put(sessionToken, target.token);
            System.out.println("[GameService DEBUG] joinLobby: playerID=" + playerID +
                    " joined lobby=" + target.token +
                    " (count=" + target.players.size() + ")");
        }

        // 3) Notify all waiting players about the updated player count.
        for (WaitingRoomGameCallbackService cb : target.waitingCallbacks) {
            try {
                cb.notifyPlayerJoined(
                        target.token,
                        target.players.size(),
                        sessionToken
                );
                System.out.println("[GameService DEBUG] Notified waiting callback for session=" + sessionToken +
                        ", player count=" + target.players.size());
            } catch (Exception e) {
                System.err.println("[GameService ERROR] Failed to notify waiting callback: " + e.getMessage());
            }
        }

        // 4) If enough players are in the waiting lobby, start a countdown to begin the game.
        if (target.players.size() >= target.minimumPlayers) {
            // Cancel any existing pending countdown for this lobby
            ScheduledFuture<?> oldTask = pendingCountdowns.remove(target.token);
            if (oldTask != null) oldTask.cancel(false);

            Lobby lobbyRef = target;
            // Schedule the countdown start almost immediately
            ScheduledFuture<?> newTask = countdownScheduler.schedule(() -> {
                // A) Broadcast “countdown start” to all waiting‐room callbacks
                for (WaitingRoomGameCallbackService cb : lobbyRef.waitingCallbacks) {
                    try {
                        cb.notifyCountdownStart(
                                lobbyRef.token,
                                lobbyRef.countdownSeconds,
                                sessionToken);
                        System.out.println("[GameService DEBUG] Sent notifyCountdownStart to session=" + sessionToken);
                    } catch (Exception e) {
                        System.err.println("[GameService ERROR] Failed to notify countdown start: " + e.getMessage());
                    }
                }
                System.out.println("[GameService DEBUG] notifyCountdownStart sent for lobby=" + lobbyRef.token);

                // B) Schedule the game to start after the countdown finishes.
                countdownScheduler.schedule(() -> {
                    for (Map.Entry<String, GameCallBackService> entry : lobbyRef.callbacks.entrySet()) {
                        String callbackSessionToken = entry.getKey();
                        GameCallBackService gcb = entry.getValue();
                        try {
                            gcb.notifyGameStart(lobbyRef.token, callbackSessionToken);
                            System.out.println("[GameService DEBUG] Sent notifyGameStart to session=" + callbackSessionToken);
                        } catch (Exception e) {
                            System.err.println("[GameService ERROR] Failed to notify game start for session=" + callbackSessionToken + ": " + e.getMessage());
                        }
                    }
                    System.out.println("[GameService DEBUG] notifyGameStart sent for lobby=" + lobbyRef.token);

                    // Schedule the first round
                    countdownScheduler.schedule(() -> {
                        try {
                            startRound(lobbyRef.token, 1);
                            System.out.println("[GameService DEBUG] Scheduled and started round 1 for lobby=" + lobbyRef.token);
                        } catch (GameNotFoundException e) {
                            System.err.println("[GameService ERROR] Failed to start round 1: " + e.getMessage());
                        }
                    }, 5, TimeUnit.SECONDS);
                }, lobbyRef.countdownSeconds, TimeUnit.SECONDS);
            }, 100, TimeUnit.MILLISECONDS);

            pendingCountdowns.put(target.token, newTask);
        }

        debugPrintAllLobbies("joinLobby");
        return target.token;
    }

    // Removes a player from a lobby and updates the game state.
    @Override
    public synchronized void leaveLobby(int playerID, String gameToken, String sessionToken)
            throws NotLoggedInException {

        // Check if the player is logged in and in a lobby.
        if (sessionToken == null || !sessionToGame.containsKey(sessionToken)) {
            System.err.println("[GameService ERROR] leaveLobby: Invalid session token for playerID=" + playerID);
            throw new NotLoggedInException();
        }

        Lobby lobby = lobbies.get(gameToken);
        if (lobby != null) {
            // Remove the player from the lobby.
            lobby.players.removeIf(pid -> pid == playerID);
            WaitingRoomGameCallbackService wcb = sessionToWaitingCallback.remove(sessionToken);
            if (wcb != null) lobby.waitingCallbacks.remove(wcb);

            // Notify remaining players of the updated player count.
            for (WaitingRoomGameCallbackService cb : lobby.waitingCallbacks) {
                try {
                    cb.notifyPlayerJoined(gameToken, lobby.players.size(), sessionToken);
                    System.out.println("[GameService DEBUG] Notified waiting callback for session=" + sessionToken +
                            ", player count=" + lobby.players.size());
                } catch (Exception e) {
                    System.err.println("[GameService ERROR] Failed to notify waiting callback: " + e.getMessage());
                }
            }

            // If too few players remain, cancel the countdown and notify players.
            if (lobby.players.size() < DEFAULT_MIN_PLAYERS) {
                ScheduledFuture<?> future = pendingCountdowns.remove(gameToken);
                if (future != null) future.cancel(false);
                for (WaitingRoomGameCallbackService cb : lobby.waitingCallbacks) {
                    try {
                        cb.notifyCountdownReset(gameToken, sessionToken);
                        System.out.println("[GameService DEBUG] Sent notifyCountdownReset to session=" + sessionToken);
                    } catch (Exception e) {
                        System.err.println("[GameService ERROR] Failed to notify countdown reset: " + e.getMessage());
                    }
                }
            }

            // If the lobby is empty, remove it entirely.
            if (lobby.players.isEmpty()) {
                lobbies.remove(gameToken);
                System.out.println("[GameService DEBUG] Lobby " + gameToken + " removed (empty)");
            }
        }

        // Clean up the player's session data.
        sessionToGame.remove(sessionToken);
        sessionToWaitingCallback.remove(sessionToken);
        debugPrintAllLobbies("leaveLobby");
    }

    // Registers a callback to get updates about the waiting room (like player count).
    @Override
    public synchronized void registerWaitingRoomCallback(
            int playerID,
            String gameToken,
            String sessionToken,
            WaitingRoomGameCallbackService cb
    ) throws NotLoggedInException {
        // Check if the player is logged in and in the correct lobby.
        if (sessionToken == null
                || !sessionToGame.containsKey(sessionToken)
                || !sessionToGame.get(sessionToken).equals(gameToken)
        ) {
            System.err.println("[GameService ERROR] registerWaitingRoomCallback: Invalid session or game token for playerID=" + playerID);
            throw new NotLoggedInException();
        }

        Lobby lobby = lobbies.get(gameToken);
        if (lobby == null) {
            System.err.println("[GameService ERROR] registerWaitingRoomCallback: Lobby not found for gameToken=" + gameToken);
            throw new NotLoggedInException();
        }

        // Add the callback to the lobby and store it for the player's session.
        lobby.waitingCallbacks.add(cb);
        sessionToWaitingCallback.put(sessionToken, cb);
        System.out.println("[GameService DEBUG] registerWaitingRoomCallback: playerID=" + playerID +
                " in lobby=" + gameToken +
                " (waitingCallbacks=" + lobby.waitingCallbacks.size() + ")");

        // Send the current player count to the new callback.
        try {
            cb.notifyPlayerJoined(gameToken, lobby.players.size(), sessionToken);
            System.out.println("[GameService DEBUG] Initial player count notification sent to session=" + sessionToken +
                    ", count=" + lobby.players.size());
        } catch (Exception e) {
            System.err.println("[GameService ERROR] Failed to send initial player count notification: " + e.getMessage());
        }
    }

    // Returns the current status of a lobby
    @Override
    public String getLobbyStatus(String sessionToken)
            throws NotLoggedInException, GameTimeOutException, NotEnoughPlayersException {
        return "";
    }

    // Returns the number of players currently in a lobby.
    @Override
    public int getNumberOfPlayersJoined(int playerID, String sessionToken)
            throws NotLoggedInException {

        // Get the lobby the player is in.
        String token = sessionToGame.get(sessionToken);
        if (token == null) {
            System.err.println("[GameService ERROR] getNumberOfPlayersJoined: No game token for session=" + sessionToken);
            throw new NotLoggedInException();
        }
        Lobby lobby = lobbies.get(token);
        int size = (lobby != null) ? lobby.players.size() : 0;
        System.out.println("[GameService DEBUG] getNumberOfPlayersJoined for lobby=" + token + ": " + size);
        return size;
    }

    // Starts a game if enough players are in the lobby.
    @Override
    public synchronized int startGame(int playerID, String sessionToken)
            throws NotEnoughPlayersException {

        String token = sessionToGame.get(sessionToken);
        Lobby lobby = (token == null ? null : lobbies.get(token));
        if (lobby == null || lobby.players.size() < DEFAULT_MIN_PLAYERS) {
            System.err.println("[GameService ERROR] startGame: Not enough players for session=" + sessionToken);
            throw new NotEnoughPlayersException();
        }

        // Update the game status in the database to "in_progress".
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE games SET game_status = 'in_progress' WHERE game_id = ?")) {
            stmt.setInt(1, lobby.gameId);
            stmt.executeUpdate();
            System.out.println("[GameService DEBUG] Updated game status to in_progress for gameId=" + lobby.gameId);
        } catch (SQLException e) {
            System.err.println("[GameService ERROR] Error updating game status: " + e.getMessage());
        }

        // Notify all players that the game is starting.
        for (Map.Entry<String, GameCallBackService> entry : lobby.callbacks.entrySet()) {
            String callbackSessionToken = entry.getKey();
            GameCallBackService cb = entry.getValue();
            try {
                cb.notifyGameStart(token, callbackSessionToken);
                System.out.println("[GameService DEBUG] Sent notifyGameStart to session=" + callbackSessionToken);
            } catch (Exception e) {
                System.err.println("[GameService ERROR] Failed to notify game start for session=" + callbackSessionToken + ": " + e.getMessage());
            }
        }
        System.out.println("[GameService DEBUG] notifyGameStart sent for lobby=" + token);

        // Schedule the first round to start after a short delay (e.g., 5 seconds).
        countdownScheduler.schedule(() -> {
            try {
                startRound(token, 1);
                System.out.println("[GameService DEBUG] Scheduled and started round 1 for lobby=" + token);
            } catch (GameNotFoundException e) {
                System.err.println("[GameService ERROR] Failed to start round 1: " + e.getMessage());
            }
        }, 5, TimeUnit.SECONDS);

        return lobby.players.size();
    }

    // Registers a callback to get updates about the game
    @Override
    public synchronized void registerCallBack(
            int playerID,
            String gameToken,
            String sessionToken,
            GameCallBackService cb
    ) throws NotLoggedInException {
        Lobby lobby = lobbies.get(gameToken);
        if (lobby == null) {
            throw new NotLoggedInException();
        }
        lobby.callbacks.put(sessionToken, cb);
        System.out.println("[GameService DEBUG] registerCallBack: playerID=" + playerID +
                ", lobby=" + gameToken +
                " (gameCallbacks=" + lobby.callbacks.size() + ")");

        // Check if a round is active and notify the late joiner
        Integer currentRound = currentRoundNumbers.get(gameToken);
        if (currentRound != null) {
            try {
                cb.notifyRoundStart(gameToken, currentRound, sessionToken);
                System.out.println("[GameService DEBUG] Sent notifyRoundStart for round=" + currentRound + " to late joiner playerID=" + playerID);
            } catch (Exception e) {
                System.err.println("[GameService ERROR] Failed to notify late joiner: " + e.getMessage());
            }
        }
    }

    @Override
    public int startRound(String gameToken, int roundNumber, int playerID, String sessionToken) throws GameNotFoundException, NotLoggedInException {
        return 0;
    }

    // Starts a new round in the game with a new word (server-internal).
    public synchronized void startRound(String gameToken, int roundNumber)
            throws GameNotFoundException {
        System.out.println("[GameService DEBUG] startRound called for gameToken=" + gameToken + ", round=" + roundNumber + ", thread=" + Thread.currentThread().getId());

        // Check if the lobby exists.
        Lobby lobby = lobbies.get(gameToken);
        if (lobby == null) {
            System.err.println("[GameService ERROR] startRound: Lobby not found for gameToken=" + gameToken);
            throw new GameNotFoundException();
        }

        // Check if round already exists in database
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM rounds WHERE game_id = ? AND round_number = ?")) {
            stmt.setInt(1, lobby.gameId);
            stmt.setInt(2, roundNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("[GameService DEBUG] Round already exists for gameId=" + lobby.gameId + ", round=" + roundNumber);
                return;
            }
        } catch (SQLException e) {
            System.err.println("[GameService ERROR] Error checking existing round: " + e.getMessage());
        }

        // Select a new unique word for this round
        String roundKey = gameToken + ":" + roundNumber;
        Set<String> usedWords = usedWordsPerGame.computeIfAbsent(gameToken, k -> new HashSet<>());
        String currentWord;
        do {
            currentWord = WORDS.get(RAND.nextInt(WORDS.size())).toUpperCase();
        } while (usedWords.contains(currentWord));
        usedWords.add(currentWord);
        roundWords.put(roundKey, currentWord);
        System.out.println("[GameService DEBUG] Assigned new word for gameToken=" + gameToken + ", round=" + roundNumber + ": " + currentWord);

        // Save the round details in the database.
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO rounds (game_id, round_number, word, start_time) VALUES (?, ?, ?, NOW())")) {
            stmt.setInt(1, lobby.gameId);
            stmt.setInt(2, roundNumber);
            stmt.setString(3, currentWord);
            stmt.executeUpdate();
            System.out.println("[GameService DEBUG] Created round record for gameId=" + lobby.gameId + ", round=" + roundNumber + ", word=" + currentWord);
        } catch (SQLException e) {
            if (e.getSQLState().equals("23000")) { // Duplicate key error
                System.out.println("[GameService DEBUG] Round already exists for gameId=" + lobby.gameId + ", round=" + roundNumber);
                return;
            }
            System.err.println("[GameService ERROR] Error creating round record: " + e.getMessage());
            throw new RuntimeException("Failed to create round record", e);
        }

        // Update the current round number
        currentRoundNumbers.put(gameToken, roundNumber);

        // Set up round state for each player
        Map<Integer, RoundState> perPlayerMap = roundStates.computeIfAbsent(gameToken, t -> new ConcurrentHashMap<>());
        perPlayerMap.clear();
        for (Integer pid : lobby.players) {
            perPlayerMap.put(pid, new RoundState(currentWord));
        }

        // Notify all players that the round has started.
        for (Map.Entry<String, GameCallBackService> entry : lobby.callbacks.entrySet()) {
            String sessionToken = entry.getKey();
            GameCallBackService cb = entry.getValue();
            try {
                cb.notifyRoundStart(gameToken, roundNumber, sessionToken);
                System.out.println("[GameService DEBUG] Sent notifyRoundStart for round=" + roundNumber + " to session=" + sessionToken);
            } catch (Exception e) {
                System.err.println("[GameService ERROR] Failed to notify round start for session=" + sessionToken + ": " + e.getMessage());
            }
        }

        // Schedule a timeout to end the round if time runs out.
        if (roundTimeoutTasks.containsKey(gameToken)) {
            roundTimeoutTasks.get(gameToken).cancel(true);
            roundTimeoutTasks.remove(gameToken);
        }
        Future<?> task = countdownScheduler.schedule(
                () -> handleRoundTimeout(gameToken, roundNumber),
                lobby.roundDuration,
                TimeUnit.SECONDS
        );
        roundTimeoutTasks.put(gameToken, task);
    }

    // Handles what happens when a round's time runs out.
    public synchronized void handleRoundTimeout(String gameToken, int roundNumber) {

        // Check if this is still the current round.
        Integer current = currentRoundNumbers.get(gameToken);
        if (current == null || current != roundNumber) return;
        Lobby lobby = lobbies.get(gameToken);
        if (lobby == null) return;

        // Check if the round already has a winner.
        String key = gameToken + ":" + roundNumber;
        if (roundWinners.containsKey(key)) return;

        String roundKey = gameToken + ":" + roundNumber;
        String secretWord = roundWords.getOrDefault(roundKey, "");

        // 1) Notify players that the round ended with no winner.
        for (Map.Entry<String, GameCallBackService> entry : lobby.callbacks.entrySet()) {
            String sessionToken = entry.getKey();
            GameCallBackService cb = entry.getValue();
            try {
                cb.notifyRoundEnd(gameToken, sessionToken, "", secretWord);
                System.out.println("[GameService DEBUG] Sent notifyRoundEnd (no winner) for round=" + roundNumber + ", word=" + secretWord + ", session=" + sessionToken);
            } catch (Exception e) {
                System.err.println("[GameService ERROR] Failed to notify round end for session=" + sessionToken + ": " + e.getMessage());
            }
        }

        // Update the round's end time in the database.
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE rounds SET end_time = NOW() WHERE game_id = ? AND round_number = ?")) {
            stmt.setInt(1, lobby.gameId);
            stmt.setInt(2, roundNumber);
            stmt.executeUpdate();
            System.out.println("[GameService DEBUG] Updated round end time for gameId=" + lobby.gameId + ", round=" + roundNumber);
        } catch (SQLException e) {
            System.err.println("[GameService ERROR] Error updating round end time: " + e.getMessage());
        }

        // 2) If there are more rounds, schedule the next one.
        if (roundNumber < lobby.totalRounds) {
            // Schedule the next round start
            countdownScheduler.schedule(() -> {
                try {
                    startRound(gameToken, roundNumber + 1);
                    System.out.println("[GameService DEBUG] Scheduled next round=" + (roundNumber + 1));
                } catch (GameNotFoundException e) {
                    System.err.println("[GameService ERROR] Failed to schedule next round: " + e.getMessage());
                }
            }, lobby.nextRoundDelay, TimeUnit.SECONDS);
        } else {
            // If this was the last round, end the game.
            System.out.println("[GameService DEBUG] Final round completed for game: " + gameToken);

            // Update the database with game completion details.
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false); // Start transaction

                try {
                    // Update game status to completed
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "UPDATE games SET game_status = 'completed', end_time = NOW() WHERE game_id = ?")) {
                        stmt.setInt(1, lobby.gameId);
                        int rows = stmt.executeUpdate();
                        System.out.println("[GameService DEBUG] Updated game status to completed for gameId=" + lobby.gameId + ", rows=" + rows);
                    }

                    // Determine game winner
                    String anySession = sessionToGame.entrySet().stream()
                            .filter(e -> gameToken.equals(e.getValue()))
                            .map(Map.Entry::getKey)
                            .findFirst()
                            .orElse(null);
                    Integer anyPid = lobby.players.isEmpty() ? null : lobby.players.get(0);
                    String gameWinner = anyPid != null && anySession != null ? getGameWinner(gameToken, anyPid, anySession) : "";
                    System.out.println("[GameService DEBUG] Game winner: " + (gameWinner.isEmpty() ? "none" : gameWinner));

                    if (!gameWinner.isEmpty()) {
                        // Get player_id from username
                        try (PreparedStatement stmt = conn.prepareStatement(
                                "SELECT player_id FROM players WHERE username = ?")) {
                            stmt.setString(1, gameWinner);
                            ResultSet rs = stmt.executeQuery();
                            if (rs.next()) {
                                int winnerPlayerId = rs.getInt("player_id");
                                System.out.println("[GameService DEBUG] Found playerId=" + winnerPlayerId + " for username=" + gameWinner);

                                // Update the game winner in the database
                                try (PreparedStatement stmt1 = conn.prepareStatement(
                                        "UPDATE games SET game_winner = ? WHERE game_id = ?")) {
                                    stmt1.setInt(1, winnerPlayerId);
                                    stmt1.setInt(2, lobby.gameId);
                                    int rows = stmt1.executeUpdate();
                                    System.out.println("[GameService DEBUG] Updated game winner to playerId=" + winnerPlayerId + ", rows=" + rows);
                                }

                                // Increment the winner's win count.
                                try (PreparedStatement stmt2 = conn.prepareStatement(
                                        "UPDATE players SET game_wins = COALESCE(game_wins, 0) + 1 WHERE player_id = ?")) {
                                    stmt2.setInt(1, winnerPlayerId);
                                    int rows = stmt2.executeUpdate();
                                    if (rows == 0) {
                                        System.err.println("[GameService ERROR] Failed to increment game_wins for playerId=" + winnerPlayerId + ": No rows affected");
                                    } else {
                                        System.out.println("[GameService DEBUG] Incremented game_wins for playerId=" + winnerPlayerId + ", rows=" + rows);
                                    }
                                }

                                // Get updated wins for notification
                                int wins = getPlayerWins(gameToken, winnerPlayerId, anySession);
                                System.out.println("[GameService DEBUG] Player " + gameWinner + " now has " + wins + " wins");
                            } else {
                                System.err.println("[GameService ERROR] No player found for username=" + gameWinner);
                            }
                        }
                    }

                    conn.commit(); // Save all database changes.
                    System.out.println("[GameService DEBUG] Game completion updates committed");
                } catch (SQLException | NotLoggedInException e) {
                    conn.rollback();
                    System.err.println("[GameService ERROR] Error updating game completion: " + e.getMessage());
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                System.err.println("[GameService ERROR] Database connection error: " + e.getMessage());
                e.printStackTrace();
            }

            // Schedule game end callback (after ensuring DB updates are done)
            countdownScheduler.schedule(() -> {
                String anySession = sessionToGame.entrySet().stream()
                        .filter(e -> gameToken.equals(e.getValue()))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(null);
                Integer anyPid = lobby.players.isEmpty() ? null : lobby.players.get(0);
                String finalGameWinner = "";
                try {
                    finalGameWinner = anyPid != null && anySession != null ? getGameWinner(gameToken, anyPid, anySession) : "";
                } catch (NotLoggedInException e) {
                    System.err.println("[GameService ERROR] Failed to get game winner: " + e.getMessage());
                }
                for (Map.Entry<String, GameCallBackService> entry : lobby.callbacks.entrySet()) {
                    String sessionToken = entry.getKey();
                    GameCallBackService cb = entry.getValue();
                    try {
                        cb.notifyGameEnd(gameToken, sessionToken, finalGameWinner);
                        System.out.println("[GameService DEBUG] Sent notifyGameEnd with winner=" + (finalGameWinner.isEmpty() ? "none" : finalGameWinner) + " to session=" + sessionToken);
                    } catch (Exception e) {
                        System.err.println("[GameService ERROR] Failed to notify game end for session=" + sessionToken + ": " + e.getMessage());
                    }
                }
                cleanupGame(gameToken);
            }, lobby.nextRoundDelay, TimeUnit.SECONDS);
        }
    }

    // Returns a masked version of the current word (e.g., "_ _ _") for a player.
    @Override
    public String getRandomWord(
            String gameToken,
            int roundNumber,
            int playerID,
            String sessionToken
    ) throws GameNotFoundException, NotLoggedInException {
        // Check if the game and player exist.
        Map<Integer, RoundState> perPlayer = roundStates.get(gameToken);
        if (perPlayer == null || !perPlayer.containsKey(playerID)) {
            System.err.println("[GameService ERROR] getRandomWord: Invalid game or player for gameToken=" + gameToken);
            throw new GameNotFoundException();
        }

        // Get the word for the current round.
        String roundKey = gameToken + ":" + roundNumber;
        String word = roundWords.get(roundKey);
        if (word == null) {
            System.err.println("[GameService ERROR] getRandomWord: No word found for gameToken=" + gameToken + ", round=" + roundNumber);
            throw new GameNotFoundException();
        }

        // Return the word with all letters hidden (e.g., "_ _ _").
        char[] mask = new char[word.length()];
        Arrays.fill(mask, '_');
        return new String(mask);
    }

    // Processes a player's guess of a letter and returns where it appears in the word.
    @Override
    public synchronized int[] guessLetter(
            String gameToken,
            int playerID,
            String sessionToken,
            char letter
    ) throws AlreadyGuessedLetterException,
            GameNotFoundException,
            NotLoggedInException,
            MaxAttemptsReachedException {

        // Check if the game and player exist.
        Map<Integer, RoundState> perPlayer = roundStates.get(gameToken);
        if (perPlayer == null || !perPlayer.containsKey(playerID)) {
            System.err.println("[GameService ERROR] guessLetter: Invalid game or player for gameToken=" + gameToken);
            throw new GameNotFoundException();
        }
        RoundState rs = perPlayer.get(playerID);

        // Get the current round number.
        int roundNum = currentRoundNumbers.getOrDefault(gameToken, 1);

        // 1) Check if the letter was already guessed.
        if (!rs.guessed.add(letter)) {
            System.out.println("[GameService DEBUG] guessLetter: Already guessed letter=" + letter + " by playerID=" + playerID);
            throw new AlreadyGuessedLetterException();
        }

        // 2) Find where the letter appears in the word.
        String roundKey = gameToken + ":" + roundNum;
        String word = roundWords.get(roundKey);
        if (word == null) {
            System.err.println("[GameService ERROR] guessLetter: No word found for gameToken=" + gameToken + ", round=" + roundNum);
            throw new GameNotFoundException();
        }
        List<Integer> hits = new ArrayList<>();
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == letter) hits.add(i);
        }

        // 3) If the guess was wrong, increment the wrong guess count.
        if (hits.isEmpty()) {
            rs.wrongCount++;
            StringHolder sh = new StringHolder();
            getSetting("number_of_lives", sh, sessionToken);
            int maxLives = Integer.parseInt(sh.value);

            // Check if the player has used all their lives.
            if (rs.wrongCount >= maxLives) {
                Lobby lobby = lobbies.get(gameToken);
                boolean allLost = perPlayer.values().stream()
                        .allMatch(s -> s.wrongCount >= maxLives);

                // If all players have lost, end the round.
                if (allLost) {
                    // a) Notify players the round ended with no winner.
                    for (Map.Entry<String, GameCallBackService> entry : lobby.callbacks.entrySet()) {
                        String callbackSessionToken = entry.getKey();
                        GameCallBackService cb = entry.getValue();
                        try {
                            cb.notifyRoundEnd(gameToken, callbackSessionToken, "", word);
                            System.out.println("[GameService DEBUG] Sent notifyRoundEnd (no winner), word=" + word + ", session=" + callbackSessionToken);
                        } catch (Exception e) {
                            System.err.println("[GameService ERROR] Failed to notify round end for session=" + callbackSessionToken + ": " + e.getMessage());
                        }
                    }

                    if (roundNum < lobby.totalRounds) {
                        // b) schedule next round after delay
                        countdownScheduler.schedule(() -> {
                            try {
                                startRound(gameToken, roundNum + 1);
                                System.out.println("[GameService DEBUG] Scheduled next round=" + (roundNum + 1));
                            } catch (GameNotFoundException e) {
                                System.err.println("[GameService ERROR] Failed to schedule next round: " + e.getMessage());
                            }
                        }, lobby.nextRoundDelay, TimeUnit.SECONDS);
                    } else {
                        // c) If this was the last round, end the game.
                        countdownScheduler.schedule(() -> {
                            try (Connection conn = DatabaseConnection.getConnection()) {
                                conn.setAutoCommit(false);
                                try {
                                    // Update game status to completed
                                    try (PreparedStatement stmt = conn.prepareStatement(
                                            "UPDATE games SET game_status = 'completed', end_time = NOW() WHERE game_id = ?")) {
                                        stmt.setInt(1, lobby.gameId);
                                        stmt.executeUpdate();
                                        System.out.println("[GameService DEBUG] Updated game status to completed for gameId=" + lobby.gameId);
                                    }

                                    conn.commit();
                                } catch (SQLException e) {
                                    conn.rollback();
                                    System.err.println("[GameService ERROR] Error updating game completion in all-lost case: " + e.getMessage());
                                }
                            } catch (SQLException e) {
                                System.err.println("[GameService ERROR] Database connection error in all-lost case: " + e.getMessage());
                            }

                            // Notify players the game ended with no winner.
                            for (Map.Entry<String, GameCallBackService> entry : lobby.callbacks.entrySet()) {
                                String callbackSessionToken = entry.getKey();
                                GameCallBackService cb = entry.getValue();
                                try {
                                    cb.notifyGameEnd(gameToken, callbackSessionToken, "");
                                    System.out.println("[GameService DEBUG] Sent notifyGameEnd (no winner) to session=" + callbackSessionToken);
                                } catch (Exception e) {
                                    System.err.println("[GameService ERROR] Failed to notify game end for session="  + ": " + e.getMessage());
                                }
                            }
                            cleanupGame(gameToken);
                        }, lobby.nextRoundDelay, TimeUnit.SECONDS);
                    }
                    System.out.println("[GameService DEBUG] Max attempts reached for playerID=" + playerID);
                    throw new MaxAttemptsReachedException();
                }
            }
        }

        // 4) Check if the player guessed all letters (wins the round).
        boolean allRevealed = word.chars()
                .mapToObj(c -> (char)c)
                .allMatch(rs.guessed::contains);

        if (allRevealed) {
            Lobby lobby = lobbies.get(gameToken);
            String username = lookupUsername(playerID);

            // a) Notify players the round ended with a winner.
            for (Map.Entry<String, GameCallBackService> entry : lobby.callbacks.entrySet()) {
                String callbackSessionToken = entry.getKey();
                GameCallBackService cb = entry.getValue();
                try {
                    cb.notifyRoundEnd(gameToken, callbackSessionToken, username, word);
                    System.out.println("[GameService DEBUG] Sent notifyRoundEnd with winner=" + username + ", word=" + word + ", session=" + callbackSessionToken);
                } catch (Exception e) {
                    System.err.println("[GameService ERROR] Failed to notify round end for session=" + callbackSessionToken + ": " + e.getMessage());
                }
            }
            roundWinners.put(gameToken + ":" + roundNum, username);
            updateRoundWinnerInDB(gameToken, roundNum, username);

            if (roundNum < lobby.totalRounds) {
                // b) schedule next round
                countdownScheduler.schedule(() -> {
                    try {
                        startRound(gameToken, roundNum + 1);
                        System.out.println("[GameService DEBUG] Scheduled next round=" + (roundNum + 1));
                    } catch (GameNotFoundException e) {
                        System.err.println("[GameService ERROR] Failed to schedule next round: " + e.getMessage());
                    }
                }, lobby.nextRoundDelay, TimeUnit.SECONDS);
            } else {
                // c) If this was the last round, end the game.
                countdownScheduler.schedule(() -> {
                    try (Connection conn = DatabaseConnection.getConnection()) {
                        conn.setAutoCommit(false);
                        try {
                            // Update game status to completed
                            try (PreparedStatement stmt = conn.prepareStatement(
                                    "UPDATE games SET game_status = 'completed', end_time = NOW() WHERE game_id = ?")) {
                                stmt.setInt(1, lobby.gameId);
                                stmt.executeUpdate();
                            }

                            // Update game_wins for winner
                            try (PreparedStatement stmt = conn.prepareStatement(
                                    "SELECT player_id FROM players WHERE username = ?")) {
                                stmt.setString(1, username);
                                ResultSet rs1 = stmt.executeQuery();
                                if (rs1.next()) {
                                    int winnerPlayerId = rs1.getInt("player_id");
                                    try (PreparedStatement stmt2 = conn.prepareStatement(
                                            "UPDATE players SET game_wins = COALESCE(game_wins, 0) + 1 WHERE player_id = ?")) {
                                        stmt2.setInt(1, winnerPlayerId);
                                        int rows = stmt2.executeUpdate();
                                        System.out.println("[GameService DEBUG] Incremented game_wins for playerId=" + winnerPlayerId + ", rows=" + rows);
                                    }
                                    try (PreparedStatement stmt3 = conn.prepareStatement(
                                            "UPDATE games SET game_winner = ? WHERE game_id = ?")) {
                                        stmt3.setInt(1, winnerPlayerId);
                                        stmt3.setInt(2, lobby.gameId);
                                        stmt3.executeUpdate();
                                    }
                                }
                            }

                            conn.commit();
                        } catch (SQLException e) {
                            conn.rollback();
                            System.err.println("[GameService ERROR] Error updating game completion in win case: " + e.getMessage());
                        }
                    } catch (SQLException e) {
                        System.err.println("[GameService ERROR] Database connection error in win case: " + e.getMessage());
                    }

                    // Notify players the game ended with a winner.
                    for (Map.Entry<String, GameCallBackService> entry : lobby.callbacks.entrySet()) {
                        String callbackSessionToken = entry.getKey();
                        GameCallBackService cb = entry.getValue();
                        try {
                            cb.notifyGameEnd(gameToken, callbackSessionToken, username);
                            System.out.println("[GameService DEBUG] Sent notifyGameEnd with winner=" + username + " to session=" + callbackSessionToken);
                        } catch (Exception e) {
                            System.err.println("[GameService ERROR] Failed to notify game end for session=" + callbackSessionToken + ": " + e.getMessage());
                        }
                    }
                    cleanupGame(gameToken);
                }, lobby.nextRoundDelay, TimeUnit.SECONDS);
            }

            // d) Return the positions of the guessed letter.
            System.out.println("[GameService DEBUG] PlayerID=" + playerID + " won round with hits=" + hits);
            return hits.stream().mapToInt(Integer::intValue).toArray();
        }

        // 5) Return the positions of the guessed letter (normal case).
        System.out.println("[GameService DEBUG] guessLetter: playerID=" + playerID + ", letter=" + letter + ", hits=" + hits);
        return hits.stream().mapToInt(Integer::intValue).toArray();
    }

    // Cleans up all data related to a game after it ends.
    private void cleanupGame(String gameToken) {
        // Remove the lobby and all associated data.
        lobbies.remove(gameToken);
        sessionToGame.values().removeIf(t -> t.equals(gameToken));
        sessionToCallback.keySet().removeIf(st -> !sessionToGame.containsKey(st));
        sessionToWaitingCallback.keySet().removeIf(st -> !sessionToGame.containsKey(st));
        roundStates.remove(gameToken);
        roundWords.keySet().removeIf(k -> k.startsWith(gameToken + ":"));
        usedWordsPerGame.remove(gameToken); // Clear used words
        currentRoundNumbers.remove(gameToken);
        roundWinners.keySet().removeIf(k -> k.startsWith(gameToken + ":"));
        System.out.println("[GameService DEBUG] Cleaned up game: " + gameToken);
    }

    // Looks up a player's username based on their ID.
    private String lookupUsername(int playerID) {
        // Query the database for the player's username.
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT username FROM players WHERE player_id = ?"
             )) {
            stmt.setInt(1, playerID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String username = rs.getString("username");
                System.out.println("[GameService DEBUG] Looked up username=" + username + " for playerID=" + playerID);
                return username;
            }
        } catch (SQLException e) {
            System.err.println("[GameService ERROR] lookupUsername failed: " + e.getMessage());
        }
        // If not found, return a default name.
        System.out.println("[GameService DEBUG] Default username for playerID=" + playerID);
        return "Player" + playerID;
    }

    // Returns the winner of a specific round.
    @Override
    public String getRoundWinner(String gameToken,
                                 int playerID,
                                 String sessionToken)
            throws NotLoggedInException {
        // Get the current round's winner, if any.
        int roundNum = currentRoundNumbers.getOrDefault(gameToken, 1);
        String key   = gameToken + ":" + roundNum;
        String winner = roundWinners.getOrDefault(key, "Unknown");
        System.out.println("[GameService DEBUG] getRoundWinner: round=" + roundNum + ", winner=" + winner);
        return winner;
    }

    // Updates the database with the winner of a round.
    private void updateRoundWinnerInDB(String gameToken, int roundNumber, String winnerUsername) {
        System.out.println("[GameService DEBUG] Updating round winner: " + winnerUsername
                + " for game: " + gameToken + " round: " + roundNumber);

        // Get the winner's player ID and update the round in the database.
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get player_id from username
            int playerId;
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT player_id FROM players WHERE username = ?")) {
                stmt.setString(1, winnerUsername);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) {
                    System.err.println("[GameService ERROR] Player not found: " + winnerUsername);
                    return;
                }
                playerId = rs.getInt("player_id");
                System.out.println("[GameService DEBUG] Found player ID: " + playerId);
            }

            Lobby lobby = lobbies.get(gameToken);
            if (lobby == null) {
                System.err.println("[GameService ERROR] Lobby not found for game token: " + gameToken);
                return;
            }

            // Update round winner and end time
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE rounds SET round_winner = ?, end_time = NOW() WHERE game_id = ? AND round_number = ?")) {
                stmt.setInt(1, playerId);
                stmt.setInt(2, lobby.gameId);
                stmt.setInt(3, roundNumber);
                int updated = stmt.executeUpdate();
                System.out.println("[GameService DEBUG] Round winner updated, rows affected: " + updated);
            }
        } catch (SQLException e) {
            System.err.println("[GameService ERROR] Error updating round winner: " + e.getMessage());
        }
    }

    // Determines the overall winner of a game based on round wins.
    @Override
    public String getGameWinner(String gameToken, int playerID, String sessionToken)
            throws NotLoggedInException {
        // Check if the player is logged in.
        if (sessionToken == null || !sessionToGame.containsKey(sessionToken)) {
            System.err.println("[GameService ERROR] getGameWinner: Invalid session token for playerID=" + playerID);
            throw new NotLoggedInException();
        }

        Lobby lobby = lobbies.get(gameToken);
        if (lobby == null) {
            System.out.println("[GameService DEBUG] getGameWinner: No lobby found for gameToken=" + gameToken);
            return "";
        }

        // Count how many rounds each player won.
        Map<String, Integer> winCounts = new HashMap<>();
        for (Map.Entry<String, String> entry : roundWinners.entrySet()) {
            if (entry.getKey().startsWith(gameToken + ":")) {
                String winner = entry.getValue();
                if (winner != null && !winner.isEmpty()) {
                    winCounts.put(winner, winCounts.getOrDefault(winner, 0) + 1);
                    System.out.println("[GameService DEBUG] Round winner found: " + winner + ", count=" + winCounts.get(winner));
                }
            }
        }

        // Find the player with the most round wins.
        String winner = winCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        // If no one won any rounds or there's a tie, return no winner.
        if (winner == null || winCounts.isEmpty()) {
            System.out.println("[GameService DEBUG] No rounds won, no game winner.");
            return "";
        }

        System.out.println("[GameService DEBUG] Determined game winner: " + winner + " with " + winCounts.get(winner) + " wins");
        return winner;
    }

    // Returns the number of games a player has won.
    @Override
    public int getPlayerWins(String gameToken, int playerID, String sessionToken)
            throws NotLoggedInException {
        // Check if the player is logged in.
        if (sessionToken == null || !sessionToGame.containsKey(sessionToken)) {
            System.err.println("[GameService ERROR] getPlayerWins: Invalid session token for playerID=" + playerID);
            throw new NotLoggedInException();
        }

        // Query the database for the player's win count.
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COALESCE(game_wins, 0) AS game_wins FROM players WHERE player_id = ?")) {
            stmt.setInt(1, playerID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int wins = rs.getInt("game_wins");
                System.out.println("[GameService DEBUG] Fetched game_wins=" + wins + " for playerID=" + playerID);
                return wins;
            }
        } catch (SQLException e) {
            System.err.println("[GameService ERROR] Error fetching player wins: " + e.getMessage());
        }
        // If not found, return 0 wins.
        System.out.println("[GameService DEBUG] Default game_wins=0 for playerID=" + playerID);
        return 0;
    }

    // Returns a player's display name
    @Override
    public String getDisplayName(int playerID, String sessionToken)
            throws NotLoggedInException {
        System.out.println("[GameService DEBUG] getDisplayName called for playerID=" + playerID);
        return "";
    }

    // Returns the top 10 players with the most game wins (leaderboard).
    @Override
    public String[] getLeaderboards(int playerID, String sessionToken)
            throws NotLoggedInException {
        // Check if the player is logged in.
        if (sessionToken == null) {
            System.err.println("[GameService ERROR] getLeaderboards: Null session token for playerID=" + playerID);
            throw new NotLoggedInException();
        }

        // Validate playerID
        if (!isValidPlayer(playerID)) {
            System.err.println("[GameService ERROR] getLeaderboards: Invalid playerID=" + playerID);
            throw new NotLoggedInException();
        }

        // Add logging to trace the caller
        System.out.println("[GameService TRACE] getLeaderboards called for playerID=" + playerID +
                ", sessionToken=" + sessionToken +
                ", thread=" + Thread.currentThread().getId() +
                ", timestamp=" + System.currentTimeMillis());

        List<String> leaderboardEntries = new ArrayList<>();
        List<Map<String, Object>> tableData = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction for consistency
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT username, COALESCE(game_wins, 0) AS game_wins FROM players ORDER BY game_wins DESC LIMIT 10")) {
                ResultSet rs = stmt.executeQuery();
                int rank = 1;
                while (rs.next()) {
                    String username = rs.getString("username");
                    int gameWins = rs.getInt("game_wins");
                    String entry = username + ":" + gameWins;
                    leaderboardEntries.add(entry);

                    // Create map for each leaderboard entry
                    Map<String, Object> row = new HashMap<>();
                    row.put("rank", rank);
                    row.put("username", username);
                    row.put("points", gameWins);
                    tableData.add(row);

                    rank++;
                }
                conn.commit(); // Save the transaction.
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("[GameService ERROR] Error fetching leaderboard: " + e.getMessage());
                e.printStackTrace();
                return new String[0];
            }
        } catch (SQLException e) {
            System.err.println("[GameService ERROR] Database connection error: " + e.getMessage());
            return new String[0];
        }

        // Print the leaderboard table
        printLeaderboardTable(tableData);

        System.out.println("[GameService DEBUG] Fetched leaderboard with " + leaderboardEntries.size() + " entries");
        return leaderboardEntries.toArray(new String[0]);
    }

    // Prints the leaderboard as a formatted table for debugging.
    private void printLeaderboardTable(List<Map<String, Object>> leaderboard) {
        // Define minimum column widths
        int rankWidth = 6; // "Rank" + padding
        int usernameWidth = 15; // Minimum width for usernames
        int pointsWidth = 8; // "Points" + padding

        // Calculate dynamic username width based on longest username
        for (Map<String, Object> entry : leaderboard) {
            String username = (String) entry.getOrDefault("username", "");
            usernameWidth = Math.max(usernameWidth, username.length());
        }

        // Print table header
        String header = String.format("[GameService DEBUG] Leaderboard:%n" +
                        "+-%s-+-%s-+-%s-+%n" +
                        "| %-" + rankWidth + "s | %-" + usernameWidth + "s | %-" + pointsWidth + "s |%n" +
                        "+-%s-+-%s-+-%s-+%n",
                String.join("", Collections.nCopies(rankWidth, "-")),
                String.join("", Collections.nCopies(usernameWidth, "-")),
                String.join("", Collections.nCopies(pointsWidth, "-")),
                "Rank", "Username", "Points",
                String.join("", Collections.nCopies(rankWidth, "-")),
                String.join("", Collections.nCopies(usernameWidth, "-")),
                String.join("", Collections.nCopies(pointsWidth, "-")));

        System.out.print(header);

        // Print table rows (handle empty leaderboard)
        if (leaderboard.isEmpty()) {
            System.out.printf("| %-" + rankWidth + "s | %-" + usernameWidth + "s | %-" + pointsWidth + "s |%n",
                    "-", "(No entries)", "-");
        } else {
            for (Map<String, Object> entry : leaderboard) {
                int rank = ((Number) entry.getOrDefault("rank", 0)).intValue();
                String username = (String) entry.getOrDefault("username", "");
                int points = ((Number) entry.getOrDefault("points", 0)).intValue();
                System.out.printf("| %-" + rankWidth + "d | %-" + usernameWidth + "s | %-" + pointsWidth + "d |%n",
                        rank, username, points);
            }
        }

        // Print table footer
        System.out.printf("+-%" + rankWidth + "s-+-%-" + usernameWidth + "s-+-%-" + pointsWidth + "s-+%n",
                String.join("", Collections.nCopies(rankWidth, "-")),
                String.join("", Collections.nCopies(usernameWidth, "-")),
                String.join("", Collections.nCopies(pointsWidth, "-")));
    }

    // Checks if a player ID is valid by looking it up in the database.
    private boolean isValidPlayer(int playerID) {
        // Query the database to see if the player exists.
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM players WHERE player_id = ?")) {
            stmt.setInt(1, playerID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("[GameService ERROR] Error validating playerID=" + playerID + ": " + e.getMessage());
        }
        return false;
    }

    // Fetches a game setting (like round duration) from the database.
    @Override
    public void getSetting(String key, StringHolder value, String sessionToken) {
        // Query the database for the setting's value.
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT " + key + " FROM settings WHERE id=1";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                value.value = rs.getString(1);
                System.out.println("[GameService DEBUG] Fetched setting: " + key + "=" + value.value);
            } else {
                value.value = "";
                System.out.println("[GameService DEBUG] No value found for setting: " + key);
            }
        } catch (SQLException e) {
            value.value = "";
            System.err.println("[GameService ERROR] Error fetching setting '" + key + "': " + e.getMessage());
        }
    }

    // Prints a list of all active lobbies for debugging.
    private void debugPrintAllLobbies(String context) {
        System.out.println(">>> [GameService DEBUG][" + context + "] Active lobbies:");
        if (lobbies.isEmpty()) {
            System.out.println("    (none)");
        } else {
            for (Lobby l : lobbies.values()) {
                System.out.println("    - " + l.token
                        + ": players=" + l.players
                        + " waitingCallbacks=" + l.waitingCallbacks.size()
                        + " gameCallbacks=" + l.callbacks.size());
            }
        }
    }
}