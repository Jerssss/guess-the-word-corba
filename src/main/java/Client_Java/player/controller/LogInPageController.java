package Client_Java.player.controller;

import Client_Java.player.PlayerClient_Java;
import Client_Java.player.model.GameLobbyModel;
import Client_Java.player.model.LogInPageModel;
import Client_Java.player.view.GameLobbyPageView;
import Client_Java.player.view.LoginPageView;
import Shared_Files.PlayerAccount;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class LogInPageController {
    private final LogInPageModel model;
    private final LoginPageView view;

    public LogInPageController(LogInPageModel model, LoginPageView view) {
        this.model = model;
        this.view = view;

        this.view.setActionContinueButton(event -> handleContinueButton());
        this.view.setActionQuitButton(event -> Platform.exit());
    }

    private void handleContinueButton() {
        String username = view.getUsernameField().getText();
        String password = view.getPasswordField().getText();

        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            view.setPromptLabel("Username or password cannot be empty!");
            view.setPromptLabelVisible(true);
            return;
        }

        try {
            boolean success = model.login(username, password);

            if (success) {
                view.setPromptLabel("Login Successful! Redirecting...");
                view.setPromptLabelVisible(true);

                System.out.println("[Client] Logged In: " + PlayerClient_Java.getSessionToken());

                // Redirect to Game Lobby after successful login
                redirectToGameLobby(PlayerClient_Java.getLoggedInPlayer());
            } else {
                view.setPromptLabel("Login Failed. Please try again!");
                view.setPromptLabelVisible(true);
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Login Exception: " + e.getMessage());
            view.setPromptLabel("Unexpected Error! Please try again...");
            view.setPromptLabelVisible(true);
            e.printStackTrace();
        }
    }

    private void redirectToGameLobby(PlayerAccount player) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/player/WWGameLobbyPage.fxml"));
            Parent root = loader.load();

            GameLobbyPageView lobbyView = loader.getController();
            GameLobbyModel lobbyModel = new GameLobbyModel(player);
            int gid = 0; // Placeholder for gameID
            new GameLobbyPageController(lobbyModel, lobbyView, player, gid, PlayerClient_Java.getSessionToken());

            Scene lobbyScene = new Scene(root);

            try {
                URL stylesheet = getClass().getClassLoader().getResource("css/styles.css");
                if (stylesheet != null) {
                    lobbyScene.getStylesheets().add(stylesheet.toExternalForm());
                } else {
                    System.out.println("Stylesheet not found, using default styling");
                }
            } catch (Exception e) {
                System.err.println("[ERROR] Loading Stylesheet: " + e.getMessage());
            }

            Stage stage = PlayerClient_Java.getStage();
            Platform.runLater(() -> {
                stage.setScene(lobbyScene);
                stage.setTitle("What's the Word - Game Lobby");
                stage.show();
            });

        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load Game Lobby FXML.");
            e.printStackTrace();
            view.setPromptLabel("Failed to load Game Lobby!");
            view.setPromptLabelVisible(true);
        }
    }
}
