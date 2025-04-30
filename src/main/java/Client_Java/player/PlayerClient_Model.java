package Client_Java.player;

import Server_Java.idls.AuthenticationIDL.AuthenticationService;
import Server_Java.idls.AuthenticationIDL.AuthenticationServiceHelper;
import Server_Java.idls.GameIDL.GameService;

import Server_Java.idls.GameIDL.GameServiceHelper;
import Server_Java.idls.PlayerCallBackIDL.GameCallBackService;
import Server_Java.idls.PlayerCallBackIDL.GameCallBackServiceHelper;
import Server_Java.idls.PlayerCallBackIDL.LoginCallbackService;
import Server_Java.idls.PlayerCallBackIDL.LoginCallbackServiceHelper;

// NEW imports for waiting-room callback
import Server_Java.idls.PlayerCallBackIDL.WaitingRoomGameCallbackService;
import Server_Java.idls.PlayerCallBackIDL.WaitingRoomGameCallbackServiceHelper;


import Server_Java.implementation.GameCallbackServiceImpl;
import Server_Java.implementation.LoginCallBackServiceImpl;
import Server_Java.implementation.WaitingRoomCallbackServiceImpl;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

/**
 * Handles ORB initialization and callback registration for client.
 */
public class PlayerClient_Model {
    public static AuthenticationService authService;
    public static GameService gameService;
    public static GameCallBackService gameCallbackService;

    private static ORB orb;
    private static POA rootPOA;

    public void init() {
        try {
            String[] orbArgs = {"-ORBInitialPort", "2000", "-ORBInitialHost", "192.168.191.28"};
            orb = ORB.init(orbArgs, null);

            org.omg.CORBA.Object poaRef = orb.resolve_initial_references("RootPOA");
            rootPOA = POAHelper.narrow(poaRef);
            rootPOA.the_POAManager().activate();

            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            org.omg.CosNaming.NamingContextExt ncRef =
                    org.omg.CosNaming.NamingContextExtHelper.narrow(objRef);

            // Try resolving each service and handle individually
            try {
                authService = AuthenticationServiceHelper.narrow(ncRef.resolve_str("AuthenticationService"));
                System.out.println("[Client] AuthenticationService resolved.");
            } catch (Exception e) {
                System.err.println("[Client ERROR] Failed to resolve 'AuthenticationService': " + e.getMessage());
                return;
            }

            try {
                gameService = GameServiceHelper.narrow(ncRef.resolve_str("GameService"));
                System.out.println("[Client] GameService resolved.");
            } catch (Exception e) {
                System.err.println("[Client ERROR] Failed to resolve 'GameService': " + e.getMessage());
                return;
            }

            try {
                gameCallbackService = GameCallBackServiceHelper.narrow(ncRef.resolve_str("GameCallBackService"));
                System.out.println("[Client] GameCallBackService resolved.");
            } catch (Exception e) {
                System.err.println("[Client ERROR] Failed to resolve 'GameCallBackService': " + e.getMessage());
                return;
            }

            System.out.println("[Client] CORBA Services Initialized Successfully!");
        } catch (Exception e) {
            System.err.println("[Client ERROR] CORBA Initialization Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static LoginCallbackService registerLoginCallback(LoginCallBackServiceImpl callbackImpl) {
        try {
            org.omg.CORBA.Object ref = rootPOA.servant_to_reference(callbackImpl);
            return LoginCallbackServiceHelper.narrow(ref);
        } catch (Exception e) {
            System.err.println("[Client ERROR] Failed to register login callback: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static GameCallBackService registerGameCallback(GameCallbackServiceImpl callbackImpl) {
        try {
            org.omg.CORBA.Object ref = rootPOA.servant_to_reference(callbackImpl);
            return GameCallBackServiceHelper.narrow(ref);
        } catch (Exception e) {
            System.err.println("[Client ERROR] Failed to register game callback: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static WaitingRoomGameCallbackService registerWaitingRoomCallback(WaitingRoomCallbackServiceImpl callbackImpl) {
        try {
            org.omg.CORBA.Object ref = rootPOA.servant_to_reference(callbackImpl);
            return WaitingRoomGameCallbackServiceHelper.narrow(ref);
        } catch (Exception e) {
            System.err.println("[Client ERROR] Failed to register waiting-room callback: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static void startOrb() {
        new Thread(() -> orb.run()).start();
    }
}
