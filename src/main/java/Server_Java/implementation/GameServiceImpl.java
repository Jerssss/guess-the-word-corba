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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

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
    private static final Map<String, String> roundWords = new ConcurrentHashMap<>();
    private static final Map<String, Map<Integer, RoundState>> roundStates = new ConcurrentHashMap<>();
    private static final Map<String,Integer> currentRoundNumbers = new ConcurrentHashMap<>();
    private static final Map<String,String>  roundWinners       = new ConcurrentHashMap<>();
    // — NEW: scheduler to delay countdown notifications —
    private static final ScheduledExecutorService countdownScheduler =
            Executors.newSingleThreadScheduledExecutor();
    // Tracks pending countdown tasks by lobby token
    private static final Map<String, ScheduledFuture<?>> pendingCountdowns =
            new ConcurrentHashMap<>();
    private static final Random RAND = new Random();

// 1) Store a map of playerID→RoundState for each gameToken:
// new: per-game → per-player RoundState


    /**
     * Represents a lobby waiting to start:
     * - players: the playerIDs joined
     * - callbacks: stubs to notify when game really starts
     * - waitingCallbacks: stubs to notify on join/countdown/reset
     */
    // in GameServiceImpl.java

// 1) Update your Lobby class to hold every setting:
    private static class Lobby {
        final String token;
        final int    lobbyWaitingTime;    // seconds to wait before force‐starting
        final int    roundDuration;       // seconds per round
        final int    nextRoundDelay;      // seconds between rounds
        final int    countdownSeconds;    // seconds countdown before start
        final int    totalRounds;         // how many rounds in the game
        final int    minimumPlayers;      // players needed to start
        final int    numberOfLives;       // wrong‐guess lives per player

        final List<Integer> players           = new CopyOnWriteArrayList<>();
        final List<GameCallBackService> callbacks         = new CopyOnWriteArrayList<>();
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
            this.token             = token;
            this.lobbyWaitingTime  = lobbyWaitingTime;
            this.roundDuration     = roundDuration;
            this.nextRoundDelay    = nextRoundDelay;
            this.countdownSeconds  = countdownSeconds;
            this.totalRounds       = totalRounds;
            this.minimumPlayers    = minimumPlayers;
            this.numberOfLives     = numberOfLives;
        }
    }
    private static class RoundState {
        /** The secret word for this round (never exposed directly to clients) */
        final String word;

        /** All letters that have been guessed so far */
        final Set<Character> guessed = ConcurrentHashMap.newKeySet();

        /** How many incorrect guesses so far */
        int wrongCount = 0;

        /** Create a new round with the given secret word */
        RoundState(String word) {
            this.word = word;
        }
    }


    // in your GameServiceImpl class‐scope, once:
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
            throws NotLoggedInException
    {
        if (sessionToken == null) {
            throw new NotLoggedInException();
        }

        // 1) If this session is already in a lobby, return it
        if (sessionToGame.containsKey(sessionToken)) {
            String existingToken = sessionToGame.get(sessionToken);
            debugPrintAllLobbies("joinLobby (already in)");
            return existingToken;
        }

        // 2) Try to find a lobby that hasn’t filled yet
        Lobby target = null;
        for (Lobby l : lobbies.values()) {
            if (l.players.size() < l.minimumPlayers) {
                target = l;
                break;
            }
        }

        // 3) If none found, create a new one and snapshot all settings
        if (target == null) {
            // Helper to fetch an integer setting
            Function<String,Integer> fetchInt = key -> {
                StringHolder sh = new StringHolder();
                getSetting(key, sh, sessionToken);
                return Integer.parseInt(sh.value);
            };

            int lobbyWaitingTime  = fetchInt.apply("lobby_waiting_time");
            int roundDuration     = fetchInt.apply("round_duration");
            int nextRoundDelay    = fetchInt.apply("next_round_delay");
            int countdownSeconds  = fetchInt.apply("countdown_to_game_start");
            int totalRounds       = fetchInt.apply("total_rounds");
            int minimumPlayers    = fetchInt.apply("minimum_players");
            int numberOfLives     = fetchInt.apply("number_of_lives");

            String newToken = UUID.randomUUID().toString();
            target = new Lobby(
                    newToken,
                    lobbyWaitingTime,
                    roundDuration,
                    nextRoundDelay,
                    countdownSeconds,
                    totalRounds,
                    minimumPlayers,
                    numberOfLives
            );
            lobbies.put(newToken, target);
            System.out.println("[GameService DEBUG] Created lobby=" + newToken +
                    " [wait=" + lobbyWaitingTime +
                    ", roundDur=" + roundDuration +
                    ", nextDelay=" + nextRoundDelay +
                    ", countdown=" + countdownSeconds +
                    ", totalRounds=" + totalRounds +
                    ", minPlayers=" + minimumPlayers +
                    ", lives=" + numberOfLives + "]");
        }

        // 4) Add the player if not already present
        if (!target.players.contains(playerID)) {
            target.players.add(playerID);
            sessionToGame.put(sessionToken, target.token);
            System.out.println("[GameService DEBUG] joinLobby: player=" + playerID +
                    " joined lobby=" + target.token +
                    " (count=" + target.players.size() + ")");
        }

        // 5) Notify all waiting-room callbacks of the new player count
        for (WaitingRoomGameCallbackService cb : target.waitingCallbacks) {
            try {
                cb.notifyPlayerJoined(
                        target.token,
                        target.players.size(),
                        sessionToken
                );
            } catch (Exception ignored) {}
        }

        // 6) If we've just hit the minimum, schedule the countdown-start notification
        if (target.players.size() == target.minimumPlayers) {
            // Cancel any previously scheduled countdown for this lobby
            ScheduledFuture<?> oldTask = pendingCountdowns.remove(target.token);
            if (oldTask != null) {
                oldTask.cancel(false);
            }

            // Schedule notifyCountdownStart after a brief delay
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

        // 7) Debug dump and return
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
    public synchronized int startRound(
            String gameToken,
            int roundNumber,
            int playerID,
            String sessionToken
    ) throws GameNotFoundException, NotLoggedInException {

        Lobby lobby = lobbies.get(gameToken);
        if (lobby == null) throw new GameNotFoundException();

        // 0) Update and log the current round number
        currentRoundNumbers.put(gameToken, roundNumber);
        System.out.println("[GameService DEBUG] startRound(): gameToken="
                + gameToken + ", roundNumber=" + roundNumber);

        // 1) Pick & store the new word
        String currentWord = WORDS.get(RAND.nextInt(WORDS.size())).toUpperCase();
        roundWords.put(gameToken, currentWord);
        System.out.println("[GameService DEBUG]   new secret word chosen (length="
                + currentWord.length() + ")");

        // 2) Reset per-player state for all
        Map<Integer, RoundState> perPlayerMap =
                roundStates.computeIfAbsent(gameToken, t -> new ConcurrentHashMap<>());
        perPlayerMap.clear();
        for (Integer pid : lobby.players) {
            perPlayerMap.put(pid, new RoundState(currentWord));
        }
        System.out.println("[GameService DEBUG]   per-player RoundState initialized for "
                + perPlayerMap.size() + " players");

        // 3) Broadcast start to all callbacks
        for (GameCallBackService cb : lobby.callbacks) {
            try {
                System.out.println("[GameService DEBUG]   -> cb.notifyRoundStart(stub="
                        + cb + ")");
                cb.notifyRoundStart(gameToken, roundNumber, sessionToken);
                System.out.println("[GameService DEBUG]   <- returned notifyRoundStart");
            } catch (Exception e) {
                System.err.println("[GameService ERROR] notifyRoundStart threw: " + e);
            }
        }

        // 4) Schedule a timeout to fire after the roundDuration
        countdownScheduler.schedule(
                () -> handleRoundTimeout(gameToken, roundNumber),
                lobby.roundDuration,
                TimeUnit.SECONDS
        );
        System.out.println("[GameService DEBUG]   timeout scheduled in "
                + lobby.roundDuration + "s");

        return lobby.players.size();
    }


    /**
     * Invoked by the scheduler if a round’s timer expires.
     */
    private synchronized void handleRoundTimeout(String gameToken, int roundNumber) {
        Integer current = currentRoundNumbers.get(gameToken);
        if (current == null || current != roundNumber) return;

        Lobby lobby = lobbies.get(gameToken);
        if (lobby == null) return;

        int totalRounds = lobby.totalRounds;

        // If someone already won this round, do nothing
        String key = gameToken + ":" + roundNumber;
        if (roundWinners.containsKey(key)) return;

        if (roundNumber >= totalRounds) {
            // LAST ROUND TIMED OUT → end game with no winner
            System.out.println("[GameService DEBUG] Timeout: final‐round reached for gameToken="
                    + gameToken + "; broadcasting notifyGameEnd(no‐winner)");
            for (GameCallBackService cb : lobby.callbacks) {
                try {
                    System.out.println("[GameService DEBUG]   -> calling cb.notifyGameEnd() stub=" + cb);
                    cb.notifyGameEnd(gameToken, "", "");
                    System.out.println("[GameService DEBUG]   <- returned from notifyGameEnd()");
                } catch (Exception e) {
                    System.err.println("[GameService ERROR] notifyGameEnd threw: " + e);
                }
            }
            // Clean up
            lobbies.remove(gameToken);
            sessionToGame.values().removeIf(t -> t.equals(gameToken));
            sessionToCallback.keySet().removeIf(st -> !sessionToGame.containsKey(st));
            sessionToWaitingCallback.keySet().removeIf(st -> !sessionToGame.containsKey(st));
        } else {
            // INTERMEDIATE ROUND TIMED OUT → no-winner round end + next round
            System.out.println("[GameService DEBUG] Timeout: intermediate‐round " + roundNumber
                    + " for gameToken=" + gameToken + "; broadcasting notifyRoundEnd(no‐winner)");
            for (GameCallBackService cb : lobby.callbacks) {
                try {
                    cb.notifyRoundEnd(gameToken, "", "");
                } catch (Exception ignored) {}
            }
            int nextRound = roundNumber + 1;
            // Kick off next round via any player/session in this lobby
            if (!lobby.players.isEmpty()) {
                int anyPlayer = lobby.players.get(0);
                String anySession = sessionToGame.entrySet().stream()
                        .filter(e -> e.getValue().equals(gameToken))
                        .map(Map.Entry::getKey)
                        .findFirst().orElse(null);
                if (anySession != null) {
                    try {
                        startRound(gameToken, nextRound, anyPlayer, anySession);
                    } catch (Exception ignore) {}
                }
            }
        }
    }



    @Override
    public String getRandomWord(
            String gameToken,
            int roundNumber,
            int playerID,
            String sessionToken
    ) throws GameNotFoundException, NotLoggedInException {
        // 1) Verify the player has a RoundState
        Map<Integer, RoundState> perPlayer = roundStates.get(gameToken);
        if (perPlayer == null || !perPlayer.containsKey(playerID)) {
            throw new GameNotFoundException();
        }

        // 2) Pull the shared word (we already upper-cased it in startRound)
        String word = roundWords.get(gameToken);
        if (word == null) throw new GameNotFoundException();

        // 3) Mask it out
        char[] mask = new char[word.length()];
        Arrays.fill(mask, '_');
        return new String(mask);
    }




    @Override
    public int[] guessLetter(
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

        // 3) Shared word
        String word = roundWords.get(gameToken);
        if (word == null) throw new GameNotFoundException();

        // 4) Find any hits
        List<Integer> hits = new ArrayList<>();
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == letter) hits.add(i);
        }

        // 5) Wrong-guess handling
        if (hits.isEmpty()) {
            rs.wrongCount++;
            StringHolder sh = new StringHolder();
            getSetting("number_of_lives", sh, sessionToken);
            int maxLives = Integer.parseInt(sh.value);

            if (rs.wrongCount >= maxLives) {
                // This player just died—check if _all_ are dead:
                Lobby lobby = lobbies.get(gameToken);
                boolean allLost = true;
                for (int pid : lobby.players) {
                    RoundState other = perPlayer.get(pid);
                    if (other.wrongCount < maxLives) {
                        allLost = false;
                        break;
                    }
                }

                if (allLost) {
                    System.out.println("[GameService DEBUG] All players out in round; broadcasting notifyRoundEnd(no‐winner)");
                    for (GameCallBackService cb : lobby.callbacks) {
                        try { cb.notifyRoundEnd(gameToken, sessionToken, ""); }
                        catch (Exception ignore) {}
                    }
                    // Auto-start next (or end) round
                    int roundNum    = currentRoundNumbers.getOrDefault(gameToken, 1);
                    int totalRounds = lobby.totalRounds;
                    int nextRound   = roundNum + 1;
                    if (roundNum < totalRounds) {
                        try {
                            startRound(gameToken, nextRound, playerID, sessionToken);
                        } catch (Exception ignore) {}
                    } else {
                        System.out.println("[GameService DEBUG] Last round ended with no winner; broadcasting notifyGameEnd(no‐winner)");
                        for (GameCallBackService cb : lobby.callbacks) {
                            try { cb.notifyGameEnd(gameToken, sessionToken, ""); }
                            catch (Exception ignore) {}
                        }
                        // Clean up
                        lobbies.remove(gameToken);
                        sessionToGame.values().removeIf(t -> t.equals(gameToken));
                        sessionToCallback.keySet().removeIf(st -> !sessionToGame.containsKey(st));
                        sessionToWaitingCallback.keySet().removeIf(st -> !sessionToGame.containsKey(st));
                    }
                }

                // Finally, signal this client is out
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
            int roundNum = currentRoundNumbers.getOrDefault(gameToken, 1);
            String key   = gameToken + ":" + roundNum;
            String username = lookupUsername(playerID);
            roundWinners.put(key, username);

            Lobby lobby = lobbies.get(gameToken);
            int totalRounds = lobby.totalRounds;

            if (roundNum >= totalRounds) {
                // FINAL ROUND: broadcast game end
                System.out.println("[GameService DEBUG] Final‐round win detected for gameToken="
                        + gameToken + ", winner=" + username);
                for (GameCallBackService cb : lobby.callbacks) {
                    try {
                        System.out.println("[GameService DEBUG]   -> calling cb.notifyGameEnd(stub=" + cb + ")");
                        cb.notifyGameEnd(gameToken, sessionToken, username);
                        System.out.println("[GameService DEBUG]   <- returned from notifyGameEnd()");
                    } catch (Exception e) {
                        System.err.println("[GameService ERROR] notifyGameEnd threw: " + e);
                    }
                }
                // Clean up
                lobbies.remove(gameToken);
                sessionToGame.values().removeIf(t -> t.equals(gameToken));
                sessionToCallback.keySet().removeIf(st -> !sessionToGame.containsKey(st));
                sessionToWaitingCallback.keySet().removeIf(st -> !sessionToGame.containsKey(st));
            } else {
                // INTERMEDIATE ROUND: broadcast round end, then next round
                System.out.println("[GameService DEBUG] Round " + roundNum + " won by " + username
                        + "; broadcasting notifyRoundEnd and kicking off next round");
                for (GameCallBackService cb : lobby.callbacks) {
                    try { cb.notifyRoundEnd(gameToken, sessionToken, username); }
                    catch (Exception ignore) {}
                }
                int nextRound = roundNum + 1;
                try {
                    startRound(gameToken, nextRound, playerID, sessionToken);
                } catch (Exception ignore) {}
            }
        }

        // 7) Return the hit positions
        return hits.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Helper to map playerID → username (used when recording winners).
     * You can cache this or fetch each time.
     */
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
        // (optional) verify sessionToken validity here
        int roundNum = currentRoundNumbers.getOrDefault(gameToken, 1);
        String key   = gameToken + ":" + roundNum;
        return roundWinners.getOrDefault(key, "Unknown");
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
