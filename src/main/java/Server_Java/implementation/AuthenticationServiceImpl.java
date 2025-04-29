package Server_Java.implementation;


import Server_Java.idls.AuthenticationIDL.AlreadyLoggedInException;
import Server_Java.idls.AuthenticationIDL.AuthenticationException;
import Server_Java.idls.AuthenticationIDL.AuthenticationServicePOA;
import Server_Java.idls.AuthenticationIDL.NotLoggedInException;
import Server_Java.idls.PlayerCallBackIDL.LoginCallbackService;
import org.omg.CORBA.IntHolder;

public class AuthenticationServiceImpl extends AuthenticationServicePOA {


    @Override
    public String login(String username, String password, IntHolder playerID, LoginCallbackService cb) throws AuthenticationException, AlreadyLoggedInException {
        return "";
    }

    @Override
    public void logout(int playerID, String sessionToken) throws NotLoggedInException {

    }

    @Override
    public String adminLogin(String username, String password, IntHolder adminID) throws AuthenticationException, AlreadyLoggedInException {
        return "";
    }

    @Override
    public void adminLogout(int adminID, String sessionToken) throws NotLoggedInException {

    }
}
