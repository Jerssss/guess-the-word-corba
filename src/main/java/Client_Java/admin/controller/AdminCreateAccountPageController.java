package Client_Java.admin.controller;

import Client_Java.admin.AdminClient_Java;
import Client_Java.admin.model.AdminClientModel;
import Client_Java.admin.model.AdminCreateAccountPageModel;
import Client_Java.admin.model.AdminMainMenuPageModel;
import Client_Java.admin.view.AdminCreateAccountPageView;
import AdminIDL.AccountExistsException;
import AdminIDL.NotLoggedInException;
import Client_Java.admin.view.AdminMainMenuPageView;
import Shared_Files.AdminAccount;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class AdminCreateAccountPageController {

    private final AdminCreateAccountPageView view;
    private final AdminCreateAccountPageModel model;

    public AdminCreateAccountPageController(AdminCreateAccountPageView view, AdminCreateAccountPageModel model) {
        this.view = view;
        this.model = model;
        setupListeners();
    }

    private void setupListeners() {
        view.setActionSaveButton(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleCreatePlayer();
            }
        });

        view.setActionReturnButton(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // Placeholder: navigate back to admin menu, or switch scenes
                view.setNoticeLabelText("Returning...");
                view.setNoticeVisible(true);
                redirectToAdminMainMenu(AdminClient_Java.getLoggidInAdmin());
            }
        });
    }

    private void handleCreatePlayer() {
        String fullname = view.getFullnameTextField().getText().trim();
        String username = view.getUsernameTextField().getText().trim();
        String password = view.getPasswordTextField().getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            view.setNoticeLabelText("Fields must not be empty.");
            view.setNoticeVisible(true);
            return;
        }

        try {
            model.createPlayer(fullname, username, password);
            view.setNoticeLabelText("Player account created successfully.");
            view.setNoticeVisible(true);
        } catch (AccountExistsException e) {
            view.setNoticeLabelText("Error: Account already exists.");
            view.setNoticeVisible(true);
        } catch (NotLoggedInException e) {
            view.setNoticeLabelText("Error: You are not logged in.");
            view.setNoticeVisible(true);
        } catch (Exception e) {
            view.setNoticeLabelText("Unexpected error occurred.");
            view.setNoticeVisible(true);
        }
    }

    private void redirectToAdminMainMenu(AdminAccount admin) {
        try {
            File fxmlFile = new File("src/main/resources/fxml/admin/AdminMainMenuPage.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
            Parent root = loader.load();

            AdminMainMenuPageView pageView = loader.getController();
            if (pageView == null) {
                System.err.println("[ERROR] AdminMainMenuPageView is NULL after FXML load!");
                return;
            } else {
                System.out.println("[DEBUG] AdminMainMenuPageView loaded successfully.");
            }

            AdminMainMenuPageModel pageModel = new AdminMainMenuPageModel(
                    AdminClientModel.adminService,
                    AdminClient_Java.getSessionToken(),
                    admin.getAdmin_id()
            );

            new AdminMainMenuPageController(
                    pageView,
                    pageModel,
                    AdminClientModel.adminService,
                    AdminClient_Java.getSessionToken(),
                    admin.getAdmin_id()
            );

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
            view.setNoticeLabelText("Failed to load Admin Main Menu!");
            view.setNoticeVisible(true);
        } catch (Exception e) {
            System.err.println("[ERROR] Unexpected exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}