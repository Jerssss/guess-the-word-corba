// File: Client_Java/player/PlayerClient_Model.java
package Client_Java.player;

import AuthenticationIDL.AuthenticationService;
import AuthenticationIDL.AuthenticationServiceHelper;
import GameIDL.GameService;
import GameIDL.GameServiceHelper;
import PlayerCallBackIDL.GameCallBackServiceHelper;
import PlayerCallBackIDL.WaitingRoomGameCallbackServiceHelper;
import PlayerCallBackIDL.LoginCallbackServiceHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

/**
 * Bootstraps ORB/POA, resolves core services, and
 * provides helper methods to register callback servants.
 */
public class PlayerClient_Model {
    private final ORB orb;
    private final POA rootPoa;
    private final AuthenticationService authService;
    private final GameService gameService;

    public PlayerClient_Model(String[] orbArgs) throws Exception {
        // ORB & POA init
        this.orb = ORB.init(orbArgs, null);
        this.rootPoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
        this.rootPoa.the_POAManager().activate();
        SessionManager.initOrb(orb, rootPoa);

        // NameService lookup
        NamingContextExt nc = NamingContextExtHelper.narrow(
                orb.resolve_initial_references("NameService")
        );

        // Resolve AuthenticationService
        this.authService = AuthenticationServiceHelper.narrow(
                nc.resolve_str("AuthenticationService")
        );
        SessionManager.setAuthService(this.authService);

        // Resolve GameService
        this.gameService = GameServiceHelper.narrow(
                nc.resolve_str("GameService")
        );
        SessionManager.setGameService(this.gameService);
    }

    public AuthenticationService getAuthService() { return authService; }
    public GameService getGameService()     { return gameService;   }

    /** Start the ORB event‐loop. */
    public void startOrb() {
        new Thread(orb::run).start();
    }

    /** Register a LoginCallback servant and return its stub. */
    public org.omg.CORBA.Object registerLoginCallback(
            org.omg.PortableServer.Servant callbackServant
    ) {
        try {
            org.omg.CORBA.Object ref = rootPoa.servant_to_reference(callbackServant);
            return LoginCallbackServiceHelper.narrow(ref);
        } catch (Exception e) {
            throw new RuntimeException("Login callback registration failed", e);
        }
    }

    /** Register a GameCallback servant and return its stub. */
    public org.omg.CORBA.Object registerGameCallback(
            org.omg.PortableServer.Servant callbackServant
    ) {
        try {
            org.omg.CORBA.Object ref = rootPoa.servant_to_reference(callbackServant);
            return GameCallBackServiceHelper.narrow(ref);
        } catch (Exception e) {
            throw new RuntimeException("Game callback registration failed", e);
        }
    }

    /** Register a WaitingRoom callback servant and return its stub. */
    public org.omg.CORBA.Object registerWaitingRoomCallback(
            org.omg.PortableServer.Servant callbackServant
    ) {
        try {
            org.omg.CORBA.Object ref = rootPoa.servant_to_reference(callbackServant);
            return WaitingRoomGameCallbackServiceHelper.narrow(ref);
        } catch (Exception e) {
            throw new RuntimeException("Waiting‐room callback registration failed", e);
        }
    }
}
