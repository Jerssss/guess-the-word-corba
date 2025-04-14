package Client_Java.player.controller;

import Client_Java.PlayerClient_Java;
import Client_Java.player.model.LogInPageModel;
import Client_Java.player.view.LoginPageView;
import PlayerGame.AlreadyLoggedInException;
import PlayerGame.AuthenticationException;
import PlayerGame.Player;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.File;
import java.io.IOException;

import org.omg.CORBA.IntHolder;

public class LogInPageController {
    private final LogInPageModel model;
    private final LoginPageView view;

    public LogInPageController(LogInPageModel model, LoginPageView view) {
        this.model = model;
        this.view = view;

        this.view.setActionContinueButton(event -> handleContinueButton());
        this.view.setActionQuitButton(event -> handleQuitButton());
    }

    private void handleContinueButton() {
        String username = view.getUsernameField().getText().trim();
        String password = view.getPasswordField().getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Please fill all the fields.");
            return;
        }

        try {
            IntHolder playerID = new IntHolder();
            String sessionToken = model.login(username, password, playerID);

            if (sessionToken != null && !sessionToken.isEmpty()) {
                // Create minimal player object
                Player player = new Player();
                player.playerID = playerID.value;
                player.username = username;

                // Store session
                PlayerClient_Java.setLoggedInPlayer(player);
                PlayerClient_Java.setSessionToken(sessionToken);

                // Redirect
                redirectToGameLobby();
            }
        } catch (AuthenticationException e) {
            e.printStackTrace();
        } catch (AlreadyLoggedInException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void handleQuitButton() {
        System.out.println("[INFO] Exiting application...");
        Platform.exit();
    }

    private void redirectToGameLobby() {
        try {
            File fxmlFile = new File("src/main/java/Client_Java/player/res/fxml/WWGameLobbyPage.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
            Parent root = loader.load();

            Scene lobbyScene = new Scene(root);
            PlayerClient_Java.getStage().setScene(lobbyScene);
            PlayerClient_Java.getStage().setTitle("What's the Word - Game Lobby");
            PlayerClient_Java.getStage().show();

            System.out.println("[INFO] Redirected to Game Lobby.");

        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load Game Lobby FXML.");
            e.printStackTrace();
        }
    }

}
