// File: Client_Java/player/implementation/LoginCallBackServiceImpl.java
package Client_Java.player.implementation;

import Client_Java.player.view.ViewNavigator;
import Server_Java.idls.PlayerCallBackIDL.LoginCallbackServicePOA;
import Server_Java.idls.PlayerCallBackIDL.NotLoggedInException;
import javafx.application.Platform;
import javafx.scene.control.Alert;

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
            System.out.println("[DEBUG][LoginCallBackService][UI] showing forced-logout alert");
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Logged Out");
            alert.setHeaderText(null);
            alert.setContentText(
                    "You have been logged out because you signed in elsewhere.\n"
                            + "playerID=" + playerID
                            + ", sessionToken=" + sessionToken
            );
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
