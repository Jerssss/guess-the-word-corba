package Server_Java.implementation;

import Client_Java.player.PlayerClient_Java;
import Client_Java.player.model.GameRoomModel;
import Client_Java.player.model.WaitingRoomModel;
import Client_Java.player.view.modals.RoundWinnerPopupView;
import Server_Java.idls.PlayerCallBackIDL.GameCallBackServicePOA;
import Server_Java.idls.PlayerCallBackIDL.WaitingRoomGameCallbackService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class GameCallbackServiceImpl extends GameCallBackServicePOA {
    private GameRoomModel model = null;

    private String sessionToken = "";

    public GameCallbackServiceImpl(GameRoomModel model, String sessionToken) {
        this.model = model;
        this.sessionToken = sessionToken;
    }



    @Override
    public void notifyGameStart(String gameToken, String sessionToken) {
        // existing logic…
    }

    @Override
    public void notifyRoundStart(String gameToken, int roundNumber, String sessionToken) {
        // existing logic…
    }

    @Override
    public void notifyRoundEnd(String gameToken,
                               String ignoredSessionToken,
                               String winnerName) {
        // Show popup on JavaFX thread
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/player/RoundWinnerPopup.fxml")
                );
                Parent root = loader.load();
                RoundWinnerPopupView ctrl = loader.getController();
                ctrl.setWinnerName(winnerName);

                Stage popup = new Stage(StageStyle.TRANSPARENT);
                popup.initOwner(PlayerClient_Java.getStage());
                popup.initModality(Modality.NONE);
                popup.setScene(new Scene(root));

                // Center over main window
                Stage main = PlayerClient_Java.getStage();
                root.applyCss(); root.layout();
                double x = main.getX() + (main.getWidth()  - root.prefWidth(-1)) / 2;
                double y = main.getY() + (main.getHeight() - root.prefHeight(-1)) / 2;
                popup.setX(x);
                popup.setY(y);

                popup.show();

                // Auto-close after configured delay
                int delay = model.getNextRoundDelay(sessionToken);
                PauseTransition wait = new PauseTransition(Duration.seconds(delay));
                wait.setOnFinished(e -> popup.close());
                wait.play();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void notifyGameEnd(String gameToken, String sessionToken, String result) {
        // final game-end popup or navigation
    }
}
