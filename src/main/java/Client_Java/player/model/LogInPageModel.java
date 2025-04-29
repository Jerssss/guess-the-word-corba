// CLIENT-SIDE MVC: Login Page Model and Controller with Forced-Logout Callback Integration

// Updated LogInPageModel.java
package Client_Java.player.model;

import Client_Java.player.PlayerClient_Java;
import Client_Java.player.PlayerClient_Model;
import Server_Java.idls.AuthenticationIDL.AuthenticationException;
import Server_Java.idls.AuthenticationIDL.AlreadyLoggedInException;
import Server_Java.idls.AuthenticationIDL.AuthenticationService;
import Server_Java.idls.PlayerCallBackIDL.LoginCallbackService;
import Shared_Files.PlayerAccount;
import org.omg.CORBA.IntHolder;

public class LogInPageModel {
    private final AuthenticationService authService;

    public LogInPageModel(AuthenticationService authService) {
        this.authService = authService;
    }

    /**
     * Attempts login with callback. Returns true if successful.
     */
    public boolean login(String username, String password, LoginCallbackService callback) {
        try {
            IntHolder playerIdHolder = new IntHolder();
            // Pass the client-side callback stub to the server
            String sessionToken = authService.login(username, password, playerIdHolder, callback);
            if (sessionToken != null && !sessionToken.isEmpty()) {
                // Store session details in client state
                PlayerClient_Java.setSessionToken(sessionToken);
                PlayerClient_Java.setLoggedInPlayerID(playerIdHolder.value);

                PlayerAccount account = new PlayerAccount(
                        playerIdHolder.value, username, password, 0);
                PlayerClient_Java.setLoggedInPlayer(account);

                return true;
            }
        } catch (AuthenticationException e) {
            System.err.println("[Login Failed] " + e.getMessage());
        } catch (AlreadyLoggedInException e) {
            System.err.println("[Login Failed] Account already logged in elsewhere.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
