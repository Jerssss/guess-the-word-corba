package Client_Java.admin.model;

import Client_Java.admin.AdminClient_Java;

import Server_Java.idls.AuthenticationIDL.AlreadyLoggedInException;
import Server_Java.idls.AuthenticationIDL.AuthenticationException;
import Server_Java.idls.AuthenticationIDL.AuthenticationService;
import Shared_Files.AdminAccount;
import org.omg.CORBA.IntHolder;

public class AdminLogInPageModel {
    private final AuthenticationService authService;

    public AdminLogInPageModel(AuthenticationService authService) {
        this.authService = authService;
    }

    public boolean login(String username, String password) {
        try {
            IntHolder adminIdHolder = new IntHolder();
            String sessionToken = authService.adminLogin(username, password, adminIdHolder);

            if (sessionToken != null && !sessionToken.isEmpty()) {
                AdminClient_Java.setSessionToken(sessionToken);
                AdminClient_Java.setLoggedInAdminID(adminIdHolder.value);

                AdminAccount temp = new AdminAccount(adminIdHolder.value, username, password);
                AdminClient_Java.setLoggidInAdmin(temp);

                return true;
            } else {
                return false;
            }
        } catch (AuthenticationException e) {
            System.err.println("[Login Failed] " + e.getMessage());
            return false;
        } catch (AlreadyLoggedInException e) {
            System.err.println("[Login] Already logged in somewhere else!");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

