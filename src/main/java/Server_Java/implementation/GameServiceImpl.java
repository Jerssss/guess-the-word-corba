package Server_Java.implementation;

import IDL_Files.GameIDL.*;

public class GameServiceImpl extends GameServicePOA {
    @Override
    public String getDisplayName(String username, String sessionToken) throws NotLoggedInException {
        return "";
    }

    @Override
    public String[] getLeaderboards(int playerID, String sessionToken) throws NotLoggedInException {
        return new String[0];
    }

    @Override
    public String joinWaitingLobby(int playerID, String sessionToken) throws NotLoggedInException {
        return "";
    }

    @Override
    public void registerCallBack(int playerID, String gameToken, String sessionToken) throws NotLoggedInException {

    }

    @Override
    public String getWaitingLobbyStatus(String gameToken) throws NotLoggedInException, GameTimeOutException, NotEnoughPlayersException {
        return "";
    }

    @Override
    public int getNumberOfPlayersJoined(int playerID, String sessionToken) throws NotLoggedInException {
        return 0;
    }

    @Override
    public int startGame(int playerID, String sessionToken) throws NotEnoughPlayersException {
        return 0;
    }

    @Override
    public int startRound(String gameToken, int playerID, String sessionToken) throws GameNotFoundException, NotLoggedInException {
        return 0;
    }

    @Override
    public String getRandomWord(String gameToken, int roundNumber, int playerID, String sessionToken) throws GameNotFoundException, NotLoggedInException {
        return "";
    }

    @Override
    public int[] guessLetter(String gameToken, int playerID, String sessionToken, char letter) throws MaxAttemptsReachedException, GameNotFoundException, AlreadyGuessedLetterException, NotLoggedInException {
        return new int[0];
    }

    @Override
    public int getRemainingWaitingTime(String gameToken, int playerID, String sessionToken) throws GameTimeOutException, NotLoggedInException {
        return 0;
    }

    @Override
    public int getRemainingRoundTime(String gameToken, int playerID, String sessionToken) throws GameTimeOutException, NotLoggedInException {
        return 0;
    }

    @Override
    public String getRoundWinner(String gameToken, int playerID, String sessionToken) throws NotLoggedInException {
        return "";
    }

    @Override
    public String getGameWinner(String gameToken, int playerID, String sessionToken) throws NotLoggedInException {
        return "";
    }

    @Override
    public int getPlayerWins(String gameToken, int playerID, String sessionToken) throws NotLoggedInException {
        return 0;
    }
}
