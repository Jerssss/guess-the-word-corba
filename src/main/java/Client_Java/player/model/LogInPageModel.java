package Client_Java.player.model;

import PlayerGame.AlreadyLoggedInException;
import PlayerGame.AuthService;
import PlayerGame.AuthenticationException;
import PlayerGame.Player;

public class LogInPageModel {
    private final AuthService authService;

    public LogInPageModel(AuthService authService) {
        this.authService = authService;
    }

    public Player login(String username, String password) throws AlreadyLoggedInException, AuthenticationException{
        return PlayerClient_Model.authService.login(username,password);
    }
}
