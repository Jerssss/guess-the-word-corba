package Client_Java.model;

import GameApp.AuthService;
import GameApp.AuthServiceHelper;
import GameApp.GameService;
import GameApp.GameServiceHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

public class Client_Model {
    public static AuthService authService;
    public static GameService gameService;

    public void init() {
        String[] args = {"-ORBInitialPort", "2000", "-ORBInitialHost", "localhost"};
        connectToServer(args);
    }

    private void connectToServer(String[] args){
        try {
            ORB orb = ORB.init(args, null);
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            authService = AuthServiceHelper.narrow(ncRef.resolve_str("Authentication"));
            gameService = GameServiceHelper.narrow(ncRef.resolve_str("Game Manager"));

            System.out.println("CONNECTED TO THE SERVER :)");

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
