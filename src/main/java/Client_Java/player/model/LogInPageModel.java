package Client_Java.player.model;

import PlayerGame.AlreadyLoggedInException;
import PlayerGame.AuthService;
import PlayerGame.AuthenticationException;
import org.omg.CORBA.IntHolder;

public class LogInPageModel {
    private final AuthService authService;

    public LogInPageModel(AuthService authService) {
        if (authService == null) {
            throw new IllegalArgumentException("AuthService cannot be null");
        }
        this.authService = authService;
    }

    public String login(String username, String password, IntHolder playerID)
            throws AlreadyLoggedInException, AuthenticationException {
        try {
            return authService.login(username, password, playerID);
        } catch (Exception e) {
            System.err.println("[ERROR] Login failed: " + e.getMessage());
            throw e;
        }
    }
}