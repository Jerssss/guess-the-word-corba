package Server_Java.implementation;

import PlayerGame.*;

public class GameImpl extends GameServicePOA {
    private Game game;

    public GameImpl() {
        game = new Game();
    }

    @Override
    public int startGame(int playerID, String sessionToken) throws NoPlayersAvailableException, GameTimeoutException {
        game.startRound();
        return 0; //TODO: Return game ID or other relevant data
    }

    @Override
    public Round startRound(int gameID, int playerID, String sessionToken) throws GameNotFoundException {
        game.startRound();
        return null; //TODO: Return round details if needed
    }

    @Override
    public String getRandomWord(int gameID, int roundNumber, int playerID, String sessionToken) {
        return game.getRandomWord();
    }

    //this was removed from the IDL and POA bruv might check this again
//    @Override
//    public int getWordLength(String word) {
//        return word.length();
//    }

    @Override
    public int[] guessLetter(int playerID, String sessionToken, char letter) throws MaxAttemptsReachedException, GameNotFoundException, AlreadyGuessedLetterException, NotLoggedInException {
        String result = game.guessLetter(playerID, letter);
        return new int[0];
//        return result.startsWith("Correct") ? 1 : -1; // Return success/failure code
    }

    @Override
    public int getRemainingAttempts(int playerID, String sessionToken) throws NotLoggedInException {
        return 0; //TODO
    }

    @Override
    public int getRemainingWaitingTime(int playerID, String sessionToken) throws GameTimeoutException, NotLoggedInException {
        return 0; //TODO
    }

    @Override
    public int getRemainingRoundTime(int gameID, int playerID, String sessionToken) throws GameTimeoutException, NotLoggedInException {
        return 0; //TODO
    }

    @Override
    public boolean isGameOver(int playerID, String sessionToken) throws NotLoggedInException {
        return false; //TODO
    }

    @Override
    public String getRoundWinner(int gameID, int playerID, String sessionToken) throws NotLoggedInException {
        return null; //TODO
    }

    @Override
    public String getGameWinner(int gameID, int playerID, String sessionToken) throws NotLoggedInException {
        return null; //TODO
    }

    @Override
    public void leaveGame(int playerID, String sessionToken, int gameID) throws NotLoggedInException {
        //TODO
    }

    @Override
    public String[] getLeaderboards(int playerID, String sessionToken) throws NotLoggedInException {
        return new String[0]; //TODO
    }

    public void endRound(int playerID, boolean playerWon) {
        game.endRound(playerWon);
    }

}