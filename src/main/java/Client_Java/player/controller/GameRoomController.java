// File: Client_Java/player/controller/GameRoomController.java
package Client_Java.player.controller;

import Client_Java.player.PlayerClient_Java;
import Client_Java.player.model.GameRoomModel;
import Client_Java.player.view.GameRoomView;
import Client_Java.player.view.ViewNavigator;
import Client_Java.player.view.modals.GameRoundPopupView;
import Client_Java.player.view.modals.RoundWinnerPopupView;
import Client_Java.player.implementation.GameCallbackServiceImpl;
import Server_Java.idls.GameIDL.NotLoggedInException;
import Server_Java.idls.PlayerCallBackIDL.GameCallBackService;
import Server_Java.idls.PlayerCallBackIDL.GameCallBackServiceHelper;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.omg.CORBA.Object;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameRoomController {
    private final GameRoomModel model;
    private final GameRoomView  view;
    private final int           playerId;
    private final String        sessionToken;
    private final String        gameToken;
    private final int           totalRounds;

    // Which round we’ve already shown
    private int           roundNumber     = 0;
    private String        secretWord      = "";
    private int           remainingLives;
    private final List<Label> blankLabels = new ArrayList<>();

    private Timeline roundTimer;
    private int      secondsRemaining;

    public GameRoomController(
            GameRoomModel model,
            GameRoomView view,
            int playerId,
            String sessionToken,
            String gameToken
    ) throws WrongPolicy, ServantNotActive, NotLoggedInException {
        this.model        = model;
        this.view         = view;
        this.playerId     = playerId;
        this.sessionToken = sessionToken;
        this.gameToken    = gameToken;
        this.totalRounds  = model.getTotalRounds(sessionToken);

        // 1) Initial UI
        remainingLives = model.getNumberOfLives(sessionToken);
        view.getLifeCountLabel().setText("Lives: " + remainingLives);
        view.getRoundLabel().setText("Waiting for game to start…");
        view.disableAlphabetButtons();
        view.getWordFlow().getChildren().clear();

        // 2) Register CORBA callback
        GameCallbackServiceImpl servant = new GameCallbackServiceImpl(this);
        Object cbRef = PlayerClient_Java
                .getClientModel()
                .registerGameCallback(servant);
        GameCallBackService cbStub = GameCallBackServiceHelper.narrow(cbRef);
        model.registerCallback(cbStub);

        // 3) Immediately request Round 1
        handleGameStart();

        // 4) Wire up Quit
        view.getQuitButton().setOnAction(this::onQuit);

        System.out.println("[DEBUG][GameRoomController] initialized, Round 1 requested.");
    }

    /** Send server “start Round 1.” */
    public void handleGameStart() {
        System.out.println("[DEBUG][GameRoomController] handleGameStart() → request Round 1");
        model.startRound(gameToken, 1, playerId, sessionToken);
    }

    /** Called by GameCallbackServiceImpl.notifyRoundStart(...) */
    public void notifyRoundStartFromCallback(int newRound) {
        if (newRound <= roundNumber) {
            System.out.println("[DEBUG] Ignoring duplicate roundStart(" + newRound + ")");
            return;
        }
        roundNumber = newRound;
        System.out.println("[DEBUG][GameRoomController] notifyRoundStart(round=" + newRound + ")");
        Platform.runLater(() -> showRoundStartScene(newRound));
    }

    /** After the “Round N” popup, build the real UI. */
    public void handleServerRoundStart(int roundNum) {
        System.out.println("[DEBUG][GameRoomController] handleServerRoundStart(round=" + roundNum + ")");
        view.getRoundLabel().setText("Round " + roundNum);
        secretWord     = model.getRandomWord(gameToken, roundNum, playerId, sessionToken);
        remainingLives = model.getNumberOfLives(sessionToken);
        view.getLifeCountLabel().setText("Lives: " + remainingLives);

        setupBlanks();
        setupAlphabet();
        startCountdown(model.getRoundDuration(sessionToken));
    }

    // ── UI Helpers ───────────────────────────────────────────────────────────

    private void setupBlanks() {
        FlowPane wf = view.getWordFlow();
        wf.getChildren().clear();
        blankLabels.clear();
        for (int i = 0; i < secretWord.length(); i++) {
            Pane cell = new Pane();
            cell.setPrefSize(50, 75);
            Label lbl = new Label("_");
            lbl.setStyle("-fx-font-size:36; -fx-font-family:Marykate;");
            lbl.layoutXProperty().bind(cell.widthProperty()
                    .subtract(lbl.widthProperty()).divide(2));
            lbl.layoutYProperty().bind(cell.heightProperty()
                    .subtract(lbl.heightProperty()).divide(2));
            cell.getChildren().add(lbl);
            wf.getChildren().add(cell);
            blankLabels.add(lbl);
        }
    }

    private void setupAlphabet() {
        FlowPane af = view.getAlphabetFlow();
        af.getChildren().clear();
        for (char c = 'A'; c <= 'Z'; c++) {
            Button btn = new Button(String.valueOf(c));
            btn.setStyle("-fx-font-size:24; -fx-font-family:Marykate;");
            btn.setOnAction(e -> handleGuess(btn));
            af.getChildren().add(btn);
        }
    }

    private void startCountdown(int durationSeconds) {
        if (roundTimer != null) roundTimer.stop();
        secondsRemaining = durationSeconds;
        updateTimerLabel();

        roundTimer = new Timeline(new javafx.animation.KeyFrame(
                Duration.seconds(1), evt -> {
            secondsRemaining--;
            updateTimerLabel();
            if (secondsRemaining <= 0) {
                roundTimer.stop();
                onRoundTimeExpired();
            }
        }));
        roundTimer.setCycleCount(durationSeconds);
        roundTimer.play();
    }

    private void updateTimerLabel() {
        int m = secondsRemaining / 60, s = secondsRemaining % 60;
        view.getTimerLabel().setText(String.format("%02d:%02d", m, s));
    }

    // ── Guess Logic ───────────────────────────────────────────────────────────

    private void handleGuess(Button btn) {
        char letter = btn.getText().charAt(0);
        btn.setDisable(true);

        List<Integer> hits = model.guessLetter(gameToken, playerId, sessionToken, letter);
        if (hits.isEmpty()) {
            remainingLives--;
            view.getLifeCountLabel().setText("Lives: " + remainingLives);
            if (remainingLives <= 0) {
                view.disableAlphabetButtons();
                System.out.println("[DEBUG] no lives left → round lost");
            }
        } else {
            hits.forEach(idx -> blankLabels.get(idx).setText(String.valueOf(letter)));
            boolean won = blankLabels.stream()
                    .noneMatch(l -> "_".equals(l.getText()));
            if (won) {
                view.disableAlphabetButtons();
                System.out.println("[DEBUG] all letters revealed → round won");
            }
        }
    }

    private void onRoundTimeExpired() {
        view.disableAlphabetButtons();
        System.out.println("[DEBUG] round timer expired");
    }

    // ── Popups ────────────────────────────────────────────────────────────────

    private void showRoundStartScene(int roundNum) {
        try {
            Stage stage = ViewNavigator.getStage();
            Scene original = stage.getScene();

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/player/GameRoundPopup.fxml")
            );
            Parent popupRoot = loader.load();

            GameRoundPopupView popupCtrl = loader.getController();
            popupCtrl.setGameTitle("What’s The Word?");
            popupCtrl.setRoundNumber(roundNum);

            stage.setScene(new Scene(popupRoot));

            int delay = model.getNextRoundDelay(sessionToken);
            PauseTransition wait = new PauseTransition(Duration.seconds(delay));
            wait.setOnFinished(e -> {
                stage.setScene(original);
                handleServerRoundStart(roundNum);
            });
            wait.play();

        } catch (Exception e) {
            e.printStackTrace();
            handleServerRoundStart(roundNum);
        }
    }

    public void showRoundEndPopup(String winnerName) {
        Platform.runLater(() -> {
            try {
                Stage stage = ViewNavigator.getStage();
                Scene originalScene = stage.getScene();

                String fxmlPath = "/fxml/player/RoundWinnerPopup.fxml";
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent popupRoot = loader.load();

                RoundWinnerPopupView controller = loader.getController();
                controller.setWinnerName(winnerName);

                stage.setScene(new Scene(popupRoot));

                PauseTransition wait = new PauseTransition(Duration.seconds(5));
                wait.setOnFinished(e -> {
                    stage.setScene(originalScene);
                    startNextRound(); // Call your method to start the next round
                });
                wait.play();

            } catch (IOException e) {
                e.printStackTrace();
                startNextRound(); // Ensure the game doesn't freeze on error
            }
        });
    }



    private void startNextRound() {
        if (roundNumber < totalRounds) {
            System.out.println("[DEBUG] Requesting server to start round " + (roundNumber + 1));
            model.startRound(gameToken, roundNumber + 1, playerId, sessionToken);
        } else {
            System.out.println("[DEBUG] All rounds complete – game end not implemented yet.");
        }
    }

    private void onQuit(ActionEvent e) {
        if (roundTimer != null) roundTimer.stop();
        try {
            ViewNavigator.goToLobby();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** Placeholder for when the entire game ends */
    public void showGameEndPopup(String winnerName) {
        // TODO
    }
}
