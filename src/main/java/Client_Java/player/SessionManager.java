// File: Client_Java/player/SessionManager.java
package Client_Java.player;

import Server_Java.idls.AuthenticationIDL.AuthenticationService;
import Server_Java.idls.GameIDL.GameService;
import Shared_Files.PlayerAccount;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

public class SessionManager {
    private static String gameToken;
    private static String sessionToken;
    private static PlayerAccount loggedInPlayer;
    private static ORB orb;
    private static POA rootPoa;
    private static GameService gameService;
    private static AuthenticationService authService;  // ← new

    public static void initOrb(ORB orbRef, POA poaRef) {
        orb     = orbRef;
        rootPoa = poaRef;
    }
    public static ORB getOrb() { return orb; }
    public static POA getPoa() { return rootPoa; }

    public static void setSessionToken(String token) {
        sessionToken = token;
    }
    public static String getSessionToken() {
        return sessionToken;
    }

    public static void setGameToken(String token) {
        gameToken = token;
    }
    public static String getGameToken() {
        return gameToken;
    }

    public static void setLoggedInPlayer(PlayerAccount acct) {
        loggedInPlayer = acct;
    }
    public static PlayerAccount getLoggedInPlayer() {
        return loggedInPlayer;
    }

    public static void setGameService(GameService svc) {
        gameService = svc;
    }
    public static GameService getGameService() {
        return gameService;
    }

    /**
     * Set and get methods for your CORBA AuthenticationService
     */
    public static void setAuthService(AuthenticationService svc) {
        authService = svc;
    }
    public static AuthenticationService getAuthService() {
        return authService;
    }
}
