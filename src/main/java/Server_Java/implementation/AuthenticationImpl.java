package Server_Java.implementation;

import PlayerGame.AlreadyLoggedInException;
import PlayerGame.AuthServicePOA;
import PlayerGame.AuthenticationException;
import PlayerGame.Player;
import Server_Java.database.PlayerQueries;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthenticationImpl extends AuthServicePOA {
    // Store active sessions (playerID -> token)
    private static final ConcurrentHashMap<Integer, String> activeSessions = new ConcurrentHashMap<>();
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    @Override
    public String login(String username, String password, org.omg.CORBA.IntHolder playerID)
            throws AuthenticationException, AlreadyLoggedInException {

        //authenticate
        Player player = PlayerQueries.login(username, password);

        //login check
        if (activeSessions.containsKey(player.playerID)) {
            throw new AlreadyLoggedInException();
        }

        //generate token
        String token = generateUniqueToken();

        //store session
        activeSessions.put(player.playerID, token);
        playerID.value = player.playerID;

        return token;
    }

    @Override
    public void logout(int playerID, String sessionToken) {
        if (sessionToken.equals(activeSessions.get(playerID))) {
            activeSessions.remove(playerID);
        }
        PlayerQueries.logout(playerID);
    }

    //unique
    public static String generateUniqueToken() {
        return UUID.randomUUID().toString();
    }

    //secure (a choice lang this looks kinda nerdy too im ngl)
    public static String generateSecureToken() {
        byte[] randomBytes = new byte[24];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    // Validation method for other services to use
    public static boolean validateSession(int playerID, String token) {
        return token != null && token.equals(activeSessions.get(playerID));
    }
}