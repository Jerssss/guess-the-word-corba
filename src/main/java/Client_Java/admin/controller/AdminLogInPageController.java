package Client_Java.admin.controller;




import Client_Java.admin.AdminClient_Java;
import Client_Java.admin.model.AdminLogInPageModel;
import Client_Java.admin.view.AdminLogInPageView;
import Client_Java.player.PlayerClient_Java;
import Client_Java.player.controller.GameLobbyController;
import Client_Java.player.model.GameLobbyModel;
import Client_Java.player.view.GameLobbyView;
import Shared_Files.AdminAccount;
import Shared_Files.PlayerAccount;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class AdminLogInPageController {
    private final AdminLogInPageView view;
    private final AdminLogInPageModel model;

    public AdminLogInPageController(AdminLogInPageModel model, AdminLogInPageView view) {
        this.view = view;
        this.model = model;

        this.view.setActionContinueButton(event -> handleContinueButton());
        this.view.setActionQuitButton(event -> Platform.exit());
    }

    private void handleContinueButton() {
        String username = view.getUsernameField().getText();
        String password = view.getPasswordField().getText();

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

                System.out.println("[Client] Logged In: " + AdminClient_Java.getSessionToken());

                redirectToAdminMainMenu(AdminClient_Java.getLoggidInAdmin());
            } else {
                view.setPromptLabel("Login Failed. Please try again!!");
                view.setPromptLabelVisible(true);
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Login Exception: " + e.getMessage());
            view.setPromptLabel("Unexpected Error! Please try again...");
            view.setPromptLabelVisible(true);
            e.printStackTrace();
        }
    }

    private void redirectToAdminMainMenu(AdminAccount admin) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/AdminMainMenuPage.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            URL css = getClass().getClassLoader().getResource("css/styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            Stage stage = AdminClient_Java.getStage();
            Platform.runLater(() -> {
                stage.setScene(scene);
                stage.setTitle("Admin Main Menu");
                stage.show();
            });

        } catch (IOException ex) {
            System.err.println("[ERROR] Failed to load game lobby: " + ex.getMessage());
            view.setPromptLabel("Failed to load Game Lobby!");
            view.setPromptLabelVisible(true);
        }
    }
}



