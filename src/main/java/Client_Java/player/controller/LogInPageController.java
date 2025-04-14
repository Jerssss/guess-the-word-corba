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
            System.err.println("[ERROR] Username or password is empty.");
            return;
        }

        try {
            org.omg.CORBA.IntHolder playerIDHolder = new org.omg.CORBA.IntHolder();
            String sessionToken = model.login(username, password, playerIDHolder);
            long playerID = playerIDHolder.value;

            // Save logged-in session data
            PlayerClient_Java.setLoggedInPlayerID(playerID);
            PlayerClient_Java.setSessionToken(sessionToken);

            System.out.println("[INFO] Login successful. Session Token: " + sessionToken);

            // Redirect to Game Lobby
            redirectToGameLobby();

        } catch (AuthenticationException e) {
            System.err.println("[AUTH FAILED] Invalid credentials. Try Again...");
        } catch (AlreadyLoggedInException e) {
            System.err.println("[AUTH FAILED] Account is already logged in. Try Again...");
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
