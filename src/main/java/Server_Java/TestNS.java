package Server_Java;

import org.omg.CORBA.*;
import org.omg.CORBA.Object;

public class TestNS {
    public static void main(String[] args) {
        ORB orb = ORB.init(new String[]{"-ORBInitialPort", "1050"}, null);
        try {
            Object obj = orb.resolve_initial_references("NameService");
            System.out.println("NameService is available.");
        } catch (Exception e) {
            System.err.println("Failed to resolve NameService:");
            e.printStackTrace();
        }
    }
}
