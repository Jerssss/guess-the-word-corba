package Server_Java;

import PlayerGame.AuthService;
import PlayerGame.AuthServiceHelper;
import PlayerGame.GameService;
import PlayerGame.GameServiceHelper;
import Server_Java.implementation.AuthenticationImpl;
import Server_Java.implementation.GameImpl;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class Server_Java {
    public static void main(String[] args) {
        try {
            // Initialize the ORB with the specified arguments
            String[] orbArgs = {"-ORBInitialPort", "2000", "-ORBInitialHost", "localhost"};
            ORB orb = ORB.init(orbArgs, null);

            // Resolve the RootPOA
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            // Create service implementations
            AuthenticationImpl authService = new AuthenticationImpl();
            GameImpl gameService = new GameImpl();

            // Register services with the ORB
            org.omg.CORBA.Object authRef = rootpoa.servant_to_reference(authService);
            org.omg.CORBA.Object gameRef = rootpoa.servant_to_reference(gameService);

            AuthService aRef = AuthServiceHelper.narrow(authRef);
            GameService gRef = GameServiceHelper.narrow(gameRef);

            // Resolve the NameService
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // Bind the services to the NameService
            NameComponent[] authPath = ncRef.to_name("Authentication");
            NameComponent[] gamePath = ncRef.to_name("Game Manager");

            ncRef.rebind(authPath, aRef);
            ncRef.rebind(gamePath, gRef);

            System.out.println("CORBA Game Server ready and waiting...");
            orb.run();
        } catch (Exception e) {
            e.printStackTrace(); // Print any exceptions that occur
        }
    }
}