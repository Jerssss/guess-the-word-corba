package Server_Java;

import IDL_Files.AdminIDL.AdminService;
import IDL_Files.AdminIDL.AdminServiceHelper;
import IDL_Files.AuthenticationIDL.AuthenticationService;
import IDL_Files.AuthenticationIDL.AuthenticationServiceHelper;
import IDL_Files.GameIDL.GameService;
import IDL_Files.GameIDL.GameServiceHelper;
import IDL_Files.PlayerCallBackIDL.PlayerCallBackService;
import IDL_Files.PlayerCallBackIDL.PlayerCallBackServiceHelper;
import Server_Java.implementation.AdminServiceImpl;
import Server_Java.implementation.AuthenticationServiceImpl;
import Server_Java.implementation.GameServiceImpl;
import Server_Java.implementation.PlayerCallBackImpl;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class Server_Java {
    public static void main(String[] args) {
        try {
            // Initialize ORB
            String[] orbArgs = {"-ORBInitialPort", "2000", "-ORBInitialHost", "localhost"};
            ORB orb = ORB.init(orbArgs, null);

            // Get RootPOA
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            // Initialize implementations
            AuthenticationServiceImpl authService = new AuthenticationServiceImpl();
            PlayerCallBackImpl callbackService = new PlayerCallBackImpl();
            GameServiceImpl gameService = new GameServiceImpl();
            AdminServiceImpl adminService = new AdminServiceImpl();

            // Convert servants to CORBA object references
            org.omg.CORBA.Object authRef = rootpoa.servant_to_reference(authService);
            org.omg.CORBA.Object callbackRef = rootpoa.servant_to_reference(callbackService);
            org.omg.CORBA.Object gameRef = rootpoa.servant_to_reference(gameService);
            org.omg.CORBA.Object adminRef = rootpoa.servant_to_reference(adminService);

            // Narrow to specific helper types
            AuthenticationService paRef = AuthenticationServiceHelper.narrow(authRef);
            PlayerCallBackService cbRef = PlayerCallBackServiceHelper.narrow(callbackRef);
            GameService pgRef = GameServiceHelper.narrow(gameRef);
            AdminService asRef = AdminServiceHelper.narrow(adminRef);

            // Bind to naming service
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            ncRef.rebind(ncRef.to_name("AuthenticationService"), paRef);
            ncRef.rebind(ncRef.to_name("PlayerCallBackService"), cbRef);
            ncRef.rebind(ncRef.to_name("PlayerGameService"), pgRef);
            ncRef.rebind(ncRef.to_name("adminService"), asRef);

            System.out.println("CORBA Game Server ready and waiting...");

            orb.run();
        } catch (Exception e) {
            System.err.println("SERVER ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
