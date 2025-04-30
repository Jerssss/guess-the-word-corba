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

        if (!target.players.contains(playerID)) {
            target.players.add(playerID);
            sessionToGame.put(sessionToken, target.token);
            System.out.println("[GameService DEBUG] joinLobby: player=" + playerID +
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
            } catch (Exception ignored) {}
        }

        if (target.players.size() == target.minimumPlayers) {
            ScheduledFuture<?> oldTask = pendingCountdowns.remove(target.token);
            if (oldTask != null) oldTask.cancel(false);

            Lobby lobbyRef = target;
            ScheduledFuture<?> newTask = countdownScheduler.schedule(() -> {
                for (WaitingRoomGameCallbackService cb : lobbyRef.waitingCallbacks) {
                    try {
                        cb.notifyCountdownStart(
                                lobbyRef.token,
                                lobbyRef.countdownSeconds,
                                sessionToken
                        );
                    } catch (Exception ignored) {}
                }
                System.out.println("[GameService DEBUG] notifyCountdownStart sent for lobby=" +
                        lobbyRef.token);
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
    public int startGame(int playerID, String sessionToken) throws NotEnoughPlayersException {
        String token = sessionToGame.get(sessionToken);
        Lobby lobby = (token == null ? null : lobbies.get(token));
        if (lobby == null || lobby.players.size() < DEFAULT_MIN_PLAYERS)
            throw new NotEnoughPlayersException();

        for (GameCallBackService cb : lobby.callbacks.values()) {
            try { cb.notifyGameStart(token, sessionToken); }
            catch (Exception ignored) {}
        }

        lobbies.remove(token);
        sessionToGame.values().removeIf(t -> t.equals(token));
        sessionToCallback.keySet().removeIf(st -> !sessionToGame.containsKey(st));
        sessionToWaitingCallback.keySet().removeIf(st -> !sessionToGame.containsKey(st));

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

        if (roundNumber < lobby.totalRounds) {
            for (GameCallBackService cb : lobby.callbacks.values()) {
                try { cb.notifyRoundEnd(gameToken, "", ""); }
                catch (Exception ignored) {}
            }
            int next = roundNumber + 1;
            if (!lobby.players.isEmpty()) {
                Integer anyPid = lobby.players.get(0);
                String anySession = sessionToGame.entrySet().stream()
                        .filter(e -> e.getValue().equals(gameToken))
                        .map(Map.Entry::getKey)
                        .findFirst().orElse(null);
                if (anySession != null) {
                    try { startRound(gameToken, next, anyPid, anySession); }
                    catch (Exception ignored) {}
                }
            }
        } else {
            countdownScheduler.schedule(() -> {
                for (GameCallBackService cb : lobby.callbacks.values()) {
                    try { cb.notifyGameEnd(gameToken, "", ""); }
                    catch (Exception ignored) {}
                }
                cleanupGame(gameToken);
            }, 5, TimeUnit.SECONDS);
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

        // 1) Lookup per-player RoundState
        Map<Integer, RoundState> perPlayer = roundStates.get(gameToken);
        if (perPlayer == null || !perPlayer.containsKey(playerID)) {
            throw new GameNotFoundException();
        }
        RoundState rs = perPlayer.get(playerID);

        // 2) Duplicate-guess check
        if (!rs.guessed.add(letter)) {
            throw new AlreadyGuessedLetterException();
        }

        // 3) Retrieve the shared word
        String word = roundWords.get(gameToken);
        if (word == null) throw new GameNotFoundException();

        // 4) Find all hit positions
        List<Integer> hits = new ArrayList<>();
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == letter) {
                hits.add(i);
            }
        }

        // 5) Handle wrong guesses & lives
        if (hits.isEmpty()) {
            rs.wrongCount++;
            StringHolder sh = new StringHolder();
            getSetting("number_of_lives", sh, sessionToken);
            int maxLives = Integer.parseInt(sh.value);

            if (rs.wrongCount >= maxLives) {
                Lobby lobby = lobbies.get(gameToken);
                boolean allLost = true;
                for (RoundState other : roundStates.get(gameToken).values()) {
                    if (other.wrongCount < maxLives) {
                        allLost = false;
                        break;
                    }
                }

                if (allLost) {
                    // No winner this round → broadcast round-end with empty winner
                    for (Map.Entry<String, GameCallBackService> e : lobby.callbacks.entrySet()) {
                        try {
                            e.getValue().notifyRoundEnd(gameToken, e.getKey(), "");
                        } catch (Exception ignored) {}
                    }
                    // If this was final round, schedule game-end after delay
                    int roundNum = currentRoundNumbers.getOrDefault(gameToken, 1);
                    if (roundNum >= lobby.totalRounds) {
                        int delay = lobby.nextRoundDelay;
                        countdownScheduler.schedule(() -> {
                            for (Map.Entry<String, GameCallBackService> e : lobby.callbacks.entrySet()) {
                                try {
                                    e.getValue().notifyGameEnd(gameToken, e.getKey(), "");
                                } catch (Exception ignored) {}
                            }
                            cleanupGame(gameToken);
                        }, delay, TimeUnit.SECONDS);
                    } else {
                        // start next round
                        try {
                            startRound(gameToken, roundNum + 1, playerID, sessionToken);
                        } catch (Exception ignored) {}
                    }
                }
                throw new MaxAttemptsReachedException();
            }
        }

        // 6) Winner check
        boolean allRevealed = true;
        for (char c : word.toCharArray()) {
            if (!rs.guessed.contains(c)) {
                allRevealed = false;
                break;
            }
        }

        if (allRevealed) {
            Lobby lobby = lobbies.get(gameToken);
            int roundNum = currentRoundNumbers.getOrDefault(gameToken, 1);
            String username = lookupUsername(playerID);

            // a) Broadcast ROUND-END to everyone
            for (Map.Entry<String, GameCallBackService> e : lobby.callbacks.entrySet()) {
                try {
                    e.getValue().notifyRoundEnd(gameToken, e.getKey(), username);
                } catch (Exception ex) {
                    System.err.println("[GameService ERROR] notifyRoundEnd to "
                            + e.getKey() + " threw: " + ex);
                }
            }

            // b) Record winner
            roundWinners.put(gameToken + ":" + roundNum, username);

            // c) If not final, start next round
            if (roundNum < lobby.totalRounds) {
                try {
                    startRound(gameToken, roundNum + 1, playerID, sessionToken);
                } catch (Exception ignored) {}
            } else {
                // d) FINAL ROUND → schedule GAME-END after same client delay
                int delay = lobby.nextRoundDelay;
                countdownScheduler.schedule(() -> {
                    for (Map.Entry<String, GameCallBackService> e : lobby.callbacks.entrySet()) {
                        try {
                            e.getValue().notifyGameEnd(gameToken, e.getKey(), username);
                        } catch (Exception ex) {
                            System.err.println("[GameService ERROR] notifyGameEnd to "
                                    + e.getKey() + " threw: " + ex);
                        }
                    }
                    cleanupGame(gameToken);
                }, delay, TimeUnit.SECONDS);
            }

            // e) Return positions so UI can reveal them
            int[] result = new int[hits.size()];
            for (int i = 0; i < hits.size(); i++) result[i] = hits.get(i);
            return result;
        }

        // 7) Default: return any hit positions
        int[] result = new int[hits.size()];
        for (int i = 0; i < hits.size(); i++) result[i] = hits.get(i);
        return result;
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
