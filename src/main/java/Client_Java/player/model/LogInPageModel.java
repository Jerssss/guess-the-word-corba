package Client_Java.player.model;

import PlayerGame.AlreadyLoggedInException;
import PlayerGame.AuthService;
import PlayerGame.AuthenticationException;

public class LogInPageModel {
    private final AuthService authService;

    public LogInPageModel(AuthService authService) {
        this.authService = authService;
    }

    public String login(String username, String password, org.omg.CORBA.IntHolder playerID)
            throws AlreadyLoggedInException, AuthenticationException {
        return authService.login(username, password, playerID);
    }
}
