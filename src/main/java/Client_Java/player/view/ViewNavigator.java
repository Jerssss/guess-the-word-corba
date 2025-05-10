package Client_Java.player.view;

import Client_Java.player.PlayerClient_Java;
import Client_Java.player.SessionManager;
import Client_Java.player.controller.GameLobbyController;
import Client_Java.player.controller.LogInController;
import Client_Java.player.controller.WaitingRoomController;
import Client_Java.player.controller.GameRoomController;
import Client_Java.player.model.GameLobbyModel;
import Client_Java.player.model.LogInPageModel;
import Client_Java.player.model.WaitingRoomModel;
import Client_Java.player.model.GameRoomModel;

import Shared_Files.PlayerAccount;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ViewNavigator {

    private static final Stage mainStage = PlayerClient_Java.getStage();
    private static final String CSS = ViewNavigator.class
            .getClassLoader()
            .getResource("css/styles.css")
            .toExternalForm();
    private static Stage primaryStage;

    /** Expose primary Stage for popups */
    public static Stage getStage() {
        return mainStage;
    }

    // Method to set the primary stage (call this during application startup)
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
        primaryStage.centerOnScreen(); // Center the primary stage when set
    }

    private static void switchScene(Parent root, String title) {
        if (primaryStage == null) {
            throw new IllegalStateException("Primary stage not set. Call setPrimaryStage first.");
        }
        Scene scene = new Scene(root);
        try {
            scene.getStylesheets().add(CSS);
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to load stylesheet: " + e.getMessage());
        }
        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.centerOnScreen(); // Center the stage after setting new scene
        primaryStage.show();
    }

    public static void goToLogin() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                ViewNavigator.class.getResource("/fxml/player/LogInPage.fxml")
        );
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load LogInPage.fxml: " + e.getMessage());
            throw e;
        }
        LogInPageModel model = new LogInPageModel(SessionManager.getAuthService());
        new LogInController(model, loader.getController());
        switchScene(root, "What's the Word — Login");
    }

    public static void goToLobby() throws Exception {
        PlayerAccount acct = SessionManager.getLoggedInPlayer();
        if (acct == null) {
            System.err.println("[ERROR] No logged-in player found, redirecting to login");
            goToLogin();
            return;
        }

        FXMLLoader loader = new FXMLLoader(
                ViewNavigator.class.getResource("/fxml/player/GameLobbyPage.fxml")
        );
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load GameLobbyPage.fxml: " + e.getMessage());
            throw e;
        }

        GameLobbyModel model = new GameLobbyModel(acct, SessionManager.getGameService());
        GameLobbyView view = loader.getController();
        if (view == null) {
            System.err.println("[ERROR] GameLobbyView controller is null");
            throw new IllegalStateException("GameLobbyView controller is null");
        }
        GameLobbyController controller = new GameLobbyController(model, view, acct);
        switchScene(root, "What's the Word — Lobby");
    }

    public static void goToWaitingRoom() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                ViewNavigator.class.getResource("/fxml/player/WaitingRoomPage.fxml")
        );
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load WaitingRoomPage.fxml: " + e.getMessage());
            throw e;
        }
        WaitingRoomModel model = new WaitingRoomModel(SessionManager.getGameService());
        new WaitingRoomController(model, loader.getController());
        switchScene(root, "What's the Word — Waiting Room");
    }

    public static void goToGameRoom(String gameToken) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                ViewNavigator.class.getResource("/fxml/player/GameRoomPage.fxml")
        );
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load GameRoomPage.fxml: " + e.getMessage());
            throw e;
        }
        GameRoomModel model = new GameRoomModel(SessionManager.getGameService());
        PlayerAccount acct = SessionManager.getLoggedInPlayer();
        new GameRoomController(
                model,
                loader.getController(),
                acct.getPlayerId(),
                SessionManager.getSessionToken(),
                gameToken
        );
        switchScene(root, "What's the Word — Game");
    }

    public static void goToGameRoom() throws Exception {
        goToGameRoom(SessionManager.getGameToken());
    }
}