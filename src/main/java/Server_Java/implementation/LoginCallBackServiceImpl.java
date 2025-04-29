package Server_Java.implementation;


import Client_Java.player.PlayerClient_Java;
import Server_Java.idls.PlayerCallBackIDL.LoginCallbackServicePOA;
import Server_Java.idls.PlayerCallBackIDL.NotLoggedInException;
import javafx.application.Platform;
import javafx.scene.control.Alert;

public class LoginCallBackServiceImpl extends LoginCallbackServicePOA {

    @Override
    public void notifyForcedLogout(int playerID, String sessionToken) throws NotLoggedInException {
        System.out.println("[Callback] Forced logout received for player " + playerID);
        Platform.runLater(() -> {
            // 1) Show an alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Logged Out");
            alert.setHeaderText(null);
            alert.setContentText("You have been logged out because you signed in elsewhere.");
            alert.showAndWait();

            // 2) Clear client session state and return to login screen
            PlayerClient_Java.handleForcedLogout();
        });
    }
}

