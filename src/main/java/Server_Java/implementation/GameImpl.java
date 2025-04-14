package Server_Java.implementation;

import PlayerGame.*;

public class GameImpl extends GameServicePOA {
    @Override
    public int startGame(int playerID, String sessionToken) throws NoPlayersAvailableException, GameTimeoutException, NotLoggedInException {
        return 0;
    }

    @Override
    public Round startRound(int gameID, int playerID, String sessionToken) throws GameNotFoundException, NotLoggedInException {
        return null;
    }

    @Override
    public String getRandomWord(int gameID, int roundNumber, int playerID, String sessionToken) throws GameNotFoundException, NotLoggedInException {
        return "";
    }

    @Override
    public int[] guessLetter(int playerID, String sessionToken, char letter) throws MaxAttemptsReachedException, GameNotFoundException, AlreadyGuessedLetterException, NotLoggedInException {
        return new int[0];
    }

    @Override
    public int getRemainingAttempts(int playerID, String sessionToken) throws NotLoggedInException {
        return 0;
    }

    @Override
    public int getRemainingWaitingTime(int playerID, String sessionToken) throws GameTimeoutException, NotLoggedInException {
        return 0;
    }

    @Override
    public int getRemainingRoundTime(int gameID, int playerID, String sessionToken) throws GameTimeoutException, NotLoggedInException {
        return 0;
    }

    @Override
    public boolean isGameOver(int playerID, String sessionToken) throws NotLoggedInException {
        return false;
    }

    @Override
    public String getRoundWinner(int gameID, int playerID, String sessionToken) throws NotLoggedInException {
        return "";
    }

    @Override
    public String getGameWinner(int gameID, int playerID, String sessionToken) throws NotLoggedInException {
        return "";
    }

    @Override
    public void leaveGame(int playerID, String sessionToken, int gameID) throws NotLoggedInException {

    }

    @Override
    public String[] getLeaderboards(int playerID, String sessionToken) throws NotLoggedInException {
        return new String[0];
    }
//    private Game game;
//
//    public GameImpl() {
//        game = new Game();
//    }
//
//    @Override
//    public int startGame(int playerID) throws NoPlayersAvailableException, GameTimeoutException {
//        game.startRound();
//        return 0; //TODO: Return game ID or other relevant data
//    }
//
//    @Override
//    public Round startRound(int gid) throws GameNotFoundException {
//        game.startRound();
//        return null; //TODO: Return round details if needed
//    }
//
//    @Override
//    public String getRandomWord() {
//        return game.getRandomWord();
//    }
//
//    @Override
//    public int getWordLength(String word) {
//        return word.length();
//    }
//
//    @Override
//    public int guessLetter(int playerID, char letter) throws GameNotFoundException {
//        String result = game.guessLetter(playerID, letter);
//        return result.startsWith("Correct") ? 1 : -1; // Return success/failure code
//    }
//
//    @Override
//    public String getCurrentLetterState(int playerID) {
//        return game.getCurrentLetterState();
//    }
//
//    @Override
//    public int getRemainingAttempts(int playerID) {
//        return game.getRemainingAttempts();
//    }
//
//    @Override
//    public int getRemainingWaitingTime() throws GameTimeoutException {
//        return 0; //TODO: Implement waiting time logic if needed
//    }
//
//    @Override
//    public int getRemainingRoundTime(int gid) throws GameTimeoutException {
//        return 0; //TODO:  Implement round time logic if needed
//    }
//
//    @Override
//    public boolean isGameOver(int playerID) {
//        return false; //TODO: Implement game over logic
//    }
//
//    @Override
//    public boolean hasWon(int playerID) {
//        return false; //TODO: Implement win checking logic
//    }
//
//    @Override
//    public String getRoundWinner(int gid) {
//        return ""; //TODO: Implement round winner logic
//    }
//
//    @Override
//    public String getGameWinner(int gid) {
//        return ""; //TODO: Implement game winner logic
//    }
//
//    @Override
//    public void leaveGame(int pid, int gid) {
//        //TODO: Implement leave game logic
//    }
//
//    @Override
//    public String[] getLeaderboards() {
//        return new String[0]; //TODO: Implement leaderboard logic kaso ang alam ko sa game lobby lang 'to?
//    }
//
//    public void endRound(int playerID, boolean playerWon) {
//        game.endRound(playerWon);
//    }
}