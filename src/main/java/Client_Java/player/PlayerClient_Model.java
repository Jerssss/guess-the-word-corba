package Client_Java.player;

import Server_Java.idls.PlayerCallBackIDL.LoginCallbackService;
import Server_Java.idls.PlayerCallBackIDL.LoginCallbackServiceHelper;
import Server_Java.implementation.LoginCallBackServiceImpl;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

/**
 * Handles ORB initialization and callback registration for client.
 */
public class PlayerClient_Model {
    public static Server_Java.idls.AuthenticationIDL.AuthenticationService authService;
    public static Server_Java.idls.GameIDL.GameService gameService;
    public static Server_Java.idls.PlayerCallBackIDL.GameCallBackService gameCallbackService;

    // Keep ORB and POA references for callback registration
    private static ORB orb;
    private static POA rootPOA;

    /**
     * Initialize ORB, resolve naming service, and narrow stubs.
     */
    public void init() {
        try {
            String[] orbArgs = {"-ORBInitialPort", "2000", "-ORBInitialHost", "localhost"};
            orb = ORB.init(orbArgs, null);

            // Obtain root POA to activate callback servants
            org.omg.CORBA.Object poaRef = orb.resolve_initial_references("RootPOA");
            rootPOA = POAHelper.narrow(poaRef);
            rootPOA.the_POAManager().activate();

            // Resolve naming context
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            org.omg.CosNaming.NamingContextExt ncRef =
                    org.omg.CosNaming.NamingContextExtHelper.narrow(objRef);

            // Narrow service references
            authService = Server_Java.idls.AuthenticationIDL.AuthenticationServiceHelper
                    .narrow(ncRef.resolve_str("AuthenticationService"));
            gameService = Server_Java.idls.GameIDL.GameServiceHelper
                    .narrow(ncRef.resolve_str("GameService"));
            gameCallbackService = Server_Java.idls.PlayerCallBackIDL.GameCallBackServiceHelper
                    .narrow(ncRef.resolve_str("GameCallBackService"));

            System.out.println("[Client] CORBA Services Initialized Successfully!");
        } catch (Exception e) {
            System.err.println("[Client ERROR] CORBA Initialization Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Registers the given LoginCallbackServiceImpl with the ORB and returns its stub.
     */
    public static LoginCallbackService registerCallback(LoginCallBackServiceImpl callbackImpl) {
        try {
            // Activate the servant to obtain an object reference
            org.omg.CORBA.Object ref = rootPOA.servant_to_reference(callbackImpl);
            // Narrow to LoginCallbackService stub
            return LoginCallbackServiceHelper.narrow(ref);
        } catch (Exception e) {
            System.err.println("[Client ERROR] Failed to register callback: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Helper to run ORB event loop if needed (e.g., in separate thread).
     */
    public static void startOrb() {
        new Thread(() -> orb.run()).start();
    }
}
