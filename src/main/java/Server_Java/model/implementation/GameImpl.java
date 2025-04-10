package Server_Java.model.implementation;

import GameApp.*;

public class GameImpl extends GameServicePOA {
    private Game game;

    public GameImpl() {
        game = new Game();
    }

    @Override
    public int startGame(int playerID) throws NoPlayersAvailableException, GameTimeoutException {
        game.startRound();
        return 0; //TODO: Return game ID or other relevant data
    }

    @Override
    public Round startRound(int gid) throws GameNotFoundException {
        game.startRound();
        return null; //TODO: Return round details if needed
    }

    @Override
    public String getRandomWord() {
        return game.getRandomWord();
    }

    @Override
    public int getWordLength(String word) {
        return word.length();
    }

    @Override
    public int guessLetter(int playerID, char letter) throws GameNotFoundException {
        String result = game.guessLetter(playerID, letter);
        return result.startsWith("Correct") ? 1 : -1; // Return success/failure code
    }

    @Override
    public String getCurrentLetterState(int playerID) {
        return game.getCurrentLetterState();
    }

    @Override
    public int getRemainingAttempts(int playerID) {
        return game.getRemainingAttempts();
    }

    @Override
    public int getRemainingWaitingTime() throws GameTimeoutException {
        return 0; //TODO: Implement waiting time logic if needed
    }

    @Override
    public int getRemainingRoundTime(int gid) throws GameTimeoutException {
        return 0; //TODO:  Implement round time logic if needed
    }

    @Override
    public boolean isGameOver(int playerID) {
        return false; //TODO: Implement game over logic
    }

    @Override
    public boolean hasWon(int playerID) {
        return false; //TODO: Implement win checking logic
    }

    @Override
    public String getRoundWinner(int gid) {
        return ""; //TODO: Implement round winner logic
    }

    @Override
    public String getGameWinner(int gid) {
        return ""; //TODO: Implement game winner logic
    }

    @Override
    public void leaveGame(int pid, int gid) {
        //TODO: Implement leave game logic
    }

    @Override
    public String[] getLeaderboards() {
        return new String[0]; //TODO: Implement leaderboard logic kaso ang alam ko sa game lobby lang 'to?
    }

    public void endRound(int playerID, boolean playerWon) {
        game.endRound(playerWon);
    }
}