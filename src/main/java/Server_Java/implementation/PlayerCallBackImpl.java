package Server_Java.implementation;

import IDL_Files.PlayerCallBackIDL.NotLoggedInException;
import IDL_Files.PlayerCallBackIDL.PlayerCallBackServicePOA;

public class PlayerCallBackImpl extends PlayerCallBackServicePOA {
    @Override
    public void notifyRoundEnd(String gameToken, String sessionToken, String result) {

    }

    @Override
    public void notifyGameEnd(String gameToken, String sessionToken, String result) {

    }

    @Override
    public void notifyForcedLogout(int playerID, String sessionToken) throws NotLoggedInException {

    }
}
