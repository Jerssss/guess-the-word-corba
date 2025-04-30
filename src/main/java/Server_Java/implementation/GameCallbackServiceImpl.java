package Server_Java.implementation;

import Client_Java.player.PlayerClient_Java;
import Client_Java.player.controller.GameRoomController;
import Client_Java.player.model.GameRoomModel;
import Client_Java.player.view.modals.GameWinnerPopupView;
import Client_Java.player.view.modals.RoundWinnerPopupView;
import Server_Java.idls.PlayerCallBackIDL.GameCallBackServicePOA;
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
    private final GameRoomModel       model;
    private final String              sessionToken;
    private final GameRoomController  controller;

    /**
     * @param model         allows fetching settings (e.g. next‐round delay)
     * @param sessionToken  your CORBA session token
     * @param controller    the UI controller to update when server pushes events
     */
    public GameCallbackServiceImpl(GameRoomModel model,
                                   String sessionToken,
                                   GameRoomController controller) {
        this.model        = model;
        this.sessionToken = sessionToken;
        this.controller   = controller;
    }

    @Override
    public void notifyGameStart(String gameToken, String sessionToken) {

    }

    @Override
    public void notifyRoundStart(String gameToken, int roundNumber,String ignoredSessionToken) { Platform.runLater(() -> {
            controller.handleServerRoundStart(roundNumber);
        });
    }

    @Override
    public void notifyRoundEnd(String gameToken,
                               String ignoredSessionToken,
                               String winnerName) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader;
                if (winnerName == null || winnerName.isEmpty()) {
                    loader = new FXMLLoader(
                            getClass().getResource("/fxml/player/NoWinnerPopup.fxml")
                    );
                } else {
                    loader = new FXMLLoader(
                            getClass().getResource("/fxml/player/RoundWinnerPopup.fxml")
                    );
                }
                Parent root = loader.load();
                Stage popup = new Stage(StageStyle.TRANSPARENT);
                popup.initOwner(PlayerClient_Java.getStage());
                popup.initModality(Modality.NONE);
                popup.setScene(new Scene(root));

                // center the popup
                Stage main = PlayerClient_Java.getStage();
                root.applyCss(); root.layout();
                popup.setX(main.getX() + (main.getWidth()  - root.prefWidth(-1)) / 2);
                popup.setY(main.getY() + (main.getHeight() - root.prefHeight(-1)) / 2);

                popup.show();

                // auto-close after delay
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
    public void notifyGameEnd(String gameToken,
                              String ignoredSessionToken,
                              String winnerName) {
        System.out.println("[Client Callback] >> ENTER notifyGameEnd("
                + "gameToken=" + gameToken
                + ", sessionToken=" + ignoredSessionToken
                + ", winner=" + winnerName
                + ") on thread=" + Thread.currentThread().getName());
        Platform.runLater(() -> {
            System.out.println("[Client Callback]    running UI code for notifyGameEnd");
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/player/GameWinnerPopup.fxml")
                );
                Parent root = loader.load();

                GameWinnerPopupView ctrl = loader.getController();
                ctrl.setGameTitle("Game Winner!");
                ctrl.setWinningUsername(winnerName);
                System.out.println("[Client Callback] Loaded GameWinnerPopupView.fxml.");

                Stage stage = PlayerClient_Java.getStage();
                stage.setScene(new Scene(root));
                stage.show();
                System.out.println("[Client Callback] Scene set to GameWinnerPopupView.");

                PauseTransition wait = new PauseTransition(
                        Duration.seconds(model.getNextRoundDelay(sessionToken))
                );
                wait.setOnFinished(e -> {
                    System.out.println("[Client Callback] Delay elapsed, invoking controller.handleGameEnd()");
                    controller.handleGameEnd();
                });
                wait.play();
            } catch (Exception e) {
                System.err.println("[Client Callback] Exception in notifyGameEnd:");
                e.printStackTrace();
            }
        });
    }





}
