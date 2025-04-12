package Client_Java.admin.model.implementation;

import GameApp.AlreadyLoggedInException;
import GameApp.AuthServicePOA;
import GameApp.AuthenticationException;
import GameApp.Player;
import Server_Java.database.PlayerQueries;

public class AuthenticationImpl extends AuthServicePOA {
    @Override
    public Player login(String username, String password) throws AuthenticationException, AlreadyLoggedInException {
        return PlayerQueries.login(username, password);
    }

    @Override
    public void logout(int playerId) {
        PlayerQueries.logout(playerId);
    }
}
