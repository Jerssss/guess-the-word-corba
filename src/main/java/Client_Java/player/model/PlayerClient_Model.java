package Client_Java.player.model;


import IDL_Files.AuthenticationIDL.AuthenticationService;


import IDL_Files.AuthenticationIDL.AuthenticationServiceHelper;
import IDL_Files.GameIDL.GameService;
import IDL_Files.GameIDL.GameServiceHelper;
import IDL_Files.PlayerCallBackIDL.PlayerCallBackService;
import IDL_Files.PlayerCallBackIDL.PlayerCallBackServiceHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CORBA.Object;

public class PlayerClient_Model {
    public static AuthenticationService authService;
    public static PlayerCallBackService callbackService;
    public static GameService gameService;

    public void init() {
        try {
            // Initialize ORB
            String[] orbArgs = {"-ORBInitialPort", "2000", "-ORBInitialHost", "localhost"};
            ORB orb = ORB.init(orbArgs, null);

            // Obtain Naming Service reference
            Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // Narrow references to services
            authService = AuthenticationServiceHelper.narrow(ncRef.resolve_str("PlayerAuthenticationService"));
            callbackService = PlayerCallBackServiceHelper.narrow(ncRef.resolve_str("PlayerCallBackService"));
            gameService = GameServiceHelper.narrow(ncRef.resolve_str("PlayerGameService"));

            System.out.println("[Client] CORBA Services Initialized Successfully!");
        } catch (Exception e) {
            System.err.println("[Client ERROR] CORBA Initialization Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
