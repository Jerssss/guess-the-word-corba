package Client_Java.player.controller;

import Client_Java.player.PlayerClient_Java;
import Client_Java.player.PlayerClient_Model;
import Client_Java.player.model.GameRoomModel;
import Client_Java.player.view.GameRoomView;
import Client_Java.player.view.modals.RoundWinnerPopupView;
import Server_Java.idls.PlayerCallBackIDL.GameCallBackService;
import Server_Java.implementation.GameCallbackServiceImpl;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameRoomController {
    private final GameRoomModel model;
    private final GameRoomView  view;
    private final int           playerId;
    private final String        sessionToken;
    private final String        gameToken;

    private int    roundNumber = 1;
    private String secretWord;
    private int    remainingLives;
    private final List<Label> blankLabels = new ArrayList<>();

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

        System.out.println("[GameRoomController] Initializing game for player="
                + playerId + " token=" + sessionToken + " game=" + gameToken);

        // Read initial lives from settings
        this.remainingLives = model.getNumberOfLives(sessionToken);
        System.out.println("[GameRoomController] Starting lives = " + remainingLives);
        view.getLifeCountLabel().setText("Lives: " + remainingLives);

        GameCallbackServiceImpl callbackServant =
                new GameCallbackServiceImpl(model, sessionToken, this);
        GameCallBackService callbackStub =
                PlayerClient_Model.registerGameCallback(callbackServant);
        model.registerCallback(playerId, gameToken, sessionToken, callbackStub);

        startRound();
    }

    private void startRound() {
        System.out.println("[GameRoomController] Starting round " + roundNumber);

        // — NEW: Update the round counter in the UI —
        view.getRoundLabel().setText("Round " + roundNumber);

        // Then pull down mask & lives and build UI as before:
        model.startRound(gameToken, roundNumber, playerId, sessionToken);
        secretWord = model.getRandomWord(gameToken, roundNumber, playerId, sessionToken);
        remainingLives = model.getNumberOfLives(sessionToken);

        setupBlanks();
        setupAlphabet();

        view.getLifeCountLabel().setText("Lives: " + remainingLives);
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
            final char letter = c;  // effectively final for the lambda
            Button btn = new Button(String.valueOf(letter));
            btn.setStyle("-fx-font-size:24; -fx-font-family:Marykate;");

            btn.setOnAction(e -> {
                System.out.println("[GameRoomController] Letter button clicked: " + letter);
                handleGuess(btn);
            });

            af.getChildren().add(btn);
        }
        System.out.println("[GameRoomController] Alphabet buttons ready");
    }// In Client_Java.player.controller.GameRoomController

    /**
     * Called by the server callback when a new round really begins.
     */
    public void handleServerRoundStart(int roundNumber) {
        // 1) Update our local round counter
        this.roundNumber = roundNumber;

        // — NEW: Update the round counter in the UI —
        view.getRoundLabel().setText("Round " + roundNumber);

        // 2) Fetch new mask & lives
        this.secretWord     = model.getRandomWord(gameToken, roundNumber, playerId, sessionToken);
        this.remainingLives = model.getNumberOfLives(sessionToken);

        // 3) Rebuild the blanks and alphabet
        setupBlanks();
        setupAlphabet();

        // 4) Sync the lives label (in case it changed)
        view.getLifeCountLabel().setText("Lives: " + remainingLives);
    }



    private void handleGuess(Button btn) {
        char letter = btn.getText().charAt(0);
        System.out.println("[GameRoomController] handleGuess() for letter: " + letter);
        btn.setDisable(true);

        List<Integer> hits = model.guessLetter(gameToken, playerId, sessionToken, letter);
        System.out.println("[GameRoomController] guessLetter returned positions: " + hits);

        if (hits.isEmpty()) {
            // Wrong guess: decrement lives, update UI, and possibly end the round
            remainingLives--;
            System.out.println("[GameRoomController] Wrong guess. Remaining lives = " + remainingLives);

            Platform.runLater(() -> {
                view.getLifeCountLabel().setText("Lives: " + remainingLives);
                revealNextCatPart();

                if (remainingLives <= 0) {
                    System.out.println("[GameRoomController] No lives left -> disabling alphabet");
                    view.getAlphabetFlow().getChildren()
                            .forEach(node -> node.setDisable(true));
                    onRoundLost();
                }
            });

        } else {
            // Correct guess: reveal letters with detailed debug
            System.out.println("[GameRoomController] Correct! Revealing letter '"
                    + letter + "' at positions: " + hits);

            for (int idx : hits) {
                Label lbl = blankLabels.get(idx);
                System.out.println("    -> Before: blankLabels[" + idx + "] = '" + lbl.getText() + "'");
                lbl.setText(String.valueOf(letter));
                System.out.println("    -> After : blankLabels[" + idx + "] = '" + lbl.getText() + "'");
            }

            // Dump full current mask
            StringBuilder state = new StringBuilder();
            blankLabels.forEach(l -> state.append(l.getText()));
            System.out.println("[GameRoomController] Current word state: " + state);

            // Check for win
            boolean allRevealed = blankLabels.stream()
                    .allMatch(l -> !"*".equals(l.getText()) && !"_".equals(l.getText()));
            if (allRevealed) {
                System.out.println("[GameRoomController] All letters revealed -> onRoundWon()");
                onRoundWon();
            }
        }
    }

    private void revealNextCatPart() {
        System.out.print("[GameRoomController] revealNextCatPart() -> ");
        if (!view.getCatTopHead().isVisible()) {
            view.getCatTopHead().setVisible(true);
            System.out.println("catTopHead");
        } else if (!view.getCatEyes().isVisible()) {
            view.getCatEyes().setVisible(true);
            System.out.println("catEyes");
        } else if (!view.getCatSnout().isVisible()) {
            view.getCatSnout().setVisible(true);
            System.out.println("catSnout");
        } else if (!view.getCatLeftWhiskers().isVisible()) {
            view.getCatLeftWhiskers().setVisible(true);
            System.out.println("catLeftWhiskers");
        } else if (!view.getCatRightWhiskers().isVisible()) {
            view.getCatRightWhiskers().setVisible(true);
            System.out.println("catRightWhiskers");
        } else {
            view.getCatBottomHead().setVisible(true);
            System.out.println("catBottomHead");
        }
    }

    // In Client_Java.player.controller.GameRoomController

    private void onRoundWon() {
        System.out.println("[GameRoomController] Round " + roundNumber + " won!");

        // 1) Fetch winner & my name
        String winnerName = model.getRoundWinner(gameToken, playerId, sessionToken);
        String myName     = model.getPlayerDisplayName(playerId, sessionToken);
        String displayMessage = winnerName.equals(myName)
                ? "You won!"
                : winnerName + " won!";

        // 2) Show popup on JavaFX thread
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/player/RoundWinnerPopup.fxml")
                );
                Parent popupRoot = loader.load();
                RoundWinnerPopupView popupController = loader.getController();
                popupController.setWinnerName(displayMessage);

                Stage popupStage = new Stage(StageStyle.TRANSPARENT);
                popupStage.initModality(Modality.NONE);
                popupStage.initOwner(PlayerClient_Java.getStage());
                popupStage.setScene(new Scene(popupRoot));

                // Center
                Stage main = PlayerClient_Java.getStage();
                popupStage.setX(main.getX() + (main.getWidth()  - popupRoot.prefWidth(-1)) / 2);
                popupStage.setY(main.getY() + (main.getHeight() - popupRoot.prefHeight(-1)) / 2);

                popupStage.show();

                // Auto-close after delay
                int delay = model.getNextRoundDelay(sessionToken);
                PauseTransition wait = new PauseTransition(Duration.seconds(delay));
                wait.setOnFinished(evt -> popupStage.close());
                wait.play();

            } catch (IOException io) {
                System.err.println("[GameRoomController] Error showing round-winner popup: " + io);
                io.printStackTrace();
            }
        });

        // 7) Tell the SERVER to start the next round (roundNumber + 1).
        int nextRound = roundNumber + 1;
        try {
            model.startRound(gameToken, nextRound, playerId, sessionToken);
        } catch (Exception e) {
            System.err.println("[GameRoomController] failed to start next round: " + e);
        }
    }
// In GameRoomController.java

    public void handleGameEnd() {
        System.out.println("[GameRoomController] handleGameEnd() — navigating back to lobby");
        // Simply delegate all the FXML loading & MVC wiring to navigateToLobby()
        PlayerClient_Java.navigateToLobby();
    }



    private void onRoundLost() {
        System.out.println("[GameRoomController] Round " + roundNumber + " lost!");
        // TODO: reveal word and show game-over screen
    }

    /** Optional back-to-lobby handler if wired in FXML **/
    public void handleCancel() {
        System.out.println("[GameRoomController] Cancel pressed, navigating back to lobby");
        PlayerClient_Java.navigateToLobby();
    }

}
