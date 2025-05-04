// File: AdminMainMenuPageController.java
package Client_Java.admin.controller;

import Client_Java.admin.model.AdminMainMenuPageModel;
import Client_Java.admin.view.AdminMainMenuPageView;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class AdminMainMenuPageController {

    private final AdminMainMenuPageView view;
    private final AdminMainMenuPageModel model;

    public AdminMainMenuPageController(AdminMainMenuPageView view, AdminMainMenuPageModel model) {
        this.view = view;
        this.model = model;
        initializeButtonActions();
    }

    private void initializeButtonActions() {
        view.setActionCreateAccountButton(this::handleCreateAccount);
        view.setActionEditConfigButton(this::handleEditConfig);
        view.setActionViewPlayersButton(this::handleViewPlayers);
        view.setActionQuitButton(this::handleQuit);
    }

    private void handleCreateAccount(ActionEvent event) {
        loadPage("/fxml/admin/AdminCreatePlayerPage.fxml");
    }

    private void handleEditConfig(ActionEvent event) {
        loadPage("/fxml/admin/AdminEditConfigurationsPage.fxml");
    }

    private void handleViewPlayers(ActionEvent event) {
        loadPage("/fxml/admin/AdminPlayerListPage.fxml");
    }

    private void loadPage(String fxmlResource) {
        try {
            URL resourceUrl = getClass().getResource(fxmlResource);
            if (resourceUrl == null) {
                System.err.println("[ERROR] FXML resource not found: " + fxmlResource);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent loadedView = loader.load();

            if (view.getPane() instanceof BorderPane) {
                ((BorderPane) view.getPane()).setCenter(loadedView);
            } else {
                System.err.println("[ERROR] Main pane is not a BorderPane. Cannot set center.");
            }

            System.out.println("[INFO] Loaded page: " + fxmlResource);
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load page: " + fxmlResource + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleQuit(ActionEvent event) {
        try {
            URL resourceUrl = getClass().getResource("/fxml/admin/AdminLogInPage.fxml");
            if (resourceUrl == null) {
                System.err.println("[ERROR] Admin Login FXML not found.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent loginView = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loginView));
            stage.centerOnScreen();
            stage.setResizable(false);
            stage.show();

            System.out.println("[INFO] Admin logged out. Returning to login screen.");
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load Admin Login Page: " + e.getMessage());
            e.printStackTrace();
        }
    }
}