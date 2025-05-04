package Server_Java.implementation;

import Server_Java.database.DatabaseConnection;
import Server_Java.idls.GameIDL.AlreadyGuessedLetterException;
import Server_Java.idls.GameIDL.GameNotFoundException;
import Server_Java.idls.GameIDL.GameServicePOA;
import Server_Java.idls.GameIDL.GameTimeOutException;
import Server_Java.idls.GameIDL.MaxAttemptsReachedException;
import Server_Java.idls.GameIDL.NotEnoughPlayersException;
import Server_Java.idls.GameIDL.NotLoggedInException;
import Server_Java.idls.PlayerCallBackIDL.GameCallBackService;
import Server_Java.idls.PlayerCallBackIDL.WaitingRoomGameCallbackService;
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
                int lobbyWaitingTime,
                int roundDuration,
                int nextRoundDelay,
                int countdownSeconds,
                int totalRounds,
                int minimumPlayers,
                int numberOfLives
        ) {
            this.token            = token;
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

    @Override
    public synchronized String joinLobby(int playerID, String sessionToken)
            throws NotLoggedInException {
        if (sessionToken == null) {
            throw new NotLoggedInException();
        }
        if (sessionToGame.containsKey(sessionToken)) {
            return sessionToGame.get(sessionToken);
        }

        // 1) Find or create a lobby
        Lobby target = null;
        for (Lobby l : lobbies.values()) {
            if (l.players.size() < l.minimumPlayers) {
                target = l;
                break;
            }
        }
        if (target == null) {
            Function<String,Integer> fetchInt = key -> {
                StringHolder sh = new StringHolder();
                getSetting(key, sh, sessionToken);
                return Integer.parseInt(sh.value);
            };
            String newToken = UUID.randomUUID().toString();
            target = new Lobby(
                    newToken,
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
                    " [wait=" + target.lobbyWaitingTime +
                    ", roundDur=" + target.roundDuration +
                    ", nextDelay=" + target.nextRoundDelay +
                    ", countdown=" + target.countdownSeconds +
                    ", totalRounds=" + target.totalRounds +
                    ", minPlayers=" + target.minimumPlayers +
                    ", lives=" + target.numberOfLives + "]");
        }

        // 2) Add player if not already in
        if (!target.players.contains(playerID)) {
            target.players.add(playerID);
            sessionToGame.put(sessionToken, target.token);
            System.out.println("[GameService DEBUG] joinLobby: player=" + playerID +
                    " joined lobby=" + target.token +
                    " (count=" + target.players.size() + ")");
        }

        // 3) Notify all waiting‐room callbacks of the new player count
        for (WaitingRoomGameCallbackService cb : target.waitingCallbacks) {
            try {
                cb.notifyPlayerJoined(
                        target.token,
                        target.players.size(),
                        sessionToken
                );
            } catch (Exception ignored) {}
        }

        // 4) If we now have enough players, start the countdown sequence
        if (target.players.size() == target.minimumPlayers) {
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
                                sessionToken
                        );
                    } catch (Exception ignored) {}
                }
                System.out.println("[GameService DEBUG] notifyCountdownStart sent for lobby="
                        + lobbyRef.token);

                // B) Schedule the actual game start after countdownSeconds
                countdownScheduler.schedule(() -> {
                    for (GameCallBackService gcb : lobbyRef.callbacks.values()) {
                        try {
                            gcb.notifyGameStart(lobbyRef.token, sessionToken);
                        } catch (Exception ignored) {}
                    }
                    System.out.println("[GameService DEBUG] notifyGameStart sent for lobby="
                            + lobbyRef.token);

                    // Optionally: clean up lobby here if you wish
                }, lobbyRef.countdownSeconds, TimeUnit.SECONDS);

            }, 100, TimeUnit.MILLISECONDS);

            pendingCountdowns.put(target.token, newTask);
        }

        debugPrintAllLobbies("joinLobby");
        return target.token;
    }


    @Override
    public synchronized void leaveLobby(int playerID,
                                        String gameToken,
                                        String sessionToken)
            throws NotLoggedInException {
        if (sessionToken == null || !sessionToGame.containsKey(sessionToken))
            throw new NotLoggedInException();

        Lobby lobby = lobbies.get(gameToken);
        if (lobby != null) {
            lobby.players.removeIf(pid -> pid == playerID);
            WaitingRoomGameCallbackService wcb = sessionToWaitingCallback.remove(sessionToken);
            if (wcb != null) lobby.waitingCallbacks.remove(wcb);

            for (WaitingRoomGameCallbackService cb : lobby.waitingCallbacks) {
                try {
                    cb.notifyPlayerJoined(gameToken, lobby.players.size(), sessionToken);
                } catch (Exception ignored) {}
            }

            if (lobby.players.size() < DEFAULT_MIN_PLAYERS) {
                ScheduledFuture<?> future = pendingCountdowns.remove(gameToken);
                if (future != null) future.cancel(false);
                for (WaitingRoomGameCallbackService cb : lobby.waitingCallbacks) {
                    try {
                        cb.notifyCountdownReset(gameToken, sessionToken);
                    } catch (Exception ignored) {}
                }
            }

            if (lobby.players.isEmpty()) {
                lobbies.remove(gameToken);
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
        ) throw new NotLoggedInException();

        Lobby lobby = lobbies.get(gameToken);
        if (lobby == null) throw new NotLoggedInException();

        lobby.waitingCallbacks.add(cb);
        sessionToWaitingCallback.put(sessionToken, cb);
        System.out.println("[GameService DEBUG] registerWaitingRoomCallback: player="
                + playerID + " in lobby=" + gameToken
                + " (waitingCallbacks=" + lobby.waitingCallbacks.size() + ")");
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
        if (token == null) throw new NotLoggedInException();
        Lobby lobby = lobbies.get(token);
        int size = (lobby != null) ? lobby.players.size() : 0;
        System.out.println("[GameService DEBUG] getNumberOfPlayersJoined for lobby="
                + token + ": " + size);
        return size;
    }
    @Override
    public synchronized int startGame(int playerID, String sessionToken)
            throws NotEnoughPlayersException {
        String token = sessionToGame.get(sessionToken);
        Lobby lobby   = (token == null ? null : lobbies.get(token));
        if (lobby == null || lobby.players.size() < DEFAULT_MIN_PLAYERS) {
            throw new NotEnoughPlayersException();
        }

        // 1) Notify all clients that the game is starting
        for (GameCallBackService cb : lobby.callbacks.values()) {
            try {
                cb.notifyGameStart(token, sessionToken);
            } catch (Exception ignored) {}
        }
        System.out.println("[GameService DEBUG] notifyGameStart sent for lobby=" + token);

        // 2) Kick off ROUND 1 immediately on the server
        //    Pick the same playerID and sessionToken (doesn't matter who),
        //    so that notifyRoundStart(1) is broadcast.
        try {
            startRound(token, 1, playerID, sessionToken);
        } catch (Exception e) {
            // Shouldn't happen: we know the game exists and has players
            e.printStackTrace();
        }

        // 3) Do *not* remove the lobby here—let cleanup happen after the last round
        //    That way future startRound/guessLetter calls work correctly.
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
        lobby.callbacks.put(sessionToken, cb);
        sessionToCallback.put(sessionToken, cb);
        System.out.println("[GameService DEBUG] registerCallBack: player="
                + playerID + ", lobby=" + gameToken
                + " (gameCallbacks=" + lobby.callbacks.size() + ")");
    }

    @Override
    public synchronized int startRound(
            String gameToken,
            int roundNumber,
            int playerID,
            String sessionToken
    ) throws GameNotFoundException, NotLoggedInException {
        Lobby lobby = lobbies.get(gameToken);
        if (lobby == null) throw new GameNotFoundException();

        currentRoundNumbers.put(gameToken, roundNumber);
        String currentWord = WORDS.get(RAND.nextInt(WORDS.size())).toUpperCase();
        roundWords.put(gameToken, currentWord);

        Map<Integer, RoundState> perPlayerMap =
                roundStates.computeIfAbsent(gameToken, t -> new ConcurrentHashMap<>());
        perPlayerMap.clear();
        for (Integer pid : lobby.players) {
            perPlayerMap.put(pid, new RoundState(currentWord));
        }

        for (GameCallBackService cb : lobby.callbacks.values()) {
            try { cb.notifyRoundStart(gameToken, roundNumber, sessionToken); }
            catch (Exception ignored) {}
        }

        countdownScheduler.schedule(
                () -> handleRoundTimeout(gameToken, roundNumber),
                lobby.roundDuration,
                TimeUnit.SECONDS
        );
        return lobby.players.size();
    }

    public synchronized void handleRoundTimeout(String gameToken, int roundNumber) {
        Integer current = currentRoundNumbers.get(gameToken);
        if (current == null || current != roundNumber) return;
        Lobby lobby = lobbies.get(gameToken);
        if (lobby == null) return;

        String key = gameToken + ":" + roundNumber;
        if (roundWinners.containsKey(key)) return;

        // 1) Notify round end (no winner)
        for (GameCallBackService cb : lobby.callbacks.values()) {
            try {
                cb.notifyRoundEnd(gameToken, "", "");
            } catch (Exception ignored) {}
        }

        // 2) Then schedule whatever comes next—**always** after the configured delay
        if (roundNumber < lobby.totalRounds) {
            // schedule the next round start
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
                    } catch (Exception ignored) {}
                }
            }, lobby.nextRoundDelay, TimeUnit.SECONDS);

        } else {
            // schedule the game end callback
            countdownScheduler.schedule(() -> {
                for (GameCallBackService cb : lobby.callbacks.values()) {
                    try {
                        cb.notifyGameEnd(gameToken, "", "");
                    } catch (Exception ignored) {}
                }
                cleanupGame(gameToken);
            }, lobby.nextRoundDelay, TimeUnit.SECONDS);
        }
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
            throw new GameNotFoundException();
        }
        String word = roundWords.get(gameToken);
        if (word == null) throw new GameNotFoundException();

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
            throw new GameNotFoundException();
        }
        RoundState rs = perPlayer.get(playerID);

        // 1) Duplicate‐guess check
        if (!rs.guessed.add(letter)) {
            throw new AlreadyGuessedLetterException();
        }

        // 2) Compute hits
        String word = roundWords.get(gameToken);
        if (word == null) throw new GameNotFoundException();
        List<Integer> hits = new ArrayList<>();
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == letter) hits.add(i);
        }

        // 3) Handle “all‐lost” due to too many wrongs
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
                    // a) notify round end (no winner)
                    lobby.callbacks.values().forEach(cb -> {
                        try { cb.notifyRoundEnd(gameToken, sessionToken, ""); }
                        catch (Exception ignored) {}
                    });

                    int roundNum = currentRoundNumbers.getOrDefault(gameToken, 1);
                    if (roundNum < lobby.totalRounds) {
                        // b) schedule next round after delay
                        countdownScheduler.schedule(() -> {
                            try {
                                startRound(gameToken, roundNum + 1, playerID, sessionToken);
                            } catch (Exception ignored) {}
                        }, lobby.nextRoundDelay, TimeUnit.SECONDS);

                    } else {
                        // c) schedule game end after delay
                        countdownScheduler.schedule(() -> {
                            lobby.callbacks.values().forEach(cb -> {
                                try { cb.notifyGameEnd(gameToken, sessionToken, ""); }
                                catch (Exception ignored) {}
                            });
                            cleanupGame(gameToken);
                        }, lobby.nextRoundDelay, TimeUnit.SECONDS);
                    }
                }

                throw new MaxAttemptsReachedException();
            }
        }

        // 4) Handle “all letters guessed” win
        boolean allRevealed = word.chars()
                .mapToObj(c -> (char)c)
                .allMatch(rs.guessed::contains);

        if (allRevealed) {
            Lobby lobby = lobbies.get(gameToken);
            int roundNum = currentRoundNumbers.getOrDefault(gameToken, 1);
            String username = lookupUsername(playerID);

            // a) notify round end with winner
            lobby.callbacks.values().forEach(cb -> {
                try { cb.notifyRoundEnd(gameToken, sessionToken, username); }
                catch (Exception ignored) {}
            });
            roundWinners.put(gameToken + ":" + roundNum, username);

            if (roundNum < lobby.totalRounds) {
                // b) schedule next round
                countdownScheduler.schedule(() -> {
                    try {
                        startRound(gameToken, roundNum + 1, playerID, sessionToken);
                    } catch (Exception ignored) {}
                }, lobby.nextRoundDelay, TimeUnit.SECONDS);

            } else {
                // c) schedule game end
                countdownScheduler.schedule(() -> {
                    lobby.callbacks.values().forEach(cb -> {
                        try { cb.notifyGameEnd(gameToken, sessionToken, username); }
                        catch (Exception ignored) {}
                    });
                    cleanupGame(gameToken);
                }, lobby.nextRoundDelay, TimeUnit.SECONDS);
            }

            // d) return positions so UI can reveal them
            return hits.stream().mapToInt(Integer::intValue).toArray();
        }

        // 5) Default: just return hits
        return hits.stream().mapToInt(Integer::intValue).toArray();
    }



    private void cleanupGame(String gameToken) {
        lobbies.remove(gameToken);
        sessionToGame.values().removeIf(t -> t.equals(gameToken));
        sessionToCallback.keySet().removeIf(st -> !sessionToGame.containsKey(st));
        sessionToWaitingCallback.keySet().removeIf(st -> !sessionToGame.containsKey(st));
        roundStates.remove(gameToken);
        roundWords.remove(gameToken);
        currentRoundNumbers.remove(gameToken);
        roundWinners.keySet().removeIf(k -> k.startsWith(gameToken + ":"));
    }

    private String lookupUsername(int playerID) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT username FROM players WHERE player_id = ?"
             )) {
            stmt.setInt(1, playerID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("username");
        } catch (SQLException e) {
            System.err.println("[GameService] lookupUsername failed: " + e);
        }
        return "Player" + playerID;
    }

    @Override
    public String getRoundWinner(String gameToken,
                                 int playerID,
                                 String sessionToken)
            throws NotLoggedInException {
        int roundNum = currentRoundNumbers.getOrDefault(gameToken, 1);
        String key   = gameToken + ":" + roundNum;
        return roundWinners.getOrDefault(key, "Unknown");
    }

    @Override
    public String getGameWinner(String gameToken, int playerID, String sessionToken)
            throws NotLoggedInException {
        return "";
    }

    @Override
    public int getPlayerWins(String gameToken, int playerID, String sessionToken)
            throws NotLoggedInException {
        return 0;
    }

    @Override
    public String getDisplayName(int playerID, String sessionToken)
            throws NotLoggedInException {
        return "";
    }

    @Override
    public String[] getLeaderboards(int playerID, String sessionToken)
            throws NotLoggedInException {
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
