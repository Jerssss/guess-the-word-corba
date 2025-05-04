// File: Client_Java/player/view/ViewNavigator.java
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

public class ViewNavigator {

    private static final Stage mainStage = PlayerClient_Java.getStage();
    private static final String CSS = ViewNavigator.class
            .getClassLoader()
            .getResource("css/styles.css")
            .toExternalForm();

    /** Expose primary Stage for popups */
    public static Stage getStage() {
        return mainStage;
    }

    private static void switchScene(Parent root, String title) {
        Scene scene = new Scene(root);
        scene.getStylesheets().add(CSS);
        mainStage.setTitle(title);
        mainStage.setScene(scene);
        mainStage.show();
    }

    public static void goToLogin() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                ViewNavigator.class.getResource("/fxml/player/LogInPage.fxml")
        );
        Parent root = loader.load();
        LogInPageModel model = new LogInPageModel(SessionManager.getAuthService());
        new LogInController(model, loader.getController());
        switchScene(root, "What's the Word — Login");
    }

    public static void goToLobby() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                ViewNavigator.class.getResource("/fxml/player/GameLobbyPage.fxml")
        );
        Parent root = loader.load();
        PlayerAccount acct   = SessionManager.getLoggedInPlayer();
        GameLobbyModel model = new GameLobbyModel(acct);
        new GameLobbyController(model, loader.getController(), acct);
        switchScene(root, "What's the Word — Lobby");
    }

    public static void goToWaitingRoom() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                ViewNavigator.class.getResource("/fxml/player/WaitingRoomPage.fxml")
        );
        Parent root = loader.load();
        WaitingRoomModel model = new WaitingRoomModel(SessionManager.getGameService());
        new WaitingRoomController(model, loader.getController());
        switchScene(root, "What's the Word — Waiting Room");
    }

    /** Navigate to the Game Room with explicit token */
    public static void goToGameRoom(String gameToken) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                ViewNavigator.class.getResource("/fxml/player/GameRoomPage.fxml")
        );
        Parent root = loader.load();
        GameRoomModel model = new GameRoomModel(SessionManager.getGameService());
        PlayerAccount acct  = SessionManager.getLoggedInPlayer();
        new GameRoomController(
                model,
                loader.getController(),
                acct.getPlayerId(),
                SessionManager.getSessionToken(),
                gameToken
        );
        switchScene(root, "What's the Word — Game");
    }

    /** Navigate to the Game Room using the stored token */
    public static void goToGameRoom() throws Exception {
        goToGameRoom(SessionManager.getGameToken());
    }
}
