package Client_Java.admin.controller;

import Client_Java.admin.AdminClient_Java;
import Client_Java.admin.model.AdminClientModel;
import Client_Java.admin.model.AdminLogInPageModel;
import Client_Java.admin.model.AdminMainMenuPageModel;
import Client_Java.admin.view.AdminLogInPageView;
import Client_Java.admin.view.AdminMainMenuPageView;
import Shared_Files.AdminAccount;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
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
            File fxmlFile = new File("src/main/resources/fxml/admin/AdminMainMenuPage.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
            Parent root = loader.load();

            AdminMainMenuPageView view = loader.getController();
            if (view == null) {
                System.err.println("[ERROR] AdminMainMenuPageView is NULL after FXML load!");
                return;
            } else {
                System.out.println("[DEBUG] AdminMainMenuPageView loaded successfully.");

                AdminMainMenuPageModel model = new AdminMainMenuPageModel(
                        AdminClientModel.adminService,
                        AdminClient_Java.getSessionToken(),
                        admin.getAdmin_id()
                );
                new AdminMainMenuPageController(view, model);
            }

            Scene scene = new Scene(root);
            URL css = getClass().getClassLoader().getResource("css/styles.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }

            Stage stage = AdminClient_Java.getStage();
            Platform.runLater(() -> {
                stage.setScene(scene);
                stage.centerOnScreen();
                stage.setResizable(false);
                stage.setTitle("Admin Main Menu");
                stage.show();
            });

            System.out.println("[Client] Admin Main Menu GUI loaded successfully.");

        } catch (IOException e) {
            System.err.println("[ERROR] IOException while loading AdminMainMenuPage: " + e.getMessage());
            e.printStackTrace();
            view.setPromptLabel("Failed to load Admin Main Menu!");
            view.setPromptLabelVisible(true);
        } catch (Exception e) {
            System.err.println("[ERROR] Unexpected exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
