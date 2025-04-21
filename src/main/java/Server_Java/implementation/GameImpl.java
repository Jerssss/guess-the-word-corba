package Server_Java.implementation;

import PlayerGame.*;

public class GameImpl extends GameServicePOA {
    private Game game;

    public GameImpl() {
        game = new Game();
    }

    @Override
    public int startGame(int playerID, String sessionToken) throws NoPlayersAvailableException, GameTimeoutException, NotLoggedInException {
        game.startRound();
        return 0; // Return game ID or other relevant data
    }

    @Override
    public Round startRound(int gameID, int playerID, String sessionToken) throws GameNotFoundException, NotLoggedInException {
        game.startRound();
        return null; // Return round details if needed
    }

    @Override
    public String getRandomWord(int gameID, int roundNumber, int playerID, String sessionToken) throws GameNotFoundException, NotLoggedInException {
        return game.getRandomWord();
    }

    @Override
    public int[] guessLetter(int playerID, String sessionToken, char letter) throws MaxAttemptsReachedException, GameNotFoundException, AlreadyGuessedLetterException, NotLoggedInException {
        String result = game.guessLetter(playerID, letter);
        return result.startsWith("Correct") ? new int[]{1} : new int[]{-1}; // Return success/failure code
    }

    @Override
    public int getRemainingAttempts(int playerID, String sessionToken) throws NotLoggedInException {
        return game.getRemainingAttempts();
    }

    @Override
    public int getRemainingWaitingTime(int playerID, String sessionToken) throws GameTimeoutException, NotLoggedInException {
        return 0; // Implement waiting time logic if needed
    }

    @Override
    public int getRemainingRoundTime(int gameID, int playerID, String sessionToken) throws GameTimeoutException, NotLoggedInException {
        return game.getRemainingRoundTime();
    }

    @Override
    public boolean isGameOver(int playerID, String sessionToken) throws NotLoggedInException {
        return game.getRemainingAttempts() <= 0 || game.getRemainingRoundTime() <= 0;
    }

    @Override
    public String getRoundWinner(int gameID, int playerID, String sessionToken) throws NotLoggedInException {
        return ""; // Implement round winner logic
    }

    @Override
    public String getGameWinner(int gameID, int playerID, String sessionToken) throws NotLoggedInException {
        return ""; // Implement game winner logic
    }

    @Override
    public void leaveGame(int playerID, String sessionToken, int gameID) throws NotLoggedInException {
        // Implement leave game logic
    }

    @Override
    public String[] getLeaderboards(int playerID, String sessionToken) throws NotLoggedInException {
        return new String[0]; // Implement leaderboard logic
    }

    @Override
    public int getNumberOfPlayersJoined(int playerID, String sessionToken) throws NotLoggedInException {
        return 0;
    }

    public void endRound(int playerID, boolean playerWon) {
        game.endRound(playerWon);
    }
}