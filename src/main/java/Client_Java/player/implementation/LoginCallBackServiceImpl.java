// File: Client_Java/player/implementation/LoginCallBackServiceImpl.java
package Client_Java.player.implementation;

import Client_Java.player.SessionManager;
import Client_Java.player.view.ViewNavigator;
import PlayerCallBackIDL.LoginCallbackServicePOA;
import PlayerCallBackIDL.NotLoggedInException;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class LoginCallBackServiceImpl extends LoginCallbackServicePOA {

    @Override
    public void notifyForcedLogout(int playerID, String sessionToken)
            throws NotLoggedInException {
        System.out.println("[DEBUG][LoginCallBackService] notifyForcedLogout()"
                + " playerID=" + playerID
                + ", sessionToken=" + sessionToken
        );

        Platform.runLater(() -> {
            System.out.println("[DEBUG][LoginCallBackService][UI] showing forced logout alert");

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Session Expired");

            Label headerLabel = new Label("Your session has been terminated");
            headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

            Label contentLabel = new Label(
                    "You have been signed out because your account was accessed from another device or location.\n\n" +
                            "Please log in again to continue."
            );
            contentLabel.setWrapText(true);

            VBox content = new VBox(10, headerLabel, contentLabel);
            content.setPrefWidth(400);
            alert.getDialogPane().setContent(content);
            alert.getDialogPane().setStyle(
                    "-fx-background-color: #ffffff; " +
                            "-fx-border-color: #ff0000; " +
                            "-fx-border-width: 2px; " +
                            "-fx-border-radius: 5px;"
            );
            alert.getDialogPane().lookupButton(ButtonType.OK).setStyle(
                    "-fx-background-color: #ff0000; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 8px 16px; " +
                            "-fx-cursor: hand;"
            );
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

            alert.showAndWait();

            // ---- NEW: reset client session state ----
            System.out.println("[DEBUG][LoginCallBackService] clearing client session");
            SessionManager.clearSession();

            try {
                System.out.println("[DEBUG][LoginCallBackService][UI] navigating back to Login view");
                ViewNavigator.goToLogin();
            } catch (Exception e) {
                System.err.println("[ERROR][LoginCallBackService] navigation failed: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
