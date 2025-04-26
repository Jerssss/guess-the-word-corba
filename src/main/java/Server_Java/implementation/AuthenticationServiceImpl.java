package Server_Java.implementation;

import IDL_Files.AuthenticationIDL.AlreadyLoggedInException;
import IDL_Files.AuthenticationIDL.AuthenticationException;
import IDL_Files.AuthenticationIDL.AuthenticationServicePOA;
import IDL_Files.AuthenticationIDL.NotLoggedInException;
import org.omg.CORBA.IntHolder;

public class AuthenticationServiceImpl extends AuthenticationServicePOA {
    @Override
    public String login(String username, String password, IntHolder playerID) throws AuthenticationException, AlreadyLoggedInException {
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
