package Server_Java.implementation;


import Server_Java.idls.PlayerCallBackIDL.LoginCallbackServicePOA;
import Server_Java.idls.PlayerCallBackIDL.NotLoggedInException;

public class LoginCallBackImpl extends LoginCallbackServicePOA {

    @Override
    public void notifyForcedLogout(int playerID, String sessionToken) throws NotLoggedInException {

    }
}

