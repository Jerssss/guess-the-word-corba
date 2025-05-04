package Client_Java.admin;

import Client_Java.admin.controller.AdminLogInPageController;
import Client_Java.admin.model.AdminClientModel;
import Client_Java.admin.model.AdminLogInPageModel;
import Client_Java.admin.view.AdminLogInPageView;
import Shared_Files.AdminAccount;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class AdminClient_Java extends Application {
    public static Stage APPLICATION_STAGE;
    private static AdminAccount loggidInAdmin;
    private static long loggedInAdminID;
    private static String sessionToken;

    public static void main(String[] args) {
        AdminClientModel clientModel = new AdminClientModel();
        clientModel.init(); // Initialize CORBA connections
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        APPLICATION_STAGE = stage;
        loadLoginGUI();
    }

    private void loadLoginGUI() {
        try {
            File fxmlFile = new File("src/main/resources/fxml/admin/AdminLogInPage.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
            Parent root = loader.load();

            AdminLogInPageView loginPageView = loader.getController();
            if (loginPageView == null) {
                System.err.println("[ERROR] AdminLoginPageView is NULL after FXML load!");
            } else {
                System.out.println("[DEBUG] AdminLoginPageView controller loaded successfully.");
                AdminLogInPageModel model = new AdminLogInPageModel(AdminClientModel.authService);
                new AdminLogInPageController(model, loginPageView);
            }

            Scene scene = new Scene(root);
            APPLICATION_STAGE.setScene(scene);
            APPLICATION_STAGE.centerOnScreen();
            APPLICATION_STAGE.setResizable(false);

            APPLICATION_STAGE.setOnCloseRequest(event -> {
                System.out.println("[INFO] Application is closing...");
                System.exit(0);
            });

            APPLICATION_STAGE.setTitle("What's The Word!!");
            APPLICATION_STAGE.show();

            System.out.println("[Client] LOGIN GUI LOADED SUCCESSFULLY");

        } catch (IOException e) {
            System.err.println("[ERROR] IOException while loading GUI: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[ERROR] Unexpected exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Session management methods
    public static void setLoggidInAdmin(AdminAccount admin) {
        loggidInAdmin = admin;
    }

    public static AdminAccount getLoggidInAdmin() {
        return loggidInAdmin;
    }

    public static void setLoggedInAdminID(long id) {
        loggedInAdminID = id;
    }

    public static long getLoggedInAdminID() {
        return loggedInAdminID;
    }

    public static void setSessionToken(String token) {
        sessionToken = token;
    }

    public static String getSessionToken() {
        return sessionToken;
    }

    public static Stage getStage() {
        return APPLICATION_STAGE;
    }
}