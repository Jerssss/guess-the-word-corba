package Client_Java.player.implementation;

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
        // Debug trace at the very start of the callback
        System.out.println("[DEBUG][LoginCallBackService] notifyForcedLogout() called"
                + " playerID=" + playerID
                + ", sessionToken=" + sessionToken
        );

        Platform.runLater(() -> {
            System.out.println("[DEBUG][LoginCallBackService][UI] showing forced logout alert");

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Session Expired");

            // Custom header
            Label headerLabel = new Label("Your session has been terminated");
            headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

            // Custom content
            Label contentLabel = new Label(
                    "You have been signed out because your account was accessed from another device or location.\n\n" +
                            "Please log in again to continue."
            );
            contentLabel.setWrapText(true);

            VBox content = new VBox(10, headerLabel, contentLabel);
            content.setPrefWidth(400);
            alert.getDialogPane().setContent(content);

            // Apply custom styling with red theme
            alert.getDialogPane().setStyle(
                    "-fx-background-color: #ffffff; " +
                            "-fx-border-color: #ff0000; " + // Changed to red
                            "-fx-border-width: 2px; " +
                            "-fx-border-radius: 5px;"
            );

            // Style the button with red theme
            alert.getDialogPane().lookupButton(ButtonType.OK).setStyle(
                    "-fx-background-color: #ff0000; " + // Changed to red
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 8px 16px; " +
                            "-fx-cursor: hand;"
            );

            // Ensure minimum size for better appearance
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

            alert.showAndWait();

            try {
                System.out.println("[DEBUG][LoginCallBackService][UI] navigating back to Login view");
                ViewNavigator.goToLogin();
            } catch (Exception e) {
                System.err.println("[ERROR][LoginCallBackService] failed to navigate to login: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}