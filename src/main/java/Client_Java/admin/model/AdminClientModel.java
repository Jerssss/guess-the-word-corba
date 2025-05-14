package Client_Java.admin.model;

import AdminIDL.AdminService;
import AdminIDL.AdminServiceHelper;
import AuthenticationIDL.AuthenticationService;
import AuthenticationIDL.AuthenticationServiceHelper;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

public class AdminClientModel {
    public static AuthenticationService authService;
    public static AdminService adminService;

    public void init() {
        try {
            // Initialize ORB
            String[] orbArgs = {"-ORBInitialPort", "1050", "-ORBInitialHost", "192.168.100.105"};
            ORB orb = ORB.init(orbArgs, null);

            // Obtain Naming Service reference
            Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // Narrow references to services
            authService = AuthenticationServiceHelper.narrow(ncRef.resolve_str("AuthenticationService"));
            adminService = AdminServiceHelper.narrow(ncRef.resolve_str("AdminService"));

            System.out.println("[Client] CORBA Services Initialized Successfully!");
        } catch (Exception e) {
            System.err.println("[Client ERROR] CORBA Initialization Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
