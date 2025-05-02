package Server_Java.implementation;

import Client_Java.player.PlayerClient_Java;
import Client_Java.player.controller.GameRoomController;
import Client_Java.player.model.GameRoomModel;
import Client_Java.player.view.modals.GameRoundPopupView;
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

import java.net.URL;

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
    public void notifyRoundStart(String gameToken,
                                 int roundNumber,
                                 String ignoredSessionToken) {
        Platform.runLater(() -> {
            try {
                // 1) Locate the FXML
                URL fxmlUrl = getClass().getResource("/fxml/player/GameRoundPopup.fxml");
                if (fxmlUrl == null) {
                    System.err.println("[Client Callback] Cannot find GameRoundPopup.fxml on classpath!");
                    // Fall back to immediately starting the round
                    controller.handleServerRoundStart(roundNumber);
                    return;
                }

                // 2) Load it
                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Parent root = loader.load();

                // 3) Configure controller
                GameRoundPopupView popupCtrl = loader.getController();
                popupCtrl.setGameTitle("What’s The Word?");
                popupCtrl.setRoundNumber(roundNumber);

                // 4) Show popup
                Stage popup = new Stage(StageStyle.TRANSPARENT);
                popup.initOwner(PlayerClient_Java.getStage());
                popup.initModality(Modality.NONE);
                popup.setScene(new Scene(root));

                // Center it
                Stage main = PlayerClient_Java.getStage();
                root.applyCss(); root.layout();
                popup.setX(main.getX() + (main.getWidth()  - root.prefWidth(-1)) / 2);
                popup.setY(main.getY() + (main.getHeight() - root.prefHeight(-1)) / 2);
                popup.show();

                // 5) Auto‑close after your next‑round delay, then start
                int delay = model.getNextRoundDelay(sessionToken);
                PauseTransition wait = new PauseTransition(Duration.seconds(delay));
                wait.setOnFinished(e -> {
                    popup.close();
                    controller.handleServerRoundStart(roundNumber);
                });
                wait.play();

            } catch (Exception e) {
                e.printStackTrace();
                // On any error, don’t block game start
                controller.handleServerRoundStart(roundNumber);
            }
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
        System.out.println("[Client Callback][notifyGameEnd] ENTER: gameToken="
                + gameToken + ", sessionToken=" + ignoredSessionToken
                + ", winnerName=" + winnerName);
        Platform.runLater(() -> {
            try {
                // 1) Find FXML
                URL fxmlUrl = getClass().getResource("/fxml/player/GameWinnerPopup.fxml");
                System.out.println("[Client Callback][notifyGameEnd] FXML URL = " + fxmlUrl);
                if (fxmlUrl == null) {
                    System.err.println("[Client Callback][notifyGameEnd] ERROR: GameWinnerPopup.fxml not found");
                    controller.handleGameEnd();
                    return;
                }

                // 2) Load FXML
                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                System.out.println("[Client Callback][notifyGameEnd] About to loader.load()");
                Parent root = loader.load();
                System.out.println("[Client Callback][notifyGameEnd] loader.load() succeeded");

                // 3) Get controller
                GameWinnerPopupView ctrl = loader.getController();
                System.out.println("[Client Callback][notifyGameEnd] Controller = " + ctrl);
                ctrl.setGameTitle("Game Winner!");
                ctrl.setWinningUsername(winnerName);
                System.out.println("[Client Callback][notifyGameEnd] setWinningUsername done");

                // 4) Create popup stage
                Stage popup = new Stage(StageStyle.TRANSPARENT);
                popup.initOwner(PlayerClient_Java.getStage());
                popup.initModality(Modality.APPLICATION_MODAL);
                popup.setScene(new Scene(root));

                // 5) Center
                Stage main = PlayerClient_Java.getStage();
                root.applyCss(); root.layout();
                double x = main.getX() + (main.getWidth()  - root.prefWidth(-1)) / 2;
                double y = main.getY() + (main.getHeight() - root.prefHeight(-1)) / 2;
                popup.setX(x);
                popup.setY(y);
                System.out.println(String.format("[Client Callback][notifyGameEnd] Popup positioned at (%.1f, %.1f)", x, y));

                // 6) Show
                popup.show();
                System.out.println("[Client Callback][notifyGameEnd] Popup.show() called");

                // 7) Auto‑close after delay
                int delay = model.getNextRoundDelay(sessionToken);
                System.out.println("[Client Callback][notifyGameEnd] Auto‑close delay = " + delay + "s");
                PauseTransition wait = new PauseTransition(Duration.seconds(delay));
                wait.setOnFinished(e -> {
                    System.out.println("[Client Callback][notifyGameEnd] Auto‑close firing now");
                    popup.close();
                    controller.handleGameEnd();
                });
                wait.play();

            } catch (Exception e) {
                System.err.println("[Client Callback][notifyGameEnd] EXCEPTION:");
                e.printStackTrace();
                controller.handleGameEnd();
            }
        });
    }






}
