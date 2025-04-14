package Server_Java.implementation;

import PlayerGame.AlreadyLoggedInException;
import PlayerGame.AuthServicePOA;
import PlayerGame.AuthenticationException;
import PlayerGame.Player;
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
