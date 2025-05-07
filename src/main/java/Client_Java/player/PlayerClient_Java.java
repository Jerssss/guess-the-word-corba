package Client_Java.player;

import Client_Java.player.view.ViewNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

public class PlayerClient_Java extends Application {
    private static PlayerClient_Model corbaClient;
    private static Stage primaryStage;

    public static void main(String[] args) {
        try {
            corbaClient = new PlayerClient_Model(new String[]{
                    "-ORBInitialPort", "1050",
                    "-ORBInitialHost", "localhost"
            });
            corbaClient.startOrb();
            // Set services so SessionManager can hand them out
            SessionManager.setGameService(corbaClient.getGameService());
            SessionManager.setAuthService(corbaClient.getAuthService());
            launch(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Expose the CORBA client helper for callback registration */
    public static PlayerClient_Model getClientModel() {
        return corbaClient;
    }

    /** Expose the main JavaFX Stage for ViewNavigator */
    public static Stage getStage() {
        return primaryStage;
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        ViewNavigator.setPrimaryStage(primaryStage);
        System.out.println("[DEBUG] Primary stage set in PlayerClient_Java");
        // Kick off the very first scene
        ViewNavigator.goToLogin();
        // Optional: lock window size if you like
        stage.setResizable(false);
    }
}