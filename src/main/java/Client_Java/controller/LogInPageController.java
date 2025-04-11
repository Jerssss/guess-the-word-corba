package Client_Java.controller;

import Client_Java.Client_Java;
import Client_Java.model.LogInPageModel;
import Client_Java.view.LoginPageView;
import GameApp.AlreadyLoggedInException;
import GameApp.AuthenticationException;
import GameApp.Player;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

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
            Player player = model.login(username, password);
            if (player != null) {
                System.out.println("[INFO] Login successful for: " + player.username);

                // Set logged-in player for use across the app (optional)
                Client_Java.setLoggedInPlayer(player);

                // Load next scene (e.g., MainMenu)
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_Java/res/fxml/WWMainMenu.fxml"));
                Parent root = loader.load();

                Client_Java.getStage().setScene(new Scene(root));
                Client_Java.getStage().setTitle("What's the Word - Main Menu");
                Client_Java.getStage().show();
            }

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
}
