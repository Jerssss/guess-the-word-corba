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
    // Track round wins per player (username → win count)
    private static final Map<String, Map<String, Integer>> gameWinCounts = new ConcurrentHashMap<>();

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
        final int lobbyWaitingTime;
        final int roundDuration;
        final int nextRoundDelay;
        final int countdownSeconds;
        final int minimumPlayers;
        final int numberOfLives;

        final List<Integer> players = new CopyOnWriteArrayList<>();
        final Map<String, GameCallBackService> callbacks = new ConcurrentHashMap<>();
        final List<WaitingRoomGameCallbackService> waitingCallbacks = new CopyOnWriteArrayList<>();

        Lobby(
                String token,
                int gameId,
                int lobbyWaitingTime,
                int roundDuration,
                int nextRoundDelay,
                int countdownSeconds,
                int minimumPlayers,
                int numberOfLives
        ) {
            this.token = token;
            this.gameId = gameId;
            this.lobbyWaitingTime = lobbyWaitingTime;
            this.roundDuration = roundDuration;
            this.nextRoundDelay = nextRoundDelay;
            this.countdownSeconds = countdownSeconds;
            this.minimumPlayers = minimumPlayers;
            this.numberOfLives = numberOfLives;
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

    @Override
    public synchronized String joinLobby(int playerID, String sessionToken)
            throws NotLoggedInException {
        if (sessionToken == null) {
            System.err.println("[GameService ERROR] joinLobby: Null session token for playerID=" + playerID);
            throw new NotLoggedInException();
        }

        if (sessionToGame.containsKey(sessionToken)) {
            System.out.println("[GameService DEBUG] joinLobby: playerID=" + playerID + " already in lobby=" + sessionToGame.get(sessionToken));
            return sessionToGame.get(sessionToken);
        }

        Lobby target = null;
        for (Lobby l : lobbies.values()) {
            if (l.players.size() < l.minimumPlayers) {
                target = l;
                break;
            }
        }
        if (target == null) {
            Function<String, Integer> fetchInt = key -> {
                StringHolder sh = new StringHolder();
                getSetting(key, sh, sessionToken);
                return Integer.parseInt(sh.value);
            };
            String newToken = UUID.randomUUID().toString();

            int gameId = -1;
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO games (game_status, start_time) VALUES ('waiting', NOW())",
                         PreparedStatement.RETURN_GENERATED_KEYS)) {
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

            target = new Lobby(
                    newToken,
                    gameId,
                    fetchInt.apply("lobby_waiting_time"),
                    fetchInt.apply("round_duration"),
                    fetchInt.apply("next_round_delay"),
                    fetchInt.apply("countdown_to_game_start"),
                    fetchInt.apply("minimum_players"),
                    fetchInt.apply("number_of_lives")
            );

            lobbies.put(newToken, target);
            gameWinCounts.put(newToken, new ConcurrentHashMap<>()); // Initialize win counts for new game
            System.out.println("[GameService DEBUG] Created lobby=" + newToken +
                    " with gameId=" + gameId);
        }

        if (!target.players.contains(playerID)) {
            target.players.add(playerID);
            sessionToGame.put(sessionToken, target.token);
            System.out.println("[GameService DEBUG] joinLobby: playerID=" + playerID +
                    " joined lobby=" + target.token +
                    " (count=" + target.players.size() + ")");
        }

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

        if (target.players.size() >= target.minimumPlayers) {
            ScheduledFuture<?> oldTask = pendingCountdowns.remove(target.token);
            if (oldTask != null) oldTask.cancel(false);

            Lobby lobbyRef = target;
            ScheduledFuture<?> newTask = countdownScheduler.schedule(() -> {
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

                countdownScheduler.schedule(() -> {
                    for (GameCallBackService gcb : lobbyRef.callbacks.values()) {
                        try {
                            gcb.notifyGameStart(lobbyRef.token, sessionToken);
                        } catch (Exception e) {
                            System.err.println("[GameService ERROR] Failed to notify game start: " + e.getMessage());
                        }
                    }
                    System.out.println("[GameService DEBUG] notifyGameStart sent for lobby=" + lobbyRef.token);
                }, lobbyRef.countdownSeconds, TimeUnit.SECONDS);
            }, 100, TimeUnit.MILLISECONDS);

            pendingCountdowns.put(target.token, newTask);
        }

        debugPrintAllLobbies("joinLobby");
        return target.token;
    }

    @Override
    public synchronized void leaveLobby(int playerID, String gameToken, String sessionToken)
            throws NotLoggedInException {
        if (sessionToken == null || !sessionToGame.containsKey(sessionToken)) {
            System.err.println("[GameService ERROR] leaveLobby: Invalid session token for playerID=" + playerID);
            throw new NotLoggedInException();
        }

        Lobby lobby = lobbies.get(gameToken);
        if (lobby != null) {
            lobby.players.removeIf(pid -> pid == playerID);
            WaitingRoomGameCallbackService wcb = sessionToWaitingCallback.remove(sessionToken);
            if (wcb != null) lobby.waitingCallbacks.remove(wcb);

            for (WaitingRoomGameCallbackService cb : lobby.waitingCallbacks) {
                try {
                    cb.notifyPlayerJoined(gameToken, lobby.players.size(), sessionToken);
                    System.out.println("[GameService DEBUG] Notified waiting callback for session=" + sessionToken +
                            ", player count=" + lobby.players.size());
                } catch (Exception e) {
                    System.err.println("[GameService ERROR] Failed to notify waiting callback: " + e.getMessage());
                }
            }

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

            if (lobby.players.isEmpty()) {
                lobbies.remove(gameToken);
                gameWinCounts.remove(gameToken);
                System.out.println("[GameService DEBUG] Lobby " + gameToken + " removed (empty)");
            }
        }

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
        ) {
            System.err.println("[GameService ERROR] registerWaitingRoomCallback: Invalid session or game token for playerID=" + playerID);
            throw new NotLoggedInException();
        }

        Lobby lobby = lobbies.get(gameToken);
        if (lobby == null) {
            System.err.println("[GameService ERROR] registerWaitingRoomCallback: Lobby not found for gameToken=" + gameToken);
            throw new NotLoggedInException();
        }

        lobby.waitingCallbacks.add(cb);
        sessionToWaitingCallback.put(sessionToken, cb);
        System.out.println("[GameService DEBUG] registerWaitingRoomCallback: playerID=" + playerID +
                " in lobby=" + gameToken +
                " (waitingCallbacks=" + lobby.waitingCallbacks.size() + ")");

        try {
            cb.notifyPlayerJoined(gameToken, lobby.players.size(), sessionToken);
            System.out.println("[GameService DEBUG] Initial player count notification sent to session=" + sessionToken +
                    ", count=" + lobby.players.size());
        } catch (Exception e) {
            System.err.println("[GameService ERROR] Failed to send initial player count notification: " + e.getMessage());
        }
    }

    @Override
    public String getLobbyStatus(String sessionToken)
            throws NotLoggedInException, GameTimeOutException, NotEnoughPlayersException {
        return "";
    }

    @Override
    public int getNumberOfPlayersJoined(int playerID, String sessionToken)
            throws NotLoggedInException {
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

    @Override
    public synchronized int startGame(int playerID, String sessionToken)
            throws NotEnoughPlayersException {
        String token = sessionToGame.get(sessionToken);
        Lobby lobby = (token == null ? null : lobbies.get(token));
        if (lobby == null || lobby.players.size() < DEFAULT_MIN_PLAYERS) {
            System.err.println("[GameService ERROR] startGame: Not enough players for session=" + sessionToken);
            throw new NotEnoughPlayersException();
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE games SET game_status = 'in_progress' WHERE game_id = ?")) {
            stmt.setInt(1, lobby.gameId);
            stmt.executeUpdate();
            System.out.println("[GameService DEBUG] Updated game status to in_progress for gameId=" + lobby.gameId);
        } catch (SQLException e) {
            System.err.println("[GameService ERROR] Error updating game status: " + e.getMessage());
        }

        for (GameCallBackService cb : lobby.callbacks.values()) {
            try {
                cb.notifyGameStart(token, sessionToken);
                System.out.println("[GameService DEBUG] Sent notifyGameStart to session=" + sessionToken);
            } catch (Exception e) {
                System.err.println("[GameService ERROR] Failed to notify game start: " + e.getMessage());
            }
        }
        System.out.println("[GameService DEBUG] notifyGameStart sent for lobby=" + token);

        try {
            startRound(token, 1, playerID, sessionToken);
            System.out.println("[GameService DEBUG] Started round 1 for lobby=" + token);
        } catch (Exception e) {
            System.err.println("[GameService ERROR] Failed to start round 1: " + e.getMessage());
            e.printStackTrace();
        }

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
        if (lobby == null) {
            System.err.println("[GameService ERROR] registerCallBack: Lobby not found for gameToken=" + gameToken);
            throw new NotLoggedInException();
        }
        lobby.callbacks.put(sessionToken, cb);
        sessionToCallback.put(sessionToken, cb);
        System.out.println("[GameService DEBUG] registerCallBack: playerID=" + playerID +
                ", lobby=" + gameToken +
                " (gameCallbacks=" + lobby.callbacks.size() + ")");
    }

    @Override
    public synchronized int startRound(
            String gameToken,
            int roundNumber,
            int playerID,
            String sessionToken
    ) throws GameNotFoundException, NotLoggedInException {
        System.out.println("[GameService DEBUG] startRound called for gameToken=" + gameToken + ", round=" + roundNumber + ", playerID=" + playerID + ", sessionToken=" + sessionToken + ", thread=" + Thread.currentThread().getId());

        Lobby lobby = lobbies.get(gameToken);
        if (lobby == null) {
            System.err.println("[GameService ERROR] startRound: Lobby not found for gameToken=" + gameToken);
            throw new GameNotFoundException();
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM rounds WHERE game_id = ? AND round_number = ?")) {
            stmt.setInt(1, lobby.gameId);
            stmt.setInt(2, roundNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("[GameService DEBUG] Round already exists for gameId=" + lobby.gameId + ", round=" + roundNumber);
                return lobby.players.size();
            }
        } catch (SQLException e) {
            System.err.println("[GameService ERROR] Error checking existing round: " + e.getMessage());
        }

        String roundKey = gameToken + ":" + roundNumber;
        Set<String> usedWords = usedWordsPerGame.computeIfAbsent(gameToken, k -> new HashSet<>());
        String currentWord;
        do {
            currentWord = WORDS.get(RAND.nextInt(WORDS.size())).toUpperCase();
        } while (usedWords.contains(currentWord));
        usedWords.add(currentWord);
        roundWords.put(roundKey, currentWord);
        System.out.println("[GameService DEBUG] Assigned new word for gameToken=" + gameToken + ", round=" + roundNumber + ": " + currentWord);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO rounds (game_id, round_number, word, start_time) VALUES (?, ?, ?, NOW())")) {
            stmt.setInt(1, lobby.gameId);
            stmt.setInt(2, roundNumber);
            stmt.setString(3, currentWord);
            stmt.executeUpdate();
            System.out.println("[GameService DEBUG] Created round record for gameId=" + lobby.gameId + ", round=" + roundNumber + ", word=" + currentWord);
        } catch (SQLException e) {
            if (e.getSQLState().equals("23000")) {
                System.out.println("[GameService DEBUG] Round already exists for gameId=" + lobby.gameId + ", round=" + roundNumber);
                return lobby.players.size();
            }
            System.err.println("[GameService ERROR] Error creating round record: " + e.getMessage());
            throw new RuntimeException("Failed to create round record", e);
        }

        currentRoundNumbers.put(gameToken, roundNumber);

        Map<Integer, RoundState> perPlayerMap =
                roundStates.computeIfAbsent(gameToken, t -> new ConcurrentHashMap<>());
        perPlayerMap.clear();
        for (Integer pid : lobby.players) {
            perPlayerMap.put(pid, new RoundState(currentWord));
        }

        for (GameCallBackService cb : lobby.callbacks.values()) {
            try {
                cb.notifyRoundStart(gameToken, roundNumber, sessionToken);
                System.out.println("[GameService DEBUG] Sent notifyRoundStart for round=" + roundNumber);
            } catch (Exception e) {
                System.err.println("[GameService ERROR] Failed to notify round start: " + e.getMessage());
            }
        }

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

        return lobby.players.size();
    }

    public synchronized void handleRoundTimeout(String gameToken, int roundNumber) {
        Integer current = currentRoundNumbers.get(gameToken);
        if (current == null || current != roundNumber) return;
        Lobby lobby = lobbies.get(gameToken);
        if (lobby == null) return;

        String key = gameToken + ":" + roundNumber;
        if (roundWinners.containsKey(key)) return;

        String roundKey = gameToken + ":" + roundNumber;
        String secretWord = roundWords.getOrDefault(roundKey, "");

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

        scheduleNextRound(gameToken, roundNumber, lobby);
    }

    private void scheduleNextRound(String gameToken, int roundNumber, Lobby lobby) {
        countdownScheduler.schedule(() -> {
            Integer anyPid = lobby.players.isEmpty() ? null : lobby.players.get(0);
            String anySession = sessionToGame.entrySet().stream()
                    .filter(e -> gameToken.equals(e.getValue()))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);
            if (anyPid != null && anySession != null) {
                try {
                    startRound(gameToken, roundNumber + 1, anyPid, anySession);
                    System.out.println("[GameService DEBUG] Scheduled next round=" + (roundNumber + 1));
                } catch (Exception e) {
                    System.err.println("[GameService ERROR] Failed to schedule next round: " + e.getMessage());
                }
            }
        }, lobby.nextRoundDelay, TimeUnit.SECONDS);
    }

    @Override
    public String getRandomWord(
            String gameToken,
            int roundNumber,
            int playerID,
            String sessionToken
    ) throws GameNotFoundException, NotLoggedInException {
        Map<Integer, RoundState> perPlayer = roundStates.get(gameToken);
        if (perPlayer == null || !perPlayer.containsKey(playerID)) {
            System.err.println("[GameService ERROR] getRandomWord: Invalid game or player for gameToken=" + gameToken);
            throw new GameNotFoundException();
        }

        String roundKey = gameToken + ":" + roundNumber;
        String word = roundWords.get(roundKey);
        if (word == null) {
            System.err.println("[GameService ERROR] getRandomWord: No word found for gameToken=" + gameToken + ", round=" + roundNumber);
            throw new GameNotFoundException();
        }

        char[] mask = new char[word.length()];
        Arrays.fill(mask, '_');
        return new String(mask);
    }

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
        Map<Integer, RoundState> perPlayer = roundStates.get(gameToken);
        if (perPlayer == null || !perPlayer.containsKey(playerID)) {
            System.err.println("[GameService ERROR] guessLetter: Invalid game or player for gameToken=" + gameToken);
            throw new GameNotFoundException();
        }
        RoundState rs = perPlayer.get(playerID);

        int roundNum = currentRoundNumbers.getOrDefault(gameToken, 1);

        if (!rs.guessed.add(letter)) {
            System.out.println("[GameService DEBUG] guessLetter: Already guessed letter=" + letter + " by playerID=" + playerID);
            throw new AlreadyGuessedLetterException();
        }

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

        if (hits.isEmpty()) {
            rs.wrongCount++;
            StringHolder sh = new StringHolder();
            getSetting("number_of_lives", sh, sessionToken);
            int maxLives = Integer.parseInt(sh.value);

            if (rs.wrongCount >= maxLives) {
                Lobby lobby = lobbies.get(gameToken);
                boolean allLost = perPlayer.values().stream()
                        .allMatch(s -> s.wrongCount >= maxLives);

                if (allLost) {
                    lobby.callbacks.values().forEach(cb -> {
                        try {
                            cb.notifyRoundEnd(gameToken, sessionToken, "", word);
                            System.out.println("[GameService DEBUG] Sent notifyRoundEnd (no winner), word=" + word + ", session=" + sessionToken);
                        } catch (Exception e) {
                            System.err.println("[GameService ERROR] Failed to notify round end: " + e.getMessage());
                        }
                    });

                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement stmt = conn.prepareStatement(
                                 "UPDATE rounds SET end_time = NOW() WHERE game_id = ? AND round_number = ?")) {
                        stmt.setInt(1, lobby.gameId);
                        stmt.setInt(2, roundNum);
                        stmt.executeUpdate();
                        System.out.println("[GameService DEBUG] Updated round end time for gameId=" + lobby.gameId + ", round=" + roundNum);
                    } catch (SQLException e) {
                        System.err.println("[GameService ERROR] Error updating round end time: " + e.getMessage());
                    }

                    scheduleNextRound(gameToken, roundNum, lobby);
                    System.out.println("[GameService DEBUG] Max attempts reached for playerID=" + playerID);
                    throw new MaxAttemptsReachedException();
                }
            }
        }

        boolean allRevealed = word.chars()
                .mapToObj(c -> (char) c)
                .allMatch(rs.guessed::contains);

        if (allRevealed) {
            Lobby lobby = lobbies.get(gameToken);
            String username = lookupUsername(playerID);

            // Update win counts
            Map<String, Integer> winCounts = gameWinCounts.computeIfAbsent(gameToken, k -> new ConcurrentHashMap<>());
            winCounts.put(username, winCounts.getOrDefault(username, 0) + 1);
            System.out.println("[GameService DEBUG] Updated win count for " + username + ": " + winCounts.get(username));

            lobby.callbacks.values().forEach(cb -> {
                try {
                    cb.notifyRoundEnd(gameToken, sessionToken, username, word);
                    System.out.println("[GameService DEBUG] Sent notifyRoundEnd with winner=" + username + ", word=" + word + ", session=" + sessionToken);
                } catch (Exception e) {
                    System.err.println("[GameService ERROR] Failed to notify round end: " + e.getMessage());
                }
            });
            roundWinners.put(gameToken + ":" + roundNum, username);
            updateRoundWinnerInDB(gameToken, roundNum, username);

            // Check if player has won 3 rounds
            if (winCounts.getOrDefault(username, 0) >= 3) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    conn.setAutoCommit(false);
                    try {
                        try (PreparedStatement stmt = conn.prepareStatement(
                                "UPDATE games SET game_status = 'completed', end_time = NOW() WHERE game_id = ?")) {
                            stmt.setInt(1, lobby.gameId);
                            stmt.executeUpdate();
                        }

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

                countdownScheduler.schedule(() -> {
                    lobby.callbacks.values().forEach(cb -> {
                        try {
                            cb.notifyGameEnd(gameToken, sessionToken, username);
                            System.out.println("[GameService DEBUG] Sent notifyGameEnd with winner=" + username);
                        } catch (Exception e) {
                            System.err.println("[GameService ERROR] Failed to notify game end: " + e.getMessage());
                        }
                    });
                    cleanupGame(gameToken);
                }, lobby.nextRoundDelay, TimeUnit.SECONDS);
            } else {
                scheduleNextRound(gameToken, roundNum, lobby);
            }

            System.out.println("[GameService DEBUG] PlayerID=" + playerID + " won round with hits=" + hits);
            return hits.stream().mapToInt(Integer::intValue).toArray();
        }

        System.out.println("[GameService DEBUG] guessLetter: playerID=" + playerID + ", letter=" + letter + ", hits=" + hits);
        return hits.stream().mapToInt(Integer::intValue).toArray();
    }

    private void cleanupGame(String gameToken) {
        lobbies.remove(gameToken);
        sessionToGame.values().removeIf(t -> t.equals(gameToken));
        sessionToCallback.keySet().removeIf(st -> !sessionToGame.containsKey(st));
        sessionToWaitingCallback.keySet().removeIf(st -> !sessionToGame.containsKey(st));
        roundStates.remove(gameToken);
        roundWords.keySet().removeIf(k -> k.startsWith(gameToken + ":"));
        usedWordsPerGame.remove(gameToken);
        gameWinCounts.remove(gameToken);
        currentRoundNumbers.remove(gameToken);
        roundWinners.keySet().removeIf(k -> k.startsWith(gameToken + ":"));
        System.out.println("[GameService DEBUG] Cleaned up game: " + gameToken);
    }

    private String lookupUsername(int playerID) {
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
        System.out.println("[GameService DEBUG] Default username for playerID=" + playerID);
        return "Player" + playerID;
    }

    @Override
    public String getRoundWinner(String gameToken,
                                 int playerID,
                                 String sessionToken)
            throws NotLoggedInException {
        int roundNum = currentRoundNumbers.getOrDefault(gameToken, 1);
        String key = gameToken + ":" + roundNum;
        String winner = roundWinners.getOrDefault(key, "Unknown");
        System.out.println("[GameService DEBUG] getRoundWinner: round=" + roundNum + ", winner=" + winner);
        return winner;
    }

    private void updateRoundWinnerInDB(String gameToken, int roundNumber, String winnerUsername) {
        System.out.println("[GameService DEBUG] Updating round winner: " + winnerUsername
                + " for game: " + gameToken + " round: " + roundNumber);

        try (Connection conn = DatabaseConnection.getConnection()) {
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

    @Override
    public String getGameWinner(String gameToken, int playerID, String sessionToken)
            throws NotLoggedInException {
        if (sessionToken == null || !sessionToGame.containsKey(sessionToken)) {
            System.err.println("[GameService ERROR] getGameWinner: Invalid session token for playerID=" + playerID);
            throw new NotLoggedInException();
        }

        Map<String, Integer> winCounts = gameWinCounts.get(gameToken);
        if (winCounts == null || winCounts.isEmpty()) {
            System.out.println("[GameService DEBUG] No rounds won, no game winner.");
            return "";
        }

        String winner = winCounts.entrySet().stream()
                .filter(entry -> entry.getValue() >= 3)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("");

        System.out.println("[GameService DEBUG] Determined game winner: " + (winner.isEmpty() ? "none" : winner));
        return winner;
    }

    @Override
    public int getPlayerWins(String gameToken, int playerID, String sessionToken)
            throws NotLoggedInException {
        if (sessionToken == null || !sessionToGame.containsKey(sessionToken)) {
            System.err.println("[GameService ERROR] getPlayerWins: Invalid session token for playerID=" + playerID);
            throw new NotLoggedInException();
        }

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
        System.out.println("[GameService DEBUG] Default game_wins=0 for playerID=" + playerID);
        return 0;
    }

    @Override
    public String getDisplayName(int playerID, String sessionToken)
            throws NotLoggedInException {
        System.out.println("[GameService DEBUG] getDisplayName called for playerID=" + playerID);
        return "";
    }

    @Override
    public String[] getLeaderboards(int playerID, String sessionToken)
            throws NotLoggedInException {
        if (sessionToken == null) {
            System.err.println("[GameService ERROR] getLeaderboards: Null session token for playerID=" + playerID);
            throw new NotLoggedInException();
        }

        if (!isValidPlayer(playerID)) {
            System.err.println("[GameService ERROR] getLeaderboards: Invalid playerID=" + playerID);
            throw new NotLoggedInException();
        }

        System.out.println("[GameService TRACE] getLeaderboards called for playerID=" + playerID +
                ", sessionToken=" + sessionToken +
                ", thread=" + Thread.currentThread().getId() +
                ", timestamp=" + System.currentTimeMillis());

        List<String> leaderboardEntries = new ArrayList<>();
        List<Map<String, Object>> tableData = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT username, COALESCE(game_wins, 0) AS game_wins FROM players ORDER BY game_wins DESC LIMIT 10")) {
                ResultSet rs = stmt.executeQuery();
                int rank = 1;
                while (rs.next()) {
                    String username = rs.getString("username");
                    int gameWins = rs.getInt("game_wins");
                    String entry = username + ":" + gameWins;
                    leaderboardEntries.add(entry);

                    Map<String, Object> row = new HashMap<>();
                    row.put("rank", rank);
                    row.put("username", username);
                    row.put("points", gameWins);
                    tableData.add(row);

                    rank++;
                }
                conn.commit();
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

        printLeaderboardTable(tableData);

        System.out.println("[GameService DEBUG] Fetched leaderboard with " + leaderboardEntries.size() + " entries");
        return leaderboardEntries.toArray(new String[0]);
    }

    private void printLeaderboardTable(List<Map<String, Object>> leaderboard) {
        int rankWidth = 6;
        int usernameWidth = 15;
        int pointsWidth = 8;

        for (Map<String, Object> entry : leaderboard) {
            String username = (String) entry.getOrDefault("username", "");
            usernameWidth = Math.max(usernameWidth, username.length());
        }

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

        System.out.printf("+-%" + rankWidth + "s-+-%-" + usernameWidth + "s-+-%-" + pointsWidth + "s-+%n",
                String.join("", Collections.nCopies(rankWidth, "-")),
                String.join("", Collections.nCopies(usernameWidth, "-")),
                String.join("", Collections.nCopies(pointsWidth, "-")));
    }

    private boolean isValidPlayer(int playerID) {
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

    @Override
    public void getSetting(String key, StringHolder value, String sessionToken) {
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