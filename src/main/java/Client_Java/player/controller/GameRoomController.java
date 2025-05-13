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
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import javafx.animation.PauseTransition;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.omg.CORBA.Object;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import java.io.IOException;
import java.io.InputStream;
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
    private int wrongCount = 0;
    private final List<Label> blankLabels = new ArrayList<>();
    private Timeline roundTimer;
    private int secondsRemaining;
    private final List<ImageView> catFaceParts;
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

        // Register controller with view
        view.setController(this);

        // Cat face components (commented out since not in current FXML)
        catFaceParts = Collections.unmodifiableList(Arrays.asList(
                view.getCatTopHead(),
                view.getCatEyes(),
                view.getCatSnout(),
                view.getCatBottomHead(),
                view.getCatRightWhiskers(),
                view.getCatLeftWhiskers()
        ));

        remainingLives = model.getNumberOfLives(sessionToken);
        view.getLifeCountLabel().setText(String.valueOf(remainingLives));
        view.getRoundLabel().setText("R" + roundNumber);
        view.disableAlphabetButtons();
        view.getBlanksFlowPane().getChildren().clear();

        GameCallbackServiceImpl servantImpl = new GameCallbackServiceImpl(this);
        GameCallBackServicePOA poaServant = servantImpl;
        Object cbRef = PlayerClient_Java.getClientModel().registerGameCallback(poaServant);
        GameCallBackService cbStub = GameCallBackServiceHelper.narrow(cbRef);
        model.registerCallback(cbStub);

        view.getQuitButton().setOnAction(this::handleQuitButton);

        handleGameStart();
    }

    public void handleGameStart() {
        System.out.println("[DEBUG] handleGameStart() → request Round 1");
        model.startRound(gameToken, 1, playerId, sessionToken);
    }

    public void notifyRoundStartFromCallback(int newRound) {
        if (endPopupShowing) {
            PauseTransition retry = new PauseTransition(Duration.seconds(1));
            retry.setOnFinished(e -> notifyRoundStartFromCallback(newRound));
            retry.play();
            return;
        }
        if (newRound <= roundNumber) return;

        roundNumber = newRound;
        Platform.runLater(() -> showRoundStartScene(newRound));
    }

    public void handleServerRoundStart(int roundNum) {
        System.out.println("[DEBUG] handleServerRoundStart for round " + roundNum);
        view.getRoundLabel().setText("R" + roundNum);
        secretWord = model.getRandomWord(gameToken, roundNum, playerId, sessionToken);
        remainingLives = model.getNumberOfLives(sessionToken);
        wrongCount = 0;
        view.getLifeCountLabel().setText(String.valueOf(remainingLives));

        view.resetAlphabetButtons();
        view.enableAlphabetButtons(); // Enable buttons for the new round
        setupBlanks();
        startCountdown(model.getRoundDuration(sessionToken));
    }

    private void setupBlanks() {
        FlowPane bf = view.getBlanksFlowPane();
        bf.getChildren().clear();
        blankLabels.clear();
        for (int i = 0; i < secretWord.length(); i++) {
            Pane cell = new Pane();
            cell.setPrefSize(58, 68);
            ImageView blankImg = new ImageView();
            blankImg.setFitWidth(58);
            blankImg.setFitHeight(68);
            blankImg.setTranslateY(50);

            Label lbl = new Label("_");
            lbl.setStyle("-fx-font-size:88; -fx-font-family:'Quick Pencil Regular'; -fx-text-fill:#61ff82;");
            lbl.setPrefSize(58, 68);
            lbl.setAlignment(javafx.geometry.Pos.CENTER);
            cell.getChildren().addAll(blankImg, lbl);
            bf.getChildren().add(cell);
            blankLabels.add(lbl);
        }
    }

    private void loadImage(ImageView imageView, String resourcePath) {
        try {
            System.out.println("[DEBUG] Attempting to load resource: " + resourcePath);
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is != null) {
                imageView.setImage(new Image(is));
                System.out.println("[DEBUG] Successfully loaded resource: " + resourcePath);
            } else {
                System.err.println("[ERROR] Image resource not found: " + resourcePath);
                System.err.println("[DEBUG] Class loader: " + getClass().getClassLoader());
                System.err.println("[DEBUG] Resource URL: " + getClass().getResource(resourcePath));
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to load image: " + resourcePath);
            e.printStackTrace();
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

    public void handleGuess(char letter) {
        System.out.println("[DEBUG] handleGuess called for letter: " + letter);
        try {
            view.disableLetterButton(letter);
            List<Integer> hits = model.guessLetter(gameToken, playerId, sessionToken, letter);
            System.out.println("[DEBUG] Guess result for letter " + letter + ": hits=" + hits);

            if (hits.isEmpty()) {
                remainingLives--;
                wrongCount++;
                view.getLifeCountLabel().setText(String.valueOf(remainingLives));
                view.showWrongLetter(letter);
                // Comment out cat face parts since not in FXML
                // if (wrongCount <= catFaceParts.size()) {
                //     catFaceParts.get(wrongCount - 1).setVisible(true);
                // }
                if (remainingLives <= 0) {
                    view.disableAlphabetButtons();
                }
            } else {
                hits.forEach(idx -> blankLabels.get(idx).setText(String.valueOf(letter)));
                view.showCorrectLetter(letter);
                boolean won = blankLabels.stream().noneMatch(l -> "_".equals(l.getText()));
                if (won) {
                    view.disableAlphabetButtons();
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Error processing guess for letter " + letter + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void onRoundTimeExpired() {
        view.disableAlphabetButtons();
    }

    private void showRoundStartScene(int roundNum) {
        try {
            Stage mainStage = ViewNavigator.getStage();
            if (mainStage == null) {
                handleServerRoundStart(roundNum);
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/player/GameRoundPopup.fxml"));
            Parent popupRoot = loader.load();
            GameRoundPopupView ctrl = loader.getController();
            ctrl.setGameTitle("What's The Word?");
            ctrl.setRoundNumber(roundNum);

            // Create styled container
            StackPane container = new StackPane(popupRoot);
            container.setBackground(new Background(new BackgroundFill(
                    Color.web("#F5F5DC"),
                    new CornerRadii(12),
                    Insets.EMPTY)));
            container.setBorder(new Border(new BorderStroke(
                    Color.web("#8B4513", 0.3),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(12),
                    new BorderWidths(0.75))));
            container.setEffect(new DropShadow(8, Color.rgb(0, 0, 0, 0.15)));
            StackPane.setMargin(popupRoot, new Insets(12));

            // Create undecorated popup stage
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(mainStage);
            popupStage.initStyle(StageStyle.TRANSPARENT);

            Scene popupScene = new Scene(container);
            popupScene.setFill(Color.TRANSPARENT);
            popupStage.setScene(popupScene);

            // Calculate center position relative to main window
            container.layout(); // Force layout pass to get proper dimensions
            popupStage.sizeToScene(); // Size stage to content

            // Get main window position and size
            double mainX = mainStage.getX();
            double mainY = mainStage.getY();
            double mainWidth = mainStage.getWidth();
            double mainHeight = mainStage.getHeight();

            // Calculate center position
            double popupWidth = container.getBoundsInLocal().getWidth();
            double popupHeight = container.getBoundsInLocal().getHeight();

            popupStage.setX(mainX + (mainWidth - popupWidth) / 2);
            popupStage.setY(mainY + (mainHeight - popupHeight) / 2);

            popupStage.show();

            PauseTransition wait = new PauseTransition(Duration.seconds(model.getNextRoundDelay(sessionToken)));
            wait.setOnFinished(e -> {
                popupStage.close();
                handleServerRoundStart(roundNum);
            });
            wait.play();
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to show round start popup: " + e.getMessage());
            handleServerRoundStart(roundNum);
        }
    }

    public void showRoundEndPopup(String winnerName, String secretWord) {
        if (endPopupShowing) return;
        endPopupShowing = true;

        Platform.runLater(() -> {
            try {
                Stage mainStage = ViewNavigator.getStage();
                if (mainStage == null) {
                    endPopupShowing = false;
                    return;
                }

                // 1. Create overlay
                StackPane mainRoot = (StackPane) mainStage.getScene().getRoot();
                Rectangle overlay = new Rectangle();
                overlay.setFill(Color.rgb(0, 0, 0, 0.5));
                overlay.widthProperty().bind(mainRoot.widthProperty());
                overlay.heightProperty().bind(mainRoot.heightProperty());
                mainRoot.getChildren().add(overlay);

                // 2. Load FXML
                String fxmlPath = (winnerName != null && !winnerName.trim().isEmpty())
                        ? "/fxml/player/RoundWinnerPopup.fxml"
                        : "/fxml/player/NoWinnerPopup.fxml";
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent popupRoot = loader.load();

                // 3. Create container with guaranteed border solution
                StackPane container = new StackPane();

                if (winnerName != null && !winnerName.trim().isEmpty()) {
                    // BULLETPROOF SOLUTION FOR ROUND WINNER POPUP

                    // 1. Create the border decoration
                    StackPane borderPane = new StackPane();
                    borderPane.setBackground(new Background(new BackgroundFill(
                            Color.web("#F5F5DC"),
                            new CornerRadii(12),
                            Insets.EMPTY)));
                    borderPane.setBorder(new Border(new BorderStroke(
                            Color.web("#8B4513", 0.3),
                            BorderStrokeStyle.SOLID,
                            new CornerRadii(12),
                            new BorderWidths(2.0)))); // Increased to 2.0 for consistency
                    borderPane.setEffect(new DropShadow(8, Color.rgb(0, 0, 0, 0.15)));
                    borderPane.setMinSize(816, 554); // Enforce minimum size
                    borderPane.setPrefSize(816, 554); // Explicit preferred size
                    borderPane.setMaxSize(816, 554); // Enforce maximum size
                    borderPane.setStyle("-fx-snap-to-pixel: true;"); // Ensure crisp rendering

                    // 2. Create a clipping mask for rounded corners
                    Rectangle clip = new Rectangle(800, 530);
                    clip.setArcWidth(12); // Match border CornerRadii
                    clip.setArcHeight(12); // Match border CornerRadii

                    // 3. Prepare the content
                    StackPane contentPane = new StackPane(popupRoot);
                    contentPane.setClip(clip);
                    contentPane.setPrefSize(800, 530); // Match clip size exactly

                    // 4. Build the container
                    container.getChildren().addAll(borderPane, contentPane);
                    StackPane.setAlignment(contentPane, Pos.CENTER); // Ensure content is centered
                    StackPane.setMargin(contentPane, new Insets(8, 8, 8, 8)); // Increased to 8px for clearance

                    // 5. Adjust the ImageView
                    ImageView imageView = (ImageView) popupRoot.lookup("#backgroundImageView");
                    if (imageView != null) {
                        imageView.setFitWidth(800);
                        imageView.setFitHeight(530);
                        imageView.setPreserveRatio(false); // Ensure exact size
                        imageView.setClip(new Rectangle(800, 530)); // Additional clip
                        StackPane.setMargin(imageView, new Insets(0)); // Reset FXML margins
                    }

                    // 6. Debug bounds and layout
                    container.applyCss();
                    container.layout();
                    System.out.println("borderPane bounds: " + borderPane.getBoundsInParent());
                    System.out.println("contentPane bounds: " + contentPane.getBoundsInParent());
                    System.out.println("imageView bounds: " + imageView.getBoundsInParent());
                }else {
                    // Normal handling for NoWinnerPopup
                    container.getChildren().add(popupRoot);
                    container.setBackground(new Background(new BackgroundFill(
                            Color.web("#F5F5DC"),
                            new CornerRadii(12),
                            Insets.EMPTY)));
                    container.setBorder(new Border(new BorderStroke(
                            Color.web("#8B4513", 0.3),
                            BorderStrokeStyle.SOLID,
                            new CornerRadii(12),
                            new BorderWidths(0.75))));
                    container.setEffect(new DropShadow(8, Color.rgb(0, 0, 0, 0.15)));
                    StackPane.setMargin(popupRoot, new Insets(12));
                }

                // 4. Set up controller
                if (winnerName != null && !winnerName.trim().isEmpty()) {
                    RoundWinnerPopupView controller = loader.getController();
                    controller.setWinnerName(winnerName);
                    controller.setWinningWord(secretWord);
                } else {
                    NoWinnerPopupView controller = loader.getController();
                    controller.setSecretWord(secretWord);
                }

                // 5. Create and show stage
                Stage popupStage = new Stage();
                popupStage.initOwner(mainStage);
                popupStage.initStyle(StageStyle.UNDECORATED);

                Scene popupScene = new Scene(container);
                popupScene.setFill(Color.TRANSPARENT);
                popupStage.setScene(popupScene);

                // 6. Perfect centering
                popupStage.addEventHandler(WindowEvent.WINDOW_SHOWN, (event) -> {
                    container.applyCss();
                    container.layout();
                    double centerX = mainStage.getX() + (mainStage.getWidth() - container.getWidth())/2;
                    double centerY = mainStage.getY() + (mainStage.getHeight() - container.getHeight())/2;
                    popupStage.setX(centerX);
                    popupStage.setY(centerY);
                });

                popupStage.show();

                // 7. Auto-close
                PauseTransition pause = new PauseTransition(Duration.seconds(5));
                pause.setOnFinished(e -> {
                    popupStage.close();
                    mainRoot.getChildren().remove(overlay);
                    endPopupShowing = false;
                });
                pause.play();

            } catch (Exception e) {
                endPopupShowing = false;
                e.printStackTrace();
            }
        });
    }

    private void handleQuitButton(ActionEvent e) {
        if (roundTimer != null) roundTimer.stop();
        try {
            ViewNavigator.goToLobby();
        } catch (Exception ex) {
            System.err.println("[ERROR] Failed to return to lobby: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void showGameEndPopup(String champion) {
        Platform.runLater(() -> {
            try {
                Stage stage = ViewNavigator.getStage();
                if (stage == null) {
                    return;
                }
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/player/GameWinnerPopup.fxml"));
                Parent popupRoot = loader.load();

                GameWinnerPopupView c = loader.getController();
                c.setGameTitle("Game Over!");
                c.setWinningUsername(champion != null ? champion : "Nobody");

                stage.setScene(new Scene(popupRoot));
                stage.centerOnScreen();

                PauseTransition wait = new PauseTransition(Duration.seconds(5));
                wait.setOnFinished(evt -> {
                    try {
                        ViewNavigator.goToLobby();
                    } catch (Exception ex) {
                        System.err.println("[ERROR] Failed to return to lobby: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                });
                wait.play();
            } catch (IOException e) {
                System.err.println("[ERROR] Failed to show game end popup: " + e.getMessage());
                try {
                    ViewNavigator.goToLobby();
                } catch (Exception ex) {
                    System.err.println("[ERROR] Failed to return to lobby: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
    }
}