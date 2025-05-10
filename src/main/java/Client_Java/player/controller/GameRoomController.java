package Client_Java.player.controller;

import Client_Java.player.PlayerClient_Java;
import Client_Java.player.model.GameRoomModel;
import Client_Java.player.view.GameRoomView;
import Client_Java.player.view.ViewNavigator;
import Client_Java.player.view.modals.GameRoundPopupView;
import Client_Java.player.view.modals.GameWinnerPopupView;
import Client_Java.player.view.modals.NoWinnerPopupView;
import Client_Java.player.view.modals.RoundWinnerPopupView;
import Client_Java.player.implementation.GameCallbackServiceImpl;
import GameIDL.NotLoggedInException;
import PlayerCallBackIDL.GameCallBackService;
import PlayerCallBackIDL.GameCallBackServiceHelper;
import PlayerCallBackIDL.GameCallBackServicePOA;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.omg.CORBA.Object;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameRoomController {
    private final GameRoomModel model;
    private final GameRoomView view;
    private final int playerId;
    private final String sessionToken;
    private final String gameToken;
    private final int totalRounds;

    private int roundNumber = 0;
    private String secretWord = "";
    private int remainingLives;
    private int wrongCount = 0; // Track wrong guesses
    private final List<Label> blankLabels = new ArrayList<>();
    private Timeline roundTimer;
    private int secondsRemaining;
    private final List<ImageView> catFaceParts; // List of cat face components

    // Prevent overlapping RoundStart popups
    private boolean endPopupShowing = false;
    private final Map<String, Integer> winCounts = new HashMap<>();

    public GameRoomController(
            GameRoomModel model,
            GameRoomView view,
            int playerId,
            String sessionToken,
            String gameToken
    ) throws WrongPolicy, ServantNotActive, NotLoggedInException {
        this.model = model;
        this.view = view;
        this.playerId = playerId;
        this.sessionToken = sessionToken;
        this.gameToken = gameToken;
        this.totalRounds = model.getTotalRounds(sessionToken);

        // Initialize cat face parts list in order of reveal (Java 8 compatible)
        catFaceParts = Collections.unmodifiableList(Arrays.asList(
                view.getCatTopHead(),
                view.getCatEyes(),
                view.getCatSnout(),
                view.getCatBottomHead(),
                view.getCatRightWhiskers(),
                view.getCatLeftWhiskers()
        ));

        // Initial UI
        remainingLives = model.getNumberOfLives(sessionToken);
        view.getLifeCountLabel().setText("Lives: " + remainingLives);
        view.getRoundLabel().setText("Waiting for game to start…");
        view.disableAlphabetButtons();
        view.getWordFlow().getChildren().clear();
        resetCatFace(); // Ensure cat face parts are hidden initially

        // Register CORBA callback
        GameCallbackServiceImpl servantImpl = new GameCallbackServiceImpl(this);
        GameCallBackServicePOA poaServant = servantImpl;
        Object cbRef = PlayerClient_Java
                .getClientModel()
                .registerGameCallback(poaServant);
        GameCallBackService cbStub =
                GameCallBackServiceHelper.narrow(cbRef);
        model.registerCallback(cbStub);

        // Kick off Round 1
        handleGameStart();

        // Quit button
        view.getQuitButton().setOnAction(this::onQuit);

        System.out.println("[DEBUG][GameRoomController] initialized, Round 1 requested.");
    }

    /** Ask server to start Round 1 */
    public void handleGameStart() {
        System.out.println("[DEBUG] handleGameStart() → request Round 1");
        model.startRound(gameToken, 1, playerId, sessionToken);
    }

    /**
     * Invoked by GameCallbackServiceImpl.notifyRoundStart(...).
     * Defers if the round‑end popup is still up.
     */
    public void notifyRoundStartFromCallback(int newRound) {
        System.out.printf("[CONTROLLER] received notifyRoundStart(%d), endPopup=%b%n",
                newRound, endPopupShowing);

        if (endPopupShowing) {
            PauseTransition retry = new PauseTransition(Duration.seconds(1));
            retry.setOnFinished(e -> notifyRoundStartFromCallback(newRound));
            retry.play();
            return;
        }
        if (newRound <= roundNumber) return;  // ignore duplicates

        roundNumber = newRound;
        Platform.runLater(() -> showRoundStartScene(newRound));
    }

    /** After the “Round N” popup, build the round UI */
    public void handleServerRoundStart(int roundNum) {
        System.out.println("[DEBUG] handleServerRoundStart(" + roundNum + ")");
        view.getRoundLabel().setText("Round " + roundNum);
        secretWord = model.getRandomWord(gameToken, roundNum, playerId, sessionToken);
        remainingLives = model.getNumberOfLives(sessionToken);
        wrongCount = 0; // Reset wrong count for new round
        view.getLifeCountLabel().setText("Lives: " + remainingLives);
        resetCatFace(); // Hide all cat face parts

        setupBlanks();
        setupAlphabet();
        startCountdown(model.getRoundDuration(sessionToken));
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

    private void resetCatFace() {
        catFaceParts.forEach(part -> part.setVisible(false));
        System.out.println("[DEBUG] Cat face parts reset to hidden");
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

    private void handleGuess(Button btn) {
        char letter = btn.getText().charAt(0);
        btn.setDisable(true);

        List<Integer> hits = model.guessLetter(
                gameToken, playerId, sessionToken, letter
        );

        if (hits.isEmpty()) {
            remainingLives--;
            wrongCount++;
            view.getLifeCountLabel().setText("Lives: " + remainingLives);
            // Reveal cat face part based on wrongCount
            if (wrongCount <= catFaceParts.size()) {
                catFaceParts.get(wrongCount - 1).setVisible(true);
                System.out.println("[DEBUG] Revealed cat face part " + wrongCount + " for wrong guess");
            }
            if (remainingLives <= 0) {
                view.disableAlphabetButtons();
                System.out.println("[DEBUG] round lost (waiting server callback)");
            }
        } else {
            hits.forEach(idx -> blankLabels.get(idx).setText(String.valueOf(letter)));
            boolean won = blankLabels.stream()
                    .noneMatch(l -> "_".equals(l.getText()));
            if (won) {
                view.disableAlphabetButtons();
                System.out.println("[DEBUG] round won (waiting server callback)");
            }
        }
    }

    /** Just disable inputs on timeout—wait for server’s RoundEnd callback */
    private void onRoundTimeExpired() {
        view.disableAlphabetButtons();
        System.out.println("[DEBUG] timer expired (waiting server callback)");
    }

    /** Show “Round N” popup then launch the round UI */
    private void showRoundStartScene(int roundNum) {
        try {
            Stage stage = ViewNavigator.getStage();
            if (stage == null) {
                System.err.println("[ERROR] Primary stage is null in showRoundStartScene");
                handleServerRoundStart(roundNum); // Fallback to round UI without popup
                return;
            }
            Scene original = stage.getScene();

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/player/GameRoundPopup.fxml")
            );
            Parent popupRoot;
            try {
                popupRoot = loader.load();
            } catch (IOException e) {
                System.err.println("[ERROR] Failed to load GameRoundPopup.fxmnl: " + e.getMessage());
                e.printStackTrace();
                handleServerRoundStart(roundNum); // Fallback to round UI
                return;
            }

            GameRoundPopupView ctrl = loader.getController();
            if (ctrl == null) {
                System.err.println("[ERROR] GameRoundPopupView controller is null");
                handleServerRoundStart(roundNum); // Fallback to round UI
                return;
            }
            ctrl.setGameTitle("What’s The Word?");
            ctrl.setRoundNumber(roundNum);

            stage.setScene(new Scene(popupRoot));
            stage.centerOnScreen(); // Center the popup
            System.out.println("[DEBUG] Showing Round " + roundNum + " popup");

            PauseTransition wait = new PauseTransition(
                    Duration.seconds(model.getNextRoundDelay(sessionToken))
            );
            wait.setOnFinished(e -> {
                stage.setScene(original);
                handleServerRoundStart(roundNum);
                System.out.println("[DEBUG] Restored original scene for Round " + roundNum);
            });
            wait.play();

        } catch (Exception e) {
            System.err.println("[ERROR] Unexpected error in showRoundStartScene: " + e.getMessage());
            e.printStackTrace();
            handleServerRoundStart(roundNum); // Fallback to round UI
        }
    }

    /**
     * Show end‑of‑round popup (winner or no‑winner).
     * If not the last round, restores the game UI after 5 s.
     * If it is the final round, leaves the popup up and waits for GameEnd.
     */
    public void showRoundEndPopup(String winnerName, String secretWord) {
        if (endPopupShowing) return;

        System.out.println("[DEBUG] showRoundEndPopup(winner=" + winnerName + ", word=" + secretWord + ")");
        if (winnerName != null && !winnerName.trim().isEmpty()) {
            winCounts.merge(winnerName, 1, Integer::sum);
        }
        endPopupShowing = true;

        Platform.runLater(() -> {
            try {
                Stage stage = ViewNavigator.getStage();
                if (stage == null) {
                    System.err.println("[ERROR] Primary stage is null in showRoundEndPopup");
                    endPopupShowing = false;
                    return;
                }
                Scene original = stage.getScene();

                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        (winnerName != null && !winnerName.trim().isEmpty())
                                ? "/fxml/player/RoundWinnerPopup.fxml"
                                : "/fxml/player/NoWinnerPopup.fxml"
                ));
                Parent popupRoot = loader.load();

                if (winnerName != null && !winnerName.trim().isEmpty()) {
                    RoundWinnerPopupView c = loader.getController();
                    c.setWinnerName(winnerName);
                    c.setWinningWord(secretWord);
                } else {
                    NoWinnerPopupView c = loader.getController();
                    c.setSecretWord(secretWord);
                }

                stage.setScene(new Scene(popupRoot));
                stage.centerOnScreen(); // Center the popup

                PauseTransition wait = new PauseTransition(Duration.seconds(5));
                wait.setOnFinished(e -> {
                    // Only restore for non‑final rounds
                    if (roundNumber < totalRounds) {
                        stage.setScene(original);
                        System.out.println("[DEBUG] client ready for next round");
                    }
                    // In either case, clear the guard so callback handling resumes
                    endPopupShowing = false;
                });
                wait.play();

            } catch (IOException e) {
                System.err.println("[ERROR] Failed to load round end popup FXML: " + e.getMessage());
                e.printStackTrace();
                endPopupShowing = false;
            }
        });
    }

    private void onQuit(ActionEvent e) {
        if (roundTimer != null) roundTimer.stop();
        try {
            ViewNavigator.goToLobby();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** Invoked by GameCallbackServiceImpl.notifyGameEnd(...) */
    public void showGameEndPopup(String champion) {
        Platform.runLater(() -> {
            try {
                Stage stage = ViewNavigator.getStage();
                if (stage == null) {
                    System.err.println("[ERROR] Primary stage is null in showGameEndPopup");
                    return;
                }
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/player/GameWinnerPopup.fxml")
                );
                Parent popupRoot = loader.load();

                GameWinnerPopupView c = loader.getController();
                c.setGameTitle("Game Over!");
                c.setWinningUsername(champion != null ? champion : "Nobody");

                stage.setScene(new Scene(popupRoot));
                stage.centerOnScreen(); // Center the popup

                PauseTransition wait = new PauseTransition(Duration.seconds(5));
                wait.setOnFinished(evt -> {
                    try {
                        ViewNavigator.goToLobby();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
                wait.play();

            } catch (IOException e) {
                System.err.println("[ERROR] Failed to load GameWinnerPopup.fxml: " + e.getMessage());
                e.printStackTrace();
                try {
                    ViewNavigator.goToLobby();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}