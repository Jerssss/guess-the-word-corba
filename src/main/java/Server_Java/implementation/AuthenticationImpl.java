package Server_Java.implementation;

import PlayerGame.*;
import PlayerGame.AuthServicePOA;
import Server_Java.database.PlayerQueries;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthenticationImpl extends AuthServicePOA {
    // Maps playerID to sessionToken
    private final Map<Long, String> sessionTokens = new HashMap<>();

    @Override
    public synchronized String login(String username, String password, org.omg.CORBA.IntHolder playerID)
            throws AuthenticationException {
        Player player = PlayerQueries.loginAllowDuplicate(username, password); // new method allows duplicate login

        long id = player.playerID;
        playerID.value = (int) id;

        // Invalidate old session if exists
        if (sessionTokens.containsKey(id)) {
            System.out.println("[FORCE LOGOUT] Previous session invalidated for playerID=" + id);
            sessionTokens.remove(id);
            PlayerQueries.logout((int) id); // forcibly log out old session
        }

        // Generate new session token
        String token = UUID.randomUUID().toString();
        sessionTokens.put(id, token);

        System.out.println("Player logged in: ID=" + id + ", Token=" + token);
        return token;
    }

    @Override
    public synchronized void logout(int playerID, String sessionToken) throws NotLoggedInException {
        String storedToken = sessionTokens.get((long) playerID);
        if (storedToken == null || !storedToken.equals(sessionToken)) {
            throw new NotLoggedInException();
        }

        sessionTokens.remove((long) playerID);
        PlayerQueries.logout(playerID);
        System.out.println("Player logged out: ID=" + playerID);
    }
}
