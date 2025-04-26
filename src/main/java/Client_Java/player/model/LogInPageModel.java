package Client_Java.player.model;

import Client_Java.PlayerClient_Java;

import IDL_Files.AuthenticationIDL.AuthenticationException;
import IDL_Files.AuthenticationIDL.AlreadyLoggedInException;
import IDL_Files.AuthenticationIDL.AuthenticationService;
import Shared_Files.PlayerAccount;
import org.omg.CORBA.IntHolder;

public class LogInPageModel {
    private final AuthenticationService authService;

    public LogInPageModel(AuthenticationService authService) {
        this.authService = authService;
    }

    public boolean login(String username, String password) {
        try {
            IntHolder playerIdHolder = new IntHolder();
            String sessionToken = authService.login(username, password, playerIdHolder);

            if (sessionToken != null && !sessionToken.isEmpty()) {
                PlayerClient_Java.setSessionToken(sessionToken);
                PlayerClient_Java.setLoggedInPlayerID(playerIdHolder.value);

                PlayerAccount dummy = new PlayerAccount(playerIdHolder.value, username, password, 0);
                PlayerClient_Java.setLoggedInPlayer(dummy);

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
