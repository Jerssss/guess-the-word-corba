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
    public String login(String username, String password, org.omg.CORBA.IntHolder playerID)
            throws AuthenticationException, AlreadyLoggedInException {
        Player player = PlayerQueries.login(username, password); // throws if not found or already logged in
        if (sessionTokens.containsKey(player.playerID)) {
            throw new AlreadyLoggedInException();
        }

        String token = UUID.randomUUID().toString();
        sessionTokens.put(Long.valueOf(player.playerID), token);
        playerID.value = player.playerID;

        // Print session token to server console
        System.out.println("Player logged in: ID=" + player.playerID + ", Token=" + token);

        return token;
    }

    @Override
    public void logout(int playerID, String sessionToken) throws NotLoggedInException {
        String storedToken = sessionTokens.get((long) playerID); // key is long
        if (storedToken == null || !storedToken.equals(sessionToken)) {
            throw new NotLoggedInException();
        }
        sessionTokens.remove((long) playerID);
        PlayerQueries.logout(playerID);

        // Optional: Print logout info
        System.out.println("Player logged out: ID=" + playerID);
    }
}
