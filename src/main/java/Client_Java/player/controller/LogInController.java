// Updated LogInController.java
package Client_Java.player.controller;

import Client_Java.player.PlayerClient_Java;
import Client_Java.player.PlayerClient_Model;
import Client_Java.player.model.LogInPageModel;
import Client_Java.player.view.LoginPageView;
import Client_Java.player.view.GameLobbyView;
import Client_Java.player.model.GameLobbyModel;
import Server_Java.implementation.LoginCallBackServiceImpl;
import Shared_Files.PlayerAccount;
import Server_Java.idls.PlayerCallBackIDL.LoginCallbackService;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Controller wires up view events, model login logic, and forced-logout callbacks.
 */
public class LogInController {
    private final LogInPageModel model;
    private final LoginPageView view;

    public LogInController(LogInPageModel model, LoginPageView view) {
        this.model = model;
        this.view = view;
        this.view.setActionContinueButton(this::handleContinueButton);
        this.view.setActionQuitButton(event -> Platform.exit());
    }

    private void handleContinueButton(ActionEvent event) {
        String username = view.getUsernameField().getText();
        String password = view.getPasswordField().getText();

        if (username.isEmpty() || password.isEmpty()) {
            view.setPromptLabel("Username or password cannot be empty!");
            view.setPromptLabelVisible(true);
            return;
        }

        try {
            // 1. Create client-side callback servant and register with ORB
            //    (Assumes PlayerClient_Java exposes ORB and POA setup)
            LoginCallBackServiceImpl callbackImpl = new LoginCallBackServiceImpl();
            LoginCallbackService callbackStub = PlayerClient_Model.registerCallback(callbackImpl);

            // 2. Perform login through model
            boolean success = model.login(username, password, callbackStub);

            if (success) {
                view.setPromptLabel("Login Successful! Redirecting...");
                view.setPromptLabelVisible(true);
                System.out.println("[Client] Logged In Token: " + PlayerClient_Java.getSessionToken());

                // Navigate to game lobby
                redirectToGameLobby(PlayerClient_Java.getLoggedInPlayer());
            } else {
                view.setPromptLabel("Login Failed. Please try again!");
                view.setPromptLabelVisible(true);
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Login Exception: " + e.getMessage());
            view.setPromptLabel("Unexpected error! Please try again...");
            view.setPromptLabelVisible(true);
            e.printStackTrace();
        }
    }

    private void redirectToGameLobby(PlayerAccount player) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/player/GameLobbyPage.fxml"));
            Parent root = loader.load();

            GameLobbyView lobbyView = loader.getController();
            GameLobbyModel lobbyModel = new GameLobbyModel(player);
            new GameLobbyController(
                    lobbyModel, lobbyView, player,
                    0, PlayerClient_Java.getSessionToken());

            Scene scene = new Scene(root);
            URL css = getClass().getClassLoader().getResource("css/styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            Stage stage = PlayerClient_Java.getStage();
            Platform.runLater(() -> {
                stage.setScene(scene);
                stage.setTitle("What's the Word - Lobby");
                stage.show();
            });
        } catch (IOException ex) {
            System.err.println("[ERROR] Failed to load game lobby: " + ex.getMessage());
            view.setPromptLabel("Failed to load Game Lobby!");
            view.setPromptLabelVisible(true);
        }
    }
}

