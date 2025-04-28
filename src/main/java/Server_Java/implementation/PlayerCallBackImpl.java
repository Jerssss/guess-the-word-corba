package Server_Java.implementation;

import Client_Java.player.PlayerClient_Java;
import IDL_Files.PlayerCallBackIDL.NotLoggedInException;
import IDL_Files.PlayerCallBackIDL.PlayerCallBackServicePOA;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class PlayerCallBackImpl extends PlayerCallBackServicePOA {
    @Override
    public void notifyRoundEnd(String gameToken, String sessionToken, String result) {

    }

    @Override
    public void notifyGameEnd(String gameToken, String sessionToken, String result) {

    }

    @Override
    public void notifyForcedLogout(int playerID, String sessionToken) throws NotLoggedInException {
        System.out.println("[Client] Forced Logout Detected!");

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_Java/player/res/fxml/LogInPage.fxml"));
                Parent root = loader.load();

                Scene loginScene = new Scene(root);

                try {
                    URL stylesheet = getClass().getClassLoader().getResource("Client_Java/player/res/css/styles.css");
                    if (stylesheet != null) {
                        loginScene.getStylesheets().add(stylesheet.toExternalForm());
                    } else {
                        System.out.println("Stylesheet not found during forced logout.");
                    }
                } catch (Exception e) {
                    System.err.println("[Client ERROR] Stylesheet Load Error: " + e.getMessage());
                }

                Stage stage = PlayerClient_Java.getStage();
                stage.setScene(loginScene);
                stage.setTitle("What's The Word - Login");
                stage.show();

                System.out.println("[Client] Redirected to Login Screen after Forced Logout!");

            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("[Client ERROR] Failed to reload Login FXML after forced logout.");
            }
        });
    }
}

