package Server_Java;



import Server_Java.idls.AdminIDL.AdminService;
import Server_Java.idls.AdminIDL.AdminServiceHelper;
import Server_Java.idls.AuthenticationIDL.AuthenticationService;
import Server_Java.idls.AuthenticationIDL.AuthenticationServiceHelper;
import Server_Java.idls.GameIDL.GameService;
import Server_Java.idls.GameIDL.GameServiceHelper;
import Server_Java.idls.PlayerCallBackIDL.GameCallBackService;
import Server_Java.idls.PlayerCallBackIDL.GameCallBackServiceHelper;
import Server_Java.idls.PlayerCallBackIDL.LoginCallbackService;
import Server_Java.idls.PlayerCallBackIDL.LoginCallbackServiceHelper;
import Server_Java.implementation.*;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Server_Java {

    private static final Set<String> activeClients = Collections.synchronizedSet(new HashSet<>());

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
            GameCallbackServiceImpl gameCallbackService = new GameCallbackServiceImpl();
            LoginCallBackServiceImpl loginCallBackService = new LoginCallBackServiceImpl();
            GameServiceImpl gameService = new GameServiceImpl();
            AdminServiceImpl adminService = new AdminServiceImpl();

            // Convert servants to CORBA object references
            org.omg.CORBA.Object authRef = rootpoa.servant_to_reference(authService);
            org.omg.CORBA.Object gameCallbackRef = rootpoa.servant_to_reference(gameCallbackService);
            org.omg.CORBA.Object loginCallbackRef = rootpoa.servant_to_reference(loginCallBackService);
            org.omg.CORBA.Object gameRef = rootpoa.servant_to_reference(gameService);
            org.omg.CORBA.Object adminRef = rootpoa.servant_to_reference(adminService);

            // Narrow to specific helper types
            AuthenticationService paRef = AuthenticationServiceHelper.narrow(authRef);
            GameCallBackService gcbRef = GameCallBackServiceHelper.narrow(gameCallbackRef);
            LoginCallbackService lcbRef = LoginCallbackServiceHelper.narrow(loginCallbackRef);
            GameService pgRef = GameServiceHelper.narrow(gameRef);
            AdminService asRef = AdminServiceHelper.narrow(adminRef);

            // Bind to naming service
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            ncRef.rebind(ncRef.to_name("AuthenticationService"), paRef);
            ncRef.rebind(ncRef.to_name("GameCallBackService"), gcbRef);
            ncRef.rebind(ncRef.to_name("LoginCallBackService"), lcbRef);
            ncRef.rebind(ncRef.to_name("GameService"), pgRef);
            ncRef.rebind(ncRef.to_name("AdminService"), asRef);

            // --- Styled Welcome Message Yehey ---
            System.out.println();
            System.out.println("=============================================");
            System.out.println("Welcome to What's The Word? Game Server");
            System.out.println("---------------------------------------------");
            System.out.println("Server Status: Running and Listening...");
            System.out.println("CORBA ORB initialized on localhost:2000");
            System.out.println("Services: Authentication, Game, Callback, Admin");
            System.out.println("=============================================");
            System.out.println();

            orb.run();
        } catch (Exception e) {
            System.err.println("SERVER ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void addActiveClient(String username) {
        activeClients.add(username);
        printActiveClients();
    }

    public static void removeActiveClient(String username) {
        activeClients.remove(username);
        printActiveClients();
    }

    public static void printActiveClients() {
        System.out.println("[SERVER] Current Active Clients:");
        if (activeClients.isEmpty()) {
            System.out.println(" - No active clients.");
        } else {
            activeClients.forEach(user -> System.out.println(" - " + user));
        }
        System.out.println("----------------------------------");
    }
}
