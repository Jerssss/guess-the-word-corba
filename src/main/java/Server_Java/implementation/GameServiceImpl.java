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

public class GameServiceImpl extends GameServicePOA {

    private static final int DEFAULT_MIN_PLAYERS = 2;
    private static final int DEFAULT_COUNTDOWN_SECONDS = 10;
    private static final int WINS_NEEDED = 3;

    private static final Map<String, Lobby> lobbies = new ConcurrentHashMap<>();
    private static final Map<String, String> sessionToGame = new ConcurrentHashMap<>();
    private static final Map<String, GameCallBackService> sessionToCallback = new ConcurrentHashMap<>();
    private static final Map<String, WaitingRoomGameCallbackService> sessionToWaitingCallback = new ConcurrentHashMap<>();
    private static final Map<String, String> roundWords = new ConcurrentHashMap<>();
    private static final Map<String, Map<Integer, RoundState>> roundStates = new ConcurrentHashMap<>();
    private static final Map<String, Integer> currentRoundNumbers = new ConcurrentHashMap<>();
    private static final Map<String, String> roundWinners = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Future<?>> roundTimeoutTasks = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> usedWordsPerGame = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, Integer>> gameWinCounts = new ConcurrentHashMap<>();
    private static final Map<String, Long> roundStartTimes = new ConcurrentHashMap<>();
    private static final Map<String, Long> lobbyCountdownStartTimes = new ConcurrentHashMap<>();

    private static final ScheduledExecutorService countdownScheduler =
            Executors.newSingleThreadScheduledExecutor();
    private static final Map<String, ScheduledFuture<?>> pendingCountdowns =
            new ConcurrentHashMap<>();
    private static final Random RAND = new Random();

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
        long completionTime = Long.MAX_VALUE;

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
            if (lobbyCountdownStartTimes.containsKey(l.token)) {
                target = l;
                break;
            }
        }
        if (target == null) {
            Map<String, Integer> settings = new HashMap<>();
            String[] keys = {
                    "lobby_waiting_time",
                    "round_duration",
                    "next_round_delay",
                    "countdown_to_game_start",
                    "minimum_players",
                    "number_of_lives"
            };
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT " + String.join(", ", keys) + " FROM settings WHERE id=1")) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    for (String key : keys) {
                        settings.put(key, rs.getInt(key));
                        System.out.println("[GameService DEBUG] Fetched setting: " + key + "=" + settings.get(key));
                    }
                } else {
                    System.err.println("[GameService ERROR] No settings found for id=1");
                    throw new RuntimeException("Failed to fetch settings");
                }
            } catch (SQLException e) {
                System.err.println("[GameService ERROR] Error fetching settings: " + e.getMessage());
                throw new RuntimeException("Failed to fetch settings");
            }

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
                    settings.get("lobby_waiting_time"),
                    settings.get("round_duration"),
                    settings.get("next_round_delay"),
                    settings.get("countdown_to_game_start"),
                    settings.get("minimum_players"),
                    settings.get("number_of_lives")
            );
            lobbies.put(newToken, target);
            gameWinCounts.put(newToken, new ConcurrentHashMap<>());
            System.out.println("[GameService DEBUG] Created lobby=" + newToken + " with gameId=" + gameId);

            lobbyCountdownStartTimes.put(newToken, System.currentTimeMillis());
            scheduleCountdown(newToken, target);
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
                cb.notifyPlayerJoined(target.token, target.players.size(), sessionToken);
                System.out.println("[GameService DEBUG] Notified waiting callback for session=" + sessionToken +
                        ", player count=" + target.players.size());
            } catch (Exception e) {
                System.err.println("[GameService ERROR] Failed to notify waiting callback: " + e.getMessage());
            }
        }

        WaitingRoomGameCallbackService newPlayerCallback = sessionToWaitingCallback.get(sessionToken);
        if (newPlayerCallback != null && lobbyCountdownStartTimes.containsKey(target.token) && pendingCountdowns.containsKey(target.token)) {
            long startTime = lobbyCountdownStartTimes.get(target.token);
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            int remaining = Math.max(0, target.countdownSeconds - (int) elapsed);
            try {
                newPlayerCallback.notifyCountdownStart(target.token, remaining, sessionToken);
                System.out.println("[GameService DEBUG] Notified new player session=" + sessionToken +
                        " of countdown, remaining=" + remaining + " seconds");
            } catch (Exception e) {
                System.err.println("[GameService ERROR] Failed to notify countdown start to new player: " + e.getMessage());
            }
        }

        debugPrintAllLobbies("joinLobby");
        return target.token;
    }

    private void scheduleCountdown(String gameToken, Lobby lobby) {
        if (!pendingCountdowns.containsKey(gameToken)) {
            ScheduledFuture<?> countdownTask = countdownScheduler.schedule(() -> {
                pendingCountdowns.remove(gameToken);
                if (lobby.players.size() >= lobby.minimumPlayers) {
                    try {
                        startGame(lobby.players.get(0), sessionToGame.entrySet().stream()
                                .filter(e -> e.getValue().equals(gameToken))
                                .findFirst()
                                .map(Map.Entry::getKey)
                                .orElse(null));
                        System.out.println("[GameService DEBUG] Countdown ended, started game for lobby=" + gameToken);
                    } catch (NotEnoughPlayersException e) {
                        System.err.println("[GameService ERROR] Unexpected: Not enough players after countdown: " + e.getMessage());
                    }
                } else {
                    for (WaitingRoomGameCallbackService cb : lobby.waitingCallbacks) {
                        try {
                            cb.notifyPlayerJoined(gameToken, lobby.players.size(), null);
                            System.out.println("[GameService DEBUG] Notified waiting clients of player count=" + lobby.players.size() + " for lobby=" + gameToken);
                        } catch (Exception e) {
                            System.err.println("[GameService ERROR] Failed to notify waiting clients: " + e.getMessage());
                        }
                    }
                    lobbyCountdownStartTimes.put(gameToken, System.currentTimeMillis());
                    System.out.println("[GameService DEBUG] Lobby " + gameToken + " remains active, waiting for more players");
                }
            }, lobby.countdownSeconds, TimeUnit.SECONDS);
            pendingCountdowns.put(gameToken, countdownTask);

            for (WaitingRoomGameCallbackService cb : lobby.waitingCallbacks) {
                try {
                    cb.notifyCountdownStart(gameToken, lobby.countdownSeconds, sessionToGame.entrySet().stream()
                            .filter(e -> e.getValue().equals(gameToken))
                            .findFirst()
                            .map(Map.Entry::getKey)
                            .orElse(null));
                    System.out.println("[GameService DEBUG] Sent notifyCountdownStart for lobby=" + gameToken);
                } catch (Exception e) {
                    System.err.println("[GameService ERROR] Failed to notify countdown start: " + e.getMessage());
                }
            }
        }
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

            if (lobby.players.isEmpty()) {
                ScheduledFuture<?> future = pendingCountdowns.remove(gameToken);
                if (future != null) future.cancel(false);
                lobbies.remove(gameToken);
                gameWinCounts.remove(gameToken);
                lobbyCountdownStartTimes.remove(gameToken);
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
        if (sessionToken == null || !sessionToGame.containsKey(sessionToken) || !sessionToGame.get(sessionToken).equals(gameToken)) {
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
            Long startTime = lobbyCountdownStartTimes.get(gameToken);
            if (startTime != null && pendingCountdowns.containsKey(gameToken)) {
                long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                int remaining = Math.max(0, lobby.countdownSeconds - (int) elapsed);
                cb.notifyCountdownStart(gameToken, remaining, sessionToken);
                System.out.println("[GameService DEBUG] Sent notifyCountdownStart with remaining=" + remaining + " to session=" + sessionToken);
            }
        } catch (Exception e) {
            System.err.println("[GameService ERROR] Failed to send initial notifications: " + e.getMessage());
        }
    }

    @Override
    public String getLobbyStatus(String sessionToken)
            throws NotLoggedInException, GameTimeOutException, NotEnoughPlayersException {
        if (sessionToken == null || !sessionToGame.containsKey(sessionToken)) {
            System.err.println("[GameService ERROR] getLobbyStatus: Invalid session token");
            throw new NotLoggedInException();
        }
        String gameToken = sessionToGame.get(sessionToken);
        Lobby lobby = lobbies.get(gameToken);
        if (lobby == null) {
            System.err.println("[GameService ERROR] getLobbyStatus: Lobby not found for gameToken=" + gameToken);
            throw new GameTimeOutException();
        }

        String status = currentRoundNumbers.containsKey(gameToken) ? "in_progress" : "waiting";
        int playerCount = lobby.players.size();
        long remainingSeconds = -1;
        Long startTime = lobbyCountdownStartTimes.get(gameToken);
        if (startTime != null && pendingCountdowns.containsKey(gameToken)) {
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            remainingSeconds = Math.max(0, lobby.countdownSeconds - elapsed);
        }

        String result = String.format(
                "Lobby %s: %s, Players: %d/%d, Countdown: %s",
                gameToken,
                status,
                playerCount,
                lobby.minimumPlayers,
                remainingSeconds >= 0 ? remainingSeconds + " seconds" : "waiting for players"
        );
        System.out.println("[GameService DEBUG] getLobbyStatus: " + result);
        return result;
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
        if (lobby == null || lobby.players.size() < lobby.minimumPlayers) {
            System.err.println("[GameService ERROR] startGame: Not enough players for session=" + sessionToken + ", required=" + (lobby != null ? lobby.minimumPlayers : DEFAULT_MIN_PLAYERS) + ", current=" + (lobby != null ? lobby.players.size() : 0));
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

        ScheduledFuture<?> countdown = pendingCountdowns.remove(token);
        if (countdown != null) {
            countdown.cancel(false);
        }
        lobbyCountdownStartTimes.remove(token);

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

        try {
            startRound(token, 1);
            System.out.println("[GameService DEBUG] Started round 1 for lobby=" + token);
        } catch (GameNotFoundException e) {
            System.err.println("[GameService ERROR] Failed to start round 1: " + e.getMessage());
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
        if (lobby == null || !sessionToGame.containsKey(sessionToken) || !sessionToGame.get(sessionToken).equals(gameToken)) {
            System.err.println("[GameService ERROR] registerCallBack: Invalid lobby or session for playerID=" + playerID + ", gameToken=" + gameToken);
            throw new NotLoggedInException();
        }
        if (!lobby.players.contains(playerID)) {
            System.err.println("[GameService ERROR] registerCallBack: PlayerID=" + playerID + " not in lobby=" + gameToken);
            throw new NotLoggedInException();
        }

        // Ensure current round is valid (not 0)
        int currentRound = currentRoundNumbers.getOrDefault(gameToken, 1);
        if (currentRound <= 0) {
            System.err.println("[GameService ERROR] registerCallBack: Invalid round number " + currentRound + " for playerID=" + playerID);
            throw new NotLoggedInException("Game not yet initialized");
        }

        if (lobby.callbacks.containsKey(sessionToken)) {
            System.out.println("[GameService DEBUG] registerCallBack: Updating existing callback for playerID=" + playerID + ", session=" + sessionToken);
        } else {
            System.out.println("[GameService DEBUG] registerCallBack: Registering new callback for playerID=" + playerID + ", session=" + sessionToken);
        }
        lobby.callbacks.put(sessionToken, cb);
        sessionToCallback.put(sessionToken, cb);
        System.out.println("[GameService DEBUG] registerCallBack: playerID=" + playerID +
                ", lobby=" + gameToken +
                " (gameCallbacks=" + lobby.callbacks.size() + ", currentRound=" + currentRound + ")");

        // Proactively notify late-joining client of current round
        try {
            cb.notifyRoundStart(gameToken, currentRound, sessionToken);
            System.out.println("[GameService DEBUG] Sent notifyRoundStart for round=" + currentRound + " to late-joining session=" + sessionToken);
        } catch (Exception e) {
            System.err.println("[GameService ERROR] Failed to notify round start for late-joining session=" + sessionToken + ": " + e.getMessage());
            lobby.callbacks.remove(sessionToken);
            sessionToCallback.remove(sessionToken);
        }
    }

    @Override
    public int startRound(String gameToken, int roundNumber, int playerID, String sessionToken)
            throws GameNotFoundException, NotLoggedInException {
        Lobby lobby = lobbies.get(gameToken);
        if (lobby == null || !lobby.players.contains(playerID)) {
            System.err.println("[GameService ERROR] startRound: Invalid game or player for gameToken=" + gameToken);
            throw new GameNotFoundException();
        }
        if (sessionToken == null || !sessionToGame.containsKey(sessionToken)) {
            System.err.println("[GameService ERROR] startRound: Invalid session for playerID=" + playerID);
            throw new NotLoggedInException();
        }

        try {
            startRound(gameToken, roundNumber);
            return 1;
        } catch (GameNotFoundException e) {
            System.err.println("[GameService ERROR] startRound failed: " + e.getMessage());
            return 0;
        }
    }

    public synchronized void startRound(String gameToken, int roundNumber)
            throws GameNotFoundException {
        // Prevent Round 0
        if (roundNumber <= 0) {
            System.err.println("[GameService ERROR] startRound: Invalid round number " + roundNumber + " for gameToken=" + gameToken);
            throw new GameNotFoundException("Round number must be greater than 0");
        }

        Lobby lobby = lobbies.get(gameToken);
        if (lobby == null) {
            System.err.println("[GameService ERROR] startRound: Lobby not found for gameToken=" + gameToken);
            throw new GameNotFoundException();
        }

        currentRoundNumbers.put(gameToken, roundNumber);
        String roundKey = gameToken + ":" + roundNumber;
        System.out.println("[GameService DEBUG] startRound: Set currentRoundNumbers for gameToken=" + gameToken + " to round=" + roundNumber);

        // Assign word
        Set<String> usedWords = usedWordsPerGame.computeIfAbsent(gameToken, k -> new HashSet<>());
        String currentWord;
        do {
            currentWord = WORDS.get(RAND.nextInt(WORDS.size())).toUpperCase();
        } while (usedWords.contains(currentWord));
        usedWords.add(currentWord);
        roundWords.put(roundKey, currentWord);
        System.out.println("[GameService DEBUG] Assigned word for gameToken=" + gameToken + ", round=" + roundNumber + ": " + currentWord);

        // Database operations
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

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO rounds (game_id, round_number, word, start_time) VALUES (?, ?, ?, NOW())")) {
            stmt.setInt(1, lobby.gameId);
            stmt.setInt(2, roundNumber);
            stmt.setString(3, currentWord);
            stmt.executeUpdate();
            System.out.println("[GameService DEBUG] Created round record for gameId=" + lobby.gameId + ", round=" + roundNumber);
        } catch (SQLException e) {
            if (e.getSQLState().equals("23000")) {
                System.out.println("[GameService DEBUG] Round already exists for gameId=" + lobby.gameId + ", round=" + roundNumber);
                return;
            }
            System.err.println("[GameService ERROR] Error creating round record: " + e.getMessage());
            currentRoundNumbers.remove(gameToken);
            throw new GameNotFoundException("Failed to create round record: " + e.getMessage());
        }

        // Initialize roundStates
        roundStartTimes.put(roundKey, System.currentTimeMillis());
        Map<Integer, RoundState> perPlayerMap = roundStates.computeIfAbsent(gameToken, t -> new ConcurrentHashMap<>());
        perPlayerMap.clear();
        for (Integer pid : lobby.players) {
            perPlayerMap.put(pid, new RoundState(currentWord));
            System.out.println("[GameService DEBUG] Initialized RoundState for playerID=" + pid + " in gameToken=" + gameToken + ", round=" + roundNumber);
        }

        // Send notifyRoundStart callbacks AFTER initialization
        List<String> failedSessions = new ArrayList<>();
        for (Map.Entry<String, GameCallBackService> entry : lobby.callbacks.entrySet()) {
            String sessionToken = entry.getKey();
            GameCallBackService cb = entry.getValue();
            try {
                cb.notifyRoundStart(gameToken, roundNumber, sessionToken);
                System.out.println("[GameService DEBUG] Sent notifyRoundStart for round=" + roundNumber + " to session=" + sessionToken);
            } catch (Exception e) {
                System.err.println("[GameService ERROR] Failed to notify round start for session=" + sessionToken + ": " + e.getMessage());
                failedSessions.add(sessionToken);
            }
        }
        for (String session : failedSessions) {
            lobby.callbacks.remove(session);
            sessionToCallback.remove(session);
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
    }

    public synchronized void handleRoundTimeout(String gameToken, int roundNumber) {
        Integer current = currentRoundNumbers.get(gameToken);
        if (current == null || current != roundNumber) return;
        Lobby lobby = lobbies.get(gameToken);
        if (lobby == null) return;

        String roundKey = gameToken + ":" + roundNumber;
        if (roundWinners.containsKey(roundKey)) return;

        String secretWord = roundWords.getOrDefault(roundKey, "");
        for (Map.Entry<String, GameCallBackService> entry : lobby.callbacks.entrySet()) {
            String sessionToken = entry.getKey();
            GameCallBackService cb = entry.getValue();
            try {
                cb.notifyRoundEnd(gameToken, sessionToken, "", secretWord);
                System.out.println("[GameService DEBUG] Sent notifyRoundEnd (timeout, no winner) for round=" + roundNumber + ", session=" + sessionToken);
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

        roundStartTimes.remove(roundKey);
        checkAndScheduleNextRoundOrEndGame(gameToken, roundNumber);
    }

    @Override
    public String getRandomWord(
            String gameToken,
            int roundNumber,
            int playerID,
            String sessionToken
    ) throws GameNotFoundException, NotLoggedInException {
        System.out.println("[GameService DEBUG] getRandomWord called: gameToken=" + gameToken +
                ", roundNumber=" + roundNumber + ", playerID=" + playerID + ", sessionToken=" + sessionToken);

        if (sessionToken == null || !sessionToGame.containsKey(sessionToken)) {
            System.err.println("[GameService ERROR] getRandomWord: Invalid sessionToken=" + sessionToken +
                    ", sessionToGame contains: " + sessionToGame.keySet());
            throw new NotLoggedInException();
        }

        String mappedGameToken = sessionToGame.get(sessionToken);
        if (!gameToken.equals(mappedGameToken)) {
            System.err.println("[GameService ERROR] getRandomWord: Game token mismatch, expected=" +
                    mappedGameToken + ", got=" + gameToken);
            throw new NotLoggedInException();
        }

        Lobby lobby = lobbies.get(gameToken);
        if (lobby == null) {
            System.err.println("[GameService ERROR] getRandomWord: Lobby not found for gameToken=" + gameToken);
            throw new GameNotFoundException();
        }

        if (!lobby.players.contains(playerID)) {
            System.err.println("[GameService ERROR] getRandomWord: PlayerID=" + playerID +
                    " not in lobby players=" + lobby.players);
            throw new NotLoggedInException();
        }

        Integer currentRound = currentRoundNumbers.get(gameToken);
        if (currentRound == null || currentRound != roundNumber) {
            System.err.println("[GameService ERROR] getRandomWord: Invalid roundNumber=" + roundNumber +
                    ", currentRound=" + (currentRound != null ? currentRound : "null"));
            throw new GameNotFoundException();
        }

        Map<Integer, RoundState> perPlayer = roundStates.get(gameToken);
        if (perPlayer == null || !perPlayer.containsKey(playerID)) {
            System.err.println("[GameService ERROR] getRandomWord: No RoundState for gameToken=" + gameToken +
                    ", playerID=" + playerID + ", roundStates=" + (perPlayer != null ? perPlayer.keySet() : "null"));
            throw new GameNotFoundException();
        }

        String roundKey = gameToken + ":" + roundNumber;
        String word = roundWords.get(roundKey);
        if (word == null) {
            System.err.println("[GameService ERROR] getRandomWord: No word found for gameToken=" + gameToken +
                    ", round=" + roundNumber);
            throw new GameNotFoundException();
        }

        RoundState rs = perPlayer.get(playerID);
        char[] mask = new char[word.length()];
        for (int i = 0; i < word.length(); i++) {
            mask[i] = rs.guessed.contains(word.charAt(i)) ? word.charAt(i) : '_';
        }
        String maskedWord = new String(mask);
        System.out.println("[GameService DEBUG] getRandomWord: Returning masked word=" + maskedWord +
                " for playerID=" + playerID + ", round=" + roundNumber);
        return maskedWord;
    }

    @Override
    public synchronized int[] guessLetter(
            String gameToken,
            int playerID,
            String sessionToken,
            char letter,
            int guessTime
    ) throws AlreadyGuessedLetterException, GameNotFoundException, NotLoggedInException, MaxAttemptsReachedException {
        Map<Integer, RoundState> perPlayer = roundStates.get(gameToken);
        if (perPlayer == null || !perPlayer.containsKey(playerID)) {
            System.err.println("[GameService ERROR] guessLetter: Invalid game or player for gameToken=" + gameToken);
            throw new GameNotFoundException();
        }
        RoundState rs = perPlayer.get(playerID);

        int roundNum = currentRoundNumbers.getOrDefault(gameToken, 1);
        String roundKey = gameToken + ":" + roundNum;
        String word = roundWords.get(roundKey);
        if (word == null) {
            System.err.println("[GameService ERROR] guessLetter: No word for gameToken=" + gameToken + ", round=" + roundNum);
            throw new GameNotFoundException();
        }

        Long roundStartTime = roundStartTimes.get(roundKey);
        if (roundStartTime == null || guessTime < 0 || guessTime > System.currentTimeMillis() - roundStartTime) {
            System.err.println("[GameService ERROR] guessLetter: Invalid guessTime=" + guessTime + " for playerID=" + playerID);
            guessTime = (int) (System.currentTimeMillis() - roundStartTime);
        }

        if (!rs.guessed.add(Character.toUpperCase(letter))) {
            System.out.println("[GameService DEBUG] guessLetter: Already guessed letter=" + letter + " by playerID=" + playerID);
            throw new AlreadyGuessedLetterException();
        }

        Lobby lobby = lobbies.get(gameToken);
        if (lobby == null) {
            System.err.println("[GameService ERROR] guessLetter: Lobby not found for gameToken=" + gameToken);
            throw new GameNotFoundException();
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO player_guesses (game_id, round_number, player_id, guess_letter, guess_time) VALUES (?, ?, ?, ?, ?)")) {
            stmt.setInt(1, lobby.gameId);
            stmt.setInt(2, roundNum);
            stmt.setInt(3, playerID);
            stmt.setString(4, String.valueOf(Character.toUpperCase(letter)));
            stmt.setLong(5, guessTime);
            stmt.executeUpdate();
            System.out.println("[GameService DEBUG] Logged guess for playerID=" + playerID + ", letter=" + letter + ", guessTime=" + guessTime);
        } catch (SQLException e) {
            System.err.println("[GameService ERROR] Error logging guess: " + e.getMessage());
        }

        List<Integer> hits = new ArrayList<>();
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == Character.toUpperCase(letter)) hits.add(i);
        }

        if (hits.isEmpty()) {
            rs.wrongCount++;
            if (rs.wrongCount >= lobby.numberOfLives) {
                throw new MaxAttemptsReachedException();
            }
        }

        boolean allRevealed = word.chars()
                .mapToObj(c -> (char)c)
                .allMatch(rs.guessed::contains);
        if (allRevealed) {
            rs.completionTime = guessTime;
            System.out.println("[GameService DEBUG] PlayerID=" + playerID + " completed word in " + guessTime + "ms");

            String winnerUsername = lookupUsername(playerID);
            roundWinners.put(roundKey, winnerUsername);

            Map<String, Integer> winCounts = gameWinCounts.get(gameToken);
            winCounts.put(winnerUsername, winCounts.getOrDefault(winnerUsername, 0) + 1);
            System.out.println("[GameService DEBUG] Round winner: " + winnerUsername + ", total wins=" + winCounts.get(winnerUsername));
            updateRoundWinnerInDB(gameToken, roundNum, winnerUsername);

            Future<?> timeoutTask = roundTimeoutTasks.get(gameToken);
            if (timeoutTask != null) {
                timeoutTask.cancel(true);
                roundTimeoutTasks.remove(gameToken);
            }

            for (Map.Entry<String, GameCallBackService> entry : lobby.callbacks.entrySet()) {
                String callbackSessionToken = entry.getKey();
                GameCallBackService cb = entry.getValue();
                try {
                    cb.notifyRoundEnd(gameToken, callbackSessionToken, winnerUsername, word);
                    System.out.println("[GameService DEBUG] Sent notifyRoundEnd with winner=" + winnerUsername + " for round=" + roundNum + ", session=" + callbackSessionToken);
                } catch (Exception e) {
                    System.err.println("[GameService ERROR] Failed to notify round end for session=" + callbackSessionToken + ": " + e.getMessage());
                }
            }

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE rounds SET end_time = NOW() WHERE game_id = ? AND round_number = ?")) {
                stmt.setInt(1, lobby.gameId);
                stmt.setInt(2, roundNum);
                stmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("[GameService ERROR] Error updating round end time: " + e.getMessage());
            }

            roundStartTimes.remove(roundKey);
            checkAndScheduleNextRoundOrEndGame(gameToken, roundNum);
        }

        System.out.println("[GameService DEBUG] guessLetter: playerID=" + playerID + ", letter=" + letter + ", hits=" + hits);
        return hits.stream().mapToInt(Integer::intValue).toArray();
    }

    private void checkAndScheduleNextRoundOrEndGame(String gameToken, int roundNumber) {
        Lobby lobby = lobbies.get(gameToken);
        if (lobby == null) return;

        String gameWinner = null;
        Map<String, Integer> winCounts = gameWinCounts.get(gameToken);
        for (Map.Entry<String, Integer> entry : winCounts.entrySet()) {
            if (entry.getValue() >= WINS_NEEDED) {
                gameWinner = entry.getKey();
                break;
            }
        }

        if (gameWinner != null) {
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
                        stmt.setString(1, gameWinner);
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                            int winnerPlayerId = rs.getInt("player_id");
                            try (PreparedStatement stmt2 = conn.prepareStatement(
                                    "UPDATE games SET game_winner = ? WHERE game_id = ?")) {
                                stmt2.setInt(1, winnerPlayerId);
                                stmt2.setInt(2, lobby.gameId);
                                stmt2.executeUpdate();
                            }
                            try (PreparedStatement stmt3 = conn.prepareStatement(
                                    "UPDATE players SET game_wins = COALESCE(game_wins, 0) + 1 WHERE player_id = ?")) {
                                stmt3.setInt(1, winnerPlayerId);
                                stmt3.executeUpdate();
                            }
                        }
                    }

                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    System.err.println("[GameService ERROR] Error updating game completion: " + e.getMessage());
                }
            } catch (SQLException e) {
                System.err.println("[GameService ERROR] Database connection error: " + e.getMessage());
            }

            for (Map.Entry<String, GameCallBackService> entry : lobby.callbacks.entrySet()) {
                String sessionToken = entry.getKey();
                GameCallBackService cb = entry.getValue();
                try {
                    cb.notifyGameEnd(gameToken, sessionToken, gameWinner);
                    System.out.println("[GameService DEBUG] Sent notifyGameEnd with winner=" + gameWinner + " to session=" + sessionToken);
                } catch (Exception e) {
                    System.err.println("[GameService ERROR] Failed to notify game end for session=" + sessionToken + ": " + e.getMessage());
                }
            }
            cleanupGame(gameToken);
        } else {
            countdownScheduler.schedule(() -> {
                try {
                    startRound(gameToken, roundNumber + 1);
                    System.out.println("[GameService DEBUG] Scheduled next round=" + (roundNumber + 1));
                } catch (GameNotFoundException e) {
                    System.err.println("[GameService ERROR] Failed to schedule next round: " + e.getMessage());
                }
            }, lobby.nextRoundDelay, TimeUnit.SECONDS);
        }
    }

    private void cleanupGame(String gameToken) {
        lobbies.remove(gameToken);
        sessionToGame.values().removeIf(t -> t.equals(gameToken));
        sessionToCallback.keySet().removeIf(st -> !sessionToGame.containsKey(st));
        sessionToWaitingCallback.keySet().removeIf(st -> !sessionToGame.containsKey(st));
        roundStates.remove(gameToken);
        roundWords.keySet().removeIf(k -> k.startsWith(gameToken + ":"));
        usedWordsPerGame.remove(gameToken);
        currentRoundNumbers.remove(gameToken);
        roundWinners.keySet().removeIf(k -> k.startsWith(gameToken + ":"));
        roundStartTimes.keySet().removeIf(k -> k.startsWith(gameToken + ":"));
        gameWinCounts.remove(gameToken);
        lobbyCountdownStartTimes.remove(gameToken);
        System.out.println("[GameService DEBUG] Cleaned up game: " + gameToken);
    }

    private String lookupUsername(int playerID) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT username FROM players WHERE player_id = ?")) {
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
        return "Player" + playerID;
    }

    @Override
    public String getRoundWinner(String gameToken, int playerID, String sessionToken)
            throws NotLoggedInException {
        if (sessionToken == null || !sessionToGame.containsKey(sessionToken)) {
            System.err.println("[GameService ERROR] getRoundWinner: Invalid session token for playerID=" + playerID);
            throw new NotLoggedInException();
        }

        int roundNum = currentRoundNumbers.getOrDefault(gameToken, 1);
        String key = gameToken + ":" + roundNum;
        String winner = roundWinners.getOrDefault(key, "");
        System.out.println("[GameService DEBUG] getRoundWinner: round=" + roundNum + ", winner=" + (winner.isEmpty() ? "none" : winner));
        return winner;
    }

    private void updateRoundWinnerInDB(String gameToken, int roundNumber, String winnerUsername) {
        if (winnerUsername.isEmpty()) return;
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
                stmt.executeUpdate();
                System.out.println("[GameService DEBUG] Updated round winner: " + winnerUsername + " for round=" + roundNumber);
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
        if (winCounts == null) return "";

        String winner = winCounts.entrySet().stream()
                .filter(e -> e.getValue() >= WINS_NEEDED)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("");
        System.out.println("[GameService DEBUG] getGameWinner: winner=" + (winner.isEmpty() ? "none" : winner));
        return winner;
    }

    @Override
    public int getPlayerWins(String gameToken, int playerID, String sessionToken)
            throws NotLoggedInException {
        if (sessionToken == null || !sessionToGame.containsKey(sessionToken)) {
            System.err.println("[GameService ERROR] getPlayerWins: Invalid session token for playerID=" + playerID);
            throw new NotLoggedInException();
        }

        String username = lookupUsername(playerID);
        Map<String, Integer> winCounts = gameWinCounts.get(gameToken);
        int wins = winCounts != null ? winCounts.getOrDefault(username, 0) : 0;
        System.out.println("[GameService DEBUG] getPlayerWins: playerID=" + playerID + ", wins=" + wins);
        return wins;
    }

    @Override
    public String getDisplayName(int playerID, String sessionToken)
            throws NotLoggedInException {
        if (sessionToken == null || !sessionToGame.containsKey(sessionToken)) {
            System.err.println("[GameService ERROR] getDisplayName: Invalid session token for playerID=" + playerID);
            throw new NotLoggedInException();
        }
        return lookupUsername(playerID);
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

        List<String> leaderboardEntries = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT username, COALESCE(game_wins, 0) AS game_wins FROM players ORDER BY game_wins DESC LIMIT 10")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String username = rs.getString("username");
                int gameWins = rs.getInt("game_wins");
                leaderboardEntries.add(username + ":" + gameWins);
            }
        } catch (SQLException e) {
            System.err.println("[GameService ERROR] Error fetching leaderboard: " + e.getMessage());
            return new String[0];
        }

        System.out.println("[GameService DEBUG] Fetched leaderboard with " + leaderboardEntries.size() + " entries");
        return leaderboardEntries.toArray(new String[0]);
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
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT " + key + " FROM settings WHERE id=1")) {
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