package Client_Java.admin.controller;

import Client_Java.admin.model.*;
import Client_Java.admin.view.*;
import AdminIDL.AdminService;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class AdminMainMenuPageController {

    private final AdminMainMenuPageView view;
    private final AdminMainMenuPageModel model;
    private final AdminService adminService;
    private final String sessionToken;
    private final int adminID;

    public AdminMainMenuPageController(AdminMainMenuPageView view, AdminMainMenuPageModel model, AdminService adminService, String sessionToken, int adminID) {
        this.view = view;
        this.model = model;
        this.adminService = adminService;
        this.sessionToken = sessionToken;
        this.adminID = adminID;
        initializeButtonActions();
    }

    private void initializeButtonActions() {
        view.setActionCreateAccountButton(this::handleCreateAccount);
        view.setActionEditConfigButton(this::handleEditConfig);
        view.setActionViewPlayersButton(this::handleViewPlayers);
        view.setActionQuitButton(this::handleQuit);
    }

    private void handleCreateAccount(ActionEvent event) {
        try {
            URL resourceUrl = getClass().getResource("/fxml/admin/AdminCreatePlayerPage.fxml");
            if (resourceUrl == null) {
                throw new IOException("FXML file not found: AdminCreatePlayerPage.fxml");
            }
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent pageView = loader.load();

            AdminCreateAccountPageView pageViewController = loader.getController();
            AdminCreateAccountPageModel pageModel = new AdminCreateAccountPageModel(adminService, sessionToken, adminID);
            new AdminCreateAccountPageController(pageViewController, pageModel);

            setCenterPane(pageView);
        } catch (IOException e) {
            handleLoadError("Create Account Page", e);
        }
    }

    private void handleEditConfig(ActionEvent event) {
        try {
            URL resourceUrl = getClass().getResource("/fxml/admin/AdminEditConfigurationsPage.fxml");
            if (resourceUrl == null) {
                throw new IOException("FXML file not found: AdminEditConfigurationsPage.fxml");
            }
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent pageView = loader.load();

            AdminEditConfigurationsPageView pageViewController = loader.getController();
            AdminEditConfigurationsPageModel pageModel = new AdminEditConfigurationsPageModel(adminService, sessionToken, adminID);
            new AdminEditConfigurationsPageController(pageViewController, pageModel);

            setCenterPane(pageView);
        } catch (IOException e) {
            handleLoadError("Edit Config Page", e);
        }
    }

    private void handleViewPlayers(ActionEvent event) {
        try {
            URL resourceUrl = getClass().getResource("/fxml/admin/AdminPlayerListPage.fxml");
            if (resourceUrl == null) {
                throw new IOException("FXML file not found: AdminPlayerListPage.fxml");
            }
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent pageView = loader.load();

            AdminPlayerListPageView pageViewController = loader.getController();
            AdminPlayerListPageModel pageModel = new AdminPlayerListPageModel(adminService, sessionToken, adminID);
            new AdminPlayerListPageController(pageModel, pageViewController);

            setCenterPane(pageView);
        } catch (IOException e) {
            handleLoadError("View Players Page", e);
        }
    }

    private void handleQuit(ActionEvent event) {
        try {
            URL resourceUrl = getClass().getResource("/fxml/admin/AdminLogInPage.fxml");
            if (resourceUrl == null) {
                throw new IOException("FXML file not found: AdminLogInPage.fxml");
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
            handleLoadError("Login Page", e);
        }
    }

    private void setCenterPane(Parent loadedView) {
        StackPane pane = view.getPane();
        if (pane != null) {
            pane.getChildren().setAll(loadedView); // Replace all children with the new view
        } else {
            System.err.println("[ERROR] StackPane is null. Cannot set new view.");
        }
    }

    private void handleLoadError(String page, Exception e) {
        System.err.println("[ERROR] Failed to load " + page + ": " + e.getMessage());
        e.printStackTrace();
    }
}