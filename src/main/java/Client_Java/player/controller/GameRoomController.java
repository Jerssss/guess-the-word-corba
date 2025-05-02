package Client_Java.player.controller;

import Client_Java.player.PlayerClient_Java;
import Client_Java.player.PlayerClient_Model;
import Client_Java.player.model.GameRoomModel;
import Client_Java.player.view.GameRoomView;
import Client_Java.player.view.modals.RoundWinnerPopupView;
import Server_Java.idls.PlayerCallBackIDL.GameCallBackService;
import Server_Java.implementation.GameCallbackServiceImpl;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.omg.CORBA.StringHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameRoomController {
    private final GameRoomModel model;
    private final GameRoomView  view;
    private final int           playerId;
    private final String        sessionToken;
    private final String        gameToken;
    private final int totalRounds;     // ← new


    private int roundNumber = 1;
    private String secretWord;
    private int remainingLives;
    private final List<Label> blankLabels = new ArrayList<>();

    // —— Countdown timer support ——
    private Timeline roundTimer;
    private int      roundSecondsRemaining;

    public GameRoomController(GameRoomModel model,
                              GameRoomView view,
                              int playerId,
                              String sessionToken,
                              String gameToken) {
        this.model        = model;
        this.view         = view;
        this.playerId     = playerId;
        this.sessionToken = sessionToken;
        this.gameToken    = gameToken;
        this.totalRounds = model.getTotalRounds(sessionToken);


        System.out.println("[GameRoomController] Initializing game for player="
                + playerId + " token=" + sessionToken + " game=" + gameToken);

        // Read initial lives from settings
        this.remainingLives = model.getNumberOfLives(sessionToken);
        System.out.println("[GameRoomController] Starting lives = " + remainingLives);
        view.getLifeCountLabel().setText("Lives: " + remainingLives);

        // Register callbacks
        GameCallbackServiceImpl callbackServant =
                new GameCallbackServiceImpl(model, sessionToken, this);
        GameCallBackService callbackStub =
                PlayerClient_Model.registerGameCallback(callbackServant);
        model.registerCallback(playerId, gameToken, sessionToken, callbackStub);

        // Prepare the first round UI (no timer yet)
        startRound();
    }

    private void startRound() {
        System.out.println("[GameRoomController] Preparing round " + roundNumber);

        // Update the round counter in the UI
        view.getRoundLabel().setText(String.valueOf(roundNumber));


        // Pull down mask & lives
        model.startRound(gameToken, roundNumber, playerId, sessionToken);
        secretWord     = model.getRandomWord(gameToken, roundNumber, playerId, sessionToken);
        remainingLives = model.getNumberOfLives(sessionToken);

        // Build UI
        setupBlanks();
        setupAlphabet();
        view.getLifeCountLabel().setText("Lives: " + remainingLives);

        // NOTE: no countdown here—wait for the server’s notifyRoundStart
    }

    /**
     * Called by the server callback when a new round really begins (after your popup).
     */
    public void handleServerRoundStart(int newRoundNumber) {
        this.roundNumber = newRoundNumber;
        System.out.println("[GameRoomController] Server triggered start of round " + newRoundNumber);

        // Mirror startRound() UI changes
        view.getRoundLabel().setText("Round " + roundNumber);
        secretWord     = model.getRandomWord(gameToken, roundNumber, playerId, sessionToken);
        remainingLives = model.getNumberOfLives(sessionToken);
        setupBlanks();
        setupAlphabet();
        view.getLifeCountLabel().setText("Lives: " + remainingLives);

        // —— Now start the countdown timer ——
        int duration = model.getRoundDuration(sessionToken);
        startCountdown(duration);
    }

    /** Shared helper to (re)start the timer countdown */
    private void startCountdown(int durationSeconds) {
        // Stop any old timer
        if (roundTimer != null) {
            roundTimer.stop();
        }
        // Initialize state
        roundSecondsRemaining = durationSeconds;
        updateTimerLabel();

        // Build and play the timeline
        roundTimer = new Timeline(new KeyFrame(Duration.seconds(1), evt -> {
            roundSecondsRemaining--;
            updateTimerLabel();
            if (roundSecondsRemaining <= 0) {
                roundTimer.stop();
                onRoundTimeExpired();
            }
        }));
        roundTimer.setCycleCount(durationSeconds);
        roundTimer.play();
    }

    /** Update the UI label to show MM:SS */
    private void updateTimerLabel() {
        int minutes = roundSecondsRemaining / 60;
        int seconds = roundSecondsRemaining % 60;
        view.getTimerLabel().setText(
                String.format("%02d:%02d", minutes, seconds)
        );
    }

    /** Called when the timer hits zero */
    private void onRoundTimeExpired() {
        System.out.println("[GameRoomController] Round time expired!");
        // Disable all letter buttons
        view.getAlphabetFlow().getChildren().forEach(n -> n.setDisable(true));
        // Treat as a round loss
        onRoundLost();
    }

    private void setupBlanks() {
        FlowPane wf = view.getWordFlow();
        wf.getChildren().clear();
        blankLabels.clear();

        for (int i = 0; i < secretWord.length(); i++) {
            Pane cell = new Pane();
            cell.setPrefSize(50, 75);

            Label lbl = new Label("_");
            lbl.setStyle("-fx-font-size:36; -fx-font-family:Marykate;");
            lbl.layoutXProperty().bind(
                    cell.widthProperty().subtract(lbl.widthProperty()).divide(2)
            );
            lbl.layoutYProperty().bind(
                    cell.heightProperty().subtract(lbl.heightProperty()).divide(2)
            );

            cell.getChildren().add(lbl);
            wf.getChildren().add(cell);
            blankLabels.add(lbl);
        }
        System.out.println("[GameRoomController] Blanks set up for "
                + blankLabels.size() + " letters");
    }

    private void setupAlphabet() {
        FlowPane af = view.getAlphabetFlow();
        af.getChildren().clear();

        for (char c = 'A'; c <= 'Z'; c++) {
            final char letter = c;
            Button btn = new Button(String.valueOf(letter));
            btn.setStyle("-fx-font-size:24; -fx-font-family:Marykate;");
            btn.setOnAction(e -> handleGuess(btn));
            af.getChildren().add(btn);
        }
        System.out.println("[GameRoomController] Alphabet buttons ready");
    }

    private void handleGuess(Button btn) {
        char letter = btn.getText().charAt(0);
        System.out.println("[GameRoomController] handleGuess() for letter: " + letter);
        btn.setDisable(true);

        List<Integer> hits = model.guessLetter(gameToken, playerId, sessionToken, letter);
        System.out.println("[GameRoomController] guessLetter returned positions: " + hits);

        if (hits.isEmpty()) {
            remainingLives--;
            System.out.println("[GameRoomController] Wrong guess. Remaining lives = " + remainingLives);
            Platform.runLater(() -> {
                view.getLifeCountLabel().setText("Lives: " + remainingLives);
                revealNextCatPart();
                if (remainingLives <= 0) {
                    view.getAlphabetFlow().getChildren().forEach(n -> n.setDisable(true));
                    onRoundLost();
                }
            });
        } else {
            hits.forEach(idx -> blankLabels.get(idx).setText(String.valueOf(letter)));
            boolean allRevealed = blankLabels.stream()
                    .allMatch(l -> !"_".equals(l.getText()));
            if (allRevealed) onRoundWon();
        }
    }

    private void revealNextCatPart() {
        if (!view.getCatTopHead().isVisible()) {
            view.getCatTopHead().setVisible(true);
        } else if (!view.getCatEyes().isVisible()) {
            view.getCatEyes().setVisible(true);
        } else if (!view.getCatSnout().isVisible()) {
            view.getCatSnout().setVisible(true);
        } else if (!view.getCatLeftWhiskers().isVisible()) {
            view.getCatLeftWhiskers().setVisible(true);
        } else if (!view.getCatRightWhiskers().isVisible()) {
            view.getCatRightWhiskers().setVisible(true);
        } else {
            view.getCatBottomHead().setVisible(true);
        }
    }

    private void onRoundWon() {
        if (roundNumber >= totalRounds) {
            System.out.println("[GameRoomController] Final round won, awaiting game end.");
            return;
        }

        int next = roundNumber + 1;
        System.out.println("[GameRoomController] Requesting next round: " + next);
        try {
            model.startRound(gameToken, next, playerId, sessionToken);
        } catch (Exception e) {
            System.err.println("[GameRoomController] Failed to start next round: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void onRoundLost() {
        if (roundTimer != null) roundTimer.stop();

        System.out.println("[GameRoomController] Round " + roundNumber + " lost!");
        // TODO: reveal secretWord and show game-over UI
    }

    public void handleGameEnd() {
        if (roundTimer != null) roundTimer.stop();
        PlayerClient_Java.navigateToLobby();
    }

    public void handleCancel() {
        if (roundTimer != null) roundTimer.stop();
        PlayerClient_Java.navigateToLobby();
    }
}
