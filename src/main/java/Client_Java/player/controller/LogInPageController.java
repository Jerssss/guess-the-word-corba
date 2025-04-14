package Client_Java.player.controller;

import Client_Java.PlayerClient_Java;
import Client_Java.player.controller.GameLobbyPageController;
import Client_Java.player.model.GameLobbyModel;
import Client_Java.player.model.LogInPageModel;
import Client_Java.player.view.GameLobbyPageView;
import Client_Java.player.model.PlayerClient_Model;
import Client_Java.player.view.LoginPageView;
import PlayerGame.AlreadyLoggedInException;
import PlayerGame.AuthenticationException;
import PlayerGame.Player;
import Server_Java.database.PlayerQueries;
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
        this.view.setActionQuitButton(event -> handleQuitButton());
    }

    private void handleContinueButton() {
        String username = view.getUsernameField().getText().trim();
        String password = view.getPasswordField().getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            view.setPromptLabel("Username or password cannot be empty!");
            view.setPromptLabelVisible(true);
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
            // Load player data and redirect
            Player player = PlayerQueries.getPlayer((int)playerID); // Add this method to PlayerQueries
            redirectToGameLobby(player);
            // Start background session checker
            new Thread(new Client_Java.util.SessionChecker(PlayerClient_Model.gameService)).start();

            // Redirect to Game Lobby
            redirectToGameLobby(player);

        } catch (AuthenticationException e) {
            view.setPromptLabel("Invalid credentials. Try again...");
            view.setPromptLabelVisible(true);
        } catch (AlreadyLoggedInException e) {
            view.setPromptLabel("Account already logged in!");
            view.setPromptLabelVisible(true);
            System.err.println("[AUTH FAILED] Account was logged in elsewhere, but you are now logged in.");
        } catch (Exception e) {
            view.setPromptLabel("Login error occurred!");
            view.setPromptLabelVisible(true);
            e.printStackTrace();
        }
    }

    private void redirectToGameLobby(Player player) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_Java/player/res/fxml/WWGameLobbyPage.fxml"));
            Parent root = loader.load();

            // Get the view and controller
            GameLobbyPageView lobbyView = loader.getController();
            GameLobbyModel lobbyModel = new GameLobbyModel(player);
            new GameLobbyPageController(lobbyModel, lobbyView, player);

            // Create scene with stylesheet
            Scene lobbyScene = new Scene(root);

            // Load stylesheet safely
            try {
                URL stylesheet = getClass().getResource("/Client_Java/player/res/css/styles.css");
                if (stylesheet != null) {
                    lobbyScene.getStylesheets().add(stylesheet.toExternalForm());
                } else {
                    System.out.println("Stylesheet not found, using default styling");
                }
            } catch (Exception e) {
                System.err.println("Error loading stylesheet: " + e.getMessage());
            }

            // Update stage
            Stage stage = PlayerClient_Java.getStage();
            Platform.runLater(() -> {
                stage.setScene(lobbyScene);
                stage.setTitle("What's the Word - Game Lobby");
                stage.show();
            });

        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load Game Lobby FXML.");
            e.printStackTrace();
            view.setPromptLabel("Failed to load game lobby!");
            view.setPromptLabelVisible(true);
        }
    }

    private void handleQuitButton() {
        System.out.println("[INFO] Exiting application...");
        Platform.exit();
    }
}