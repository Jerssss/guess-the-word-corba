package Server_Java.implementation;

import IDL_Files.AdminIDL.*;

public class AdminServiceImpl extends AdminServicePOA {
    @Override
    public void createPlayer(String username, String password, String sessionToken, int adminID) throws AccountExistsException, NotLoggedInException {

    }

    @Override
    public void modifyPlayer(int playerID, String newPassword, String sessionToken, int adminID) throws AccountNotFoundException, ExistingPasswordException, NotLoggedInException {

    }

    @Override
    public void deletePlayer(int playerID, String sessionToken, int adminID) throws AccountNotFoundException, AccountCurrentlyActiveException, NotLoggedInException {

    }

    @Override
    public String searchPlayer(String username, String sessionToken, int adminID) throws NotLoggedInException, PlayerNotFoundException {
        return "";
    }

    @Override
    public void modifyWaitingTime(int waitTime, String sessionToken, int adminID) throws NotLoggedInException {

    }

    @Override
    public void modifyRoundDuration(int roundTime, String sessionToken, int adminID) throws NotLoggedInException {

    }
}
