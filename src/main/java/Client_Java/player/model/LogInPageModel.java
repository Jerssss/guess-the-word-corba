package Client_Java.player.model;

import GameApp.AlreadyLoggedInException;
import GameApp.AuthService;
import GameApp.AuthenticationException;
import GameApp.Player;

public class LogInPageModel {
    private final AuthService authService;

    public LogInPageModel(AuthService authService) {
        this.authService = authService;
    }

    public Player login(String username, String password) throws AlreadyLoggedInException, AuthenticationException{
        return Client_Model.authService.login(username,password);
    }
}
