package Client_Java;

import Client_Java.controller.LogInPageController;
import Client_Java.model.Client_Model;
import Client_Java.model.LogInPageModel;
import Client_Java.view.LoginPageView;
import GameApp.Player;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class Client_Java extends Application {
    public static Stage APPLICATION_STAGE;
    private static Player loggedInPlayer; // Variable to store the logged-in player

    public static void main(String[] args) {
        Client_Model clientModel = new Client_Model();
        clientModel.init(); // CORBA connection
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        APPLICATION_STAGE = stage;
        loadLoginGUI();
    }

    private void loadLoginGUI() {
        try {
            File fxmlFile = new File("src/main/java/Client_Java/res/fxml/WWLogInPage.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
            Parent root = loader.load();

            // Get the controller (view in this case)
            LoginPageView loginPageView = loader.getController();
            if (loginPageView == null) {
                System.err.println("[ERROR] LoginPageView is NULL after FXML load!");
            } else {
                System.out.println("[DEBUG] LoginPageView controller loaded successfully.");
                LogInPageModel model = new LogInPageModel(Client_Model.authService);
                new LogInPageController(model, loginPageView);
            }

            // Set the scene
            Scene scene = new Scene(root);
            APPLICATION_STAGE.setScene(scene);
            APPLICATION_STAGE.centerOnScreen();
            APPLICATION_STAGE.setResizable(false);

            APPLICATION_STAGE.setOnCloseRequest(event -> {
                System.out.println("[INFO] Close request received. Terminating the application...");
                System.exit(0); // or custom cleanup logic
            });

            APPLICATION_STAGE.setTitle("What's The Word!!");
            APPLICATION_STAGE.show();

            System.out.println("[Client] LOGIN GUI LOADED SUCCESSFULLY");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("[ERROR] Could not load WWLogInPage.fxml: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[ERROR] Unexpected error in loadLoginGUI(): " + e.getMessage());
        }
    }

    // Method to set the logged-in player
    public static void setLoggedInPlayer(Player player) {
        loggedInPlayer = player;
    }

    // Method to get the logged-in player
    public static Player getLoggedInPlayer() {
        return loggedInPlayer;
    }

    // Method to get the application stage
    public static Stage getStage() {
        return APPLICATION_STAGE;
    }
}