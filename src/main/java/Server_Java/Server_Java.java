package Server_Java;

import AdminIDL.AdminService;
import AdminIDL.AdminServiceHelper;
import AuthenticationIDL.AuthenticationService;
import AuthenticationIDL.AuthenticationServiceHelper;
import GameIDL.GameService;
import GameIDL.GameServiceHelper;
import Server_Java.implementation.AuthenticationServiceImpl;
import Server_Java.implementation.GameServiceImpl;
import Server_Java.implementation.AdminServiceImpl;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Server_Java {

    private static final Set<String> activeClients =
            Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        try {
            // 1) ORB & POA initialization
            String hostIP = "192.168.12.129";
            String[] orbArgs = {"-ORBInitialPort", "1050", "-ORBInitialHost", hostIP};
            ORB orb = ORB.init(orbArgs, null);

            POA rootPoa = POAHelper.narrow(
                    orb.resolve_initial_references("RootPOA")
            );
            rootPoa.the_POAManager().activate();

            // 2) Instantiate your core servants
            AuthenticationServiceImpl authService = new AuthenticationServiceImpl();
            GameServiceImpl gameService = new GameServiceImpl();
            AdminServiceImpl adminService = new AdminServiceImpl(authService);

            // 3) Convert servants to CORBA object references
            org.omg.CORBA.Object authRef  = rootPoa.servant_to_reference(authService);
            org.omg.CORBA.Object gameRef  = rootPoa.servant_to_reference(gameService);
            org.omg.CORBA.Object adminRef = rootPoa.servant_to_reference(adminService);

            // 4) Narrow to typed interfaces
            AuthenticationService authSrv = AuthenticationServiceHelper.narrow(authRef);
            GameService gameSrv           = GameServiceHelper.narrow(gameRef);
            AdminService adminSrv         = AdminServiceHelper.narrow(adminRef);

            // 5) Bind into the Naming Service
            NamingContextExt ncRef = NamingContextExtHelper.narrow(
                    orb.resolve_initial_references("NameService")
            );
            ncRef.rebind(ncRef.to_name("AuthenticationService"), authSrv);
            ncRef.rebind(ncRef.to_name("GameService"),           gameSrv);
            ncRef.rebind(ncRef.to_name("AdminService"),          adminSrv);

            // 6) Log status
            System.out.println("\n=============================================");
            System.out.println("What's The Word? Game Server is up!");
            System.out.println("Host: " + hostIP + ", Port: 1050");
            InetAddress localhost = InetAddress.getLocalHost();
            System.out.println("Local IP: " + localhost.getHostAddress());
            System.out.println("Published Services: Authentication, Game, Admin");
            System.out.println("=============================================\n");

            // 7) Run the ORB loop
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
        System.out.println("[SERVER] Active Clients:");
        if (activeClients.isEmpty()) {
            System.out.println(" - none");
        } else {
            activeClients.forEach(u -> System.out.println(" - " + u));
        }
        System.out.println("----------------------------------");
    }
}
