package Client_Java.player;

import Client_Java.player.controller.LogInController;
import Client_Java.player.model.LogInPageModel;
import Client_Java.player.view.LoginPageView;
import Shared_Files.PlayerAccount;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class PlayerClient_Java extends Application {
    public static Stage APPLICATION_STAGE;
    private static PlayerAccount loggedInPlayer; // Updated to PlayerAccount
    private static long loggedInPlayerID;
    private static String sessionToken;

    public static void main(String[] args) {
        PlayerClient_Model clientModel = new PlayerClient_Model();
        clientModel.init(); // Initialize CORBA connections
        PlayerClient_Model.startOrb();
        launch(args);
    }
    public static void handleForcedLogout() {
        // Clear session
        sessionToken = null;
        loggedInPlayer = null;
        loggedInPlayerID = 0;

        // Reload the login UI
        Stage stage = getStage();
        // You might refactor loadLoginGUI() to be public or extract its logic here
        loadLoginGUI();
    }


    @Override
    public void start(Stage stage) {
        APPLICATION_STAGE = stage;
        loadLoginGUI();
    }

    private static void loadLoginGUI() {
        try {
            File fxmlFile = new File("src/main/resources/fxml/player/LogInPage.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
            Parent root = loader.load();

            LoginPageView loginPageView = loader.getController();
            if (loginPageView == null) {
                System.err.println("[ERROR] LoginPageView is NULL after FXML load!");
            } else {
                System.out.println("[DEBUG] LoginPageView controller loaded successfully.");
                LogInPageModel model = new LogInPageModel(PlayerClient_Model.authService);
                new LogInController(model, loginPageView);
            }

            Scene scene = new Scene(root);
            APPLICATION_STAGE.setScene(scene);
            APPLICATION_STAGE.centerOnScreen();
            APPLICATION_STAGE.setResizable(false);

            APPLICATION_STAGE.setOnCloseRequest(event -> {
                System.out.println("[INFO] Application is closing...");
                System.exit(0);
            });

            APPLICATION_STAGE.setTitle("What's The Word!!");
            APPLICATION_STAGE.show();

            System.out.println("[Client] LOGIN GUI LOADED SUCCESSFULLY");

        } catch (IOException e) {
            System.err.println("[ERROR] IOException while loading GUI: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[ERROR] Unexpected exception: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // Session management methods
    public static void setLoggedInPlayer(PlayerAccount player) {
        loggedInPlayer = player;
    }

    public static PlayerAccount getLoggedInPlayer() {
        return loggedInPlayer;
    }

    public static void setLoggedInPlayerID(long id) {
        loggedInPlayerID = id;
    }

    public static long getLoggedInPlayerID() {
        return loggedInPlayerID;
    }

    public static void setSessionToken(String token) {
        sessionToken = token;
    }

    public static String getSessionToken() {
        return sessionToken;
    }

    public static Stage getStage() {
        return APPLICATION_STAGE;
    }
}
