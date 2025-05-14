// File: Client_Java/player/SessionManager.java
package Client_Java.player;

import AuthenticationIDL.AuthenticationService;
import GameIDL.GameService;
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
    private static AuthenticationService authService;

    // --------------------
    // ORB & POA management
    // --------------------
    public static synchronized void initOrb(ORB orbRef, POA poaRef) {
        System.out.println("[DEBUG][SessionManager] initOrb()");
        orb = orbRef;
        rootPoa = poaRef;
    }
    public static synchronized ORB getOrb() {
        System.out.println("[DEBUG][SessionManager] getOrb() -> " + orb);
        return orb;
    }
    public static synchronized POA getPoa() {
        System.out.println("[DEBUG][SessionManager] getPoa() -> " + rootPoa);
        return rootPoa;
    }

    // ---------------
    // Session Token
    // ---------------
    public static synchronized void setSessionToken(String token) {
        System.out.println("[DEBUG][SessionManager] setSessionToken(" + token + ")");
        sessionToken = token;
    }
    public static synchronized String getSessionToken() {
        System.out.println("[DEBUG][SessionManager] getSessionToken() -> " + sessionToken);
        return sessionToken;
    }

    // ------------
    // Game Token
    // ------------
    public static synchronized void setGameToken(String token) {
        System.out.println("[DEBUG][SessionManager] setGameToken(" + token + ")");
        gameToken = token;
    }
    public static synchronized String getGameToken() {
        System.out.println("[DEBUG][SessionManager] getGameToken() -> " + gameToken);
        return gameToken;
    }

    // -------------------
    // Logged-in Player
    // -------------------
    public static synchronized void setLoggedInPlayer(PlayerAccount acct) {
        String user = (acct != null ? acct.getUsername() : "null");
        System.out.println("[DEBUG][SessionManager] setLoggedInPlayer(" + user + ")");
        loggedInPlayer = acct;
    }
    public static synchronized PlayerAccount getLoggedInPlayer() {
        System.out.println("[DEBUG][SessionManager] getLoggedInPlayer() -> " +
                (loggedInPlayer != null ? loggedInPlayer.getUsername() : "null"));
        return loggedInPlayer;
    }

    // ----------------
    // Game Service
    // ----------------
    public static synchronized void setGameService(GameService svc) {
        System.out.println("[DEBUG][SessionManager] setGameService(" + svc + ")");
        gameService = svc;
    }
    public static synchronized GameService getGameService() {
        System.out.println("[DEBUG][SessionManager] getGameService() -> " + gameService);
        return gameService;
    }

    // -----------------------
    // Authentication Service
    // -----------------------
    public static synchronized void setAuthService(AuthenticationService svc) {
        System.out.println("[DEBUG][SessionManager] setAuthService(" + svc + ")");
        authService = svc;
    }
    public static synchronized AuthenticationService getAuthService() {
        System.out.println("[DEBUG][SessionManager] getAuthService() -> " + authService);
        return authService;
    }

    // -----------------------
    // Clear everything
    // -----------------------
    public static synchronized void clearSession() {
        System.out.println("[DEBUG][SessionManager] clearSession()");
        gameToken = null;
        sessionToken = null;
        loggedInPlayer = null;
        // (OPTIONAL) if you ever need to clear services/ORB too:
        // orb = null;
        // rootPoa = null;
        // gameService = null;
        // authService = null;
    }
}
