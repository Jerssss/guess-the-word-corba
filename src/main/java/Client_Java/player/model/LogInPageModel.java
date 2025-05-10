// File: Client_Java/player/model/LogInPageModel.java
package Client_Java.player.model;

import AuthenticationIDL.*;
import PlayerCallBackIDL.LoginCallbackService;
import org.omg.CORBA.IntHolder;
import Shared_Files.PlayerAccount;

public class LogInPageModel {
    private final AuthenticationService authService;

    public LogInPageModel(AuthenticationService authService) {
        this.authService = authService;
    }

    /**
     * Attempts login; returns LoginResult on success.
     * May throw AuthenticationException or AlreadyLoggedInException.
     */
    public LoginResult login(
            String username,
            String password,
            LoginCallbackService callback
    ) throws AuthenticationException, AlreadyLoggedInException {
        IntHolder idHolder = new IntHolder();
        String token = authService.login(username, password, idHolder, callback);

        PlayerAccount acct = new PlayerAccount(
                idHolder.value,
                username,
                /* omit or mask password */ "",
                0
        );
        return new LoginResult(acct, token);
    }
}
