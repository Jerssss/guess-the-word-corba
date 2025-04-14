package Client_Java.player.model;

import PlayerGame.AuthService;
import PlayerGame.AuthServiceHelper;
import PlayerGame.GameService;
import PlayerGame.GameServiceHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

public class PlayerClient_Model {
    public static AuthService authService;
    public static GameService gameService;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;

    public void init() {
        String[] args = {"-ORBInitialPort", "2000", "-ORBInitialHost", "localhost"};

        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                connectToServer(args);
                if (authService != null && gameService != null) {
                    System.out.println("[SUCCESS] Connected to server services");
                    return;
                }
            } catch (Exception e) {
                System.err.println("[ATTEMPT " + (i+1) + "] Connection failed: " + e.getMessage());
                if (i < MAX_RETRIES - 1) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        throw new RuntimeException("Failed to connect to server after " + MAX_RETRIES + " attempts");
    }

    private void connectToServer(String[] args) throws Exception {
        ORB orb = ORB.init(args, null);

        // Get naming service reference
        org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
        NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

        // Resolve services using exact names
        authService = AuthServiceHelper.narrow(ncRef.resolve_str("AuthService"));
        if (authService == null) {
            throw new RuntimeException("AuthService not found in naming service");
        }

        gameService = GameServiceHelper.narrow(ncRef.resolve_str("GameService"));
        if (gameService == null) {
            throw new RuntimeException("GameService not found in naming service");
        }
    }
}