package Client_Java.player.view;

import Client_Java.player.controller.GameRoomController;
import Client_Java.player.view.modals.GameRoundPopupView;
import Client_Java.player.view.modals.GameWinnerPopupView;
import Client_Java.player.view.modals.NoWinnerPopupView;
import Client_Java.player.view.modals.RoundWinnerPopupView;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameRoomView {
    @FXML
    private ImageView gameRoomBackgroundImage;
    @FXML
    private FlowPane blanksFlowPane;
    @FXML
    private Label roundLabel;
    @FXML
    private Label lifeCountLabel;
    @FXML
    private Label playerCountLabel; // Players count
    @FXML
    private Label timerLabel;
    @FXML
    private Text timeLabel;
    @FXML
    private Text playersLabel;

    @FXML
    private Button quitButton;

    @FXML
    private ImageView heartsImageView;

    // Correct guess ImageViews
    @FXML
    private ImageView correctGuessA;
    @FXML
    private ImageView correctGuessB;
    @FXML
    private ImageView correctGuessC;
    @FXML
    private ImageView correctGuessD;
    @FXML
    private ImageView correctGuessE;
    @FXML
    private ImageView correctGuessF;
    @FXML
    private ImageView correctGuessG;
    @FXML
    private ImageView correctGuessH;
    @FXML
    private ImageView correctGuessI;
    @FXML
    private ImageView correctGuessJ;
    @FXML
    private ImageView correctGuessK;
    @FXML
    private ImageView correctGuessL;
    @FXML
    private ImageView correctGuessM;
    @FXML
    private ImageView correctGuessN;
    @FXML
    private ImageView correctGuessO;
    @FXML
    private ImageView correctGuessP;
    @FXML
    private ImageView correctGuessQ;
    @FXML
    private ImageView correctGuessR;
    @FXML
    private ImageView correctGuessS;
    @FXML
    private ImageView correctGuessT;
    @FXML
    private ImageView correctGuessU;
    @FXML
    private ImageView correctGuessV;
    @FXML
    private ImageView correctGuessW;
    @FXML
    private ImageView correctGuessX;
    @FXML
    private ImageView correctGuessY;
    @FXML
    private ImageView correctGuessZ;

    // Wrong guess ImageViews
    @FXML
    private ImageView wrongGuessA;
    @FXML
    private ImageView wrongGuessB;
    @FXML
    private ImageView wrongGuessC;
    @FXML
    private ImageView wrongGuessD;
    @FXML
    private ImageView wrongGuessE;
    @FXML
    private ImageView wrongGuessF;
    @FXML
    private ImageView wrongGuessG;
    @FXML
    private ImageView wrongGuessH;
    @FXML
    private ImageView wrongGuessI;
    @FXML
    private ImageView wrongGuessJ;
    @FXML
    private ImageView wrongGuessK;
    @FXML
    private ImageView wrongGuessL;
    @FXML
    private ImageView wrongGuessM;
    @FXML
    private ImageView wrongGuessN;
    @FXML
    private ImageView wrongGuessO;
    @FXML
    private ImageView wrongGuessP;
    @FXML
    private ImageView wrongGuessQ;
    @FXML
    private ImageView wrongGuessR;
    @FXML
    private ImageView wrongGuessS;
    @FXML
    private ImageView wrongGuessT;
    @FXML
    private ImageView wrongGuessU;
    @FXML
    private ImageView wrongGuessV;
    @FXML
    private ImageView wrongGuessW;
    @FXML
    private ImageView wrongGuessX;
    @FXML
    private ImageView wrongGuessY;
    @FXML
    private ImageView wrongGuessZ;

    private Font amaticSC;
    private Font pencilant;
    private Font quickPencil;
    private final String QUICKPENCIL_FONT_PATH = "/css/fonts/QuickPencilRegular-0R59.ttf";
    private final String AMATICSC_FONT_PATH = "/css/fonts/AmaticSC-Bold.ttf";
    private final String PENCILANT_FONT_PATH = "/css/fonts/Pencilant Script.ttf";
    private final String LETTER_HOVER_IMAGE_PATH = "/images/testUI/hovers/letter_hovered.png";

    // Maps for letter ImageViews
    private final Map<Character, ImageView> correctLetterImages = new HashMap<>();
    private final Map<Character, ImageView> wrongLetterImages = new HashMap<>();
    private final Map<Character, Button> alphabetButtons = new HashMap<>();

    // Reference to controller
    private GameRoomController controller;
    private Timeline roundTimer;
    private int secondsRemaining;
    private final List<Label> blankLabels = new ArrayList<>();
    private boolean isKeyboardInputEnabled = false; // Track keyboard input state
    private boolean endPopupShowing = false; // Track if round end popup is showing
    private boolean waitingPopupShowing = false; // Track if waiting popup is showing
    private Stage waitingPopupStage; // Track the waiting popup
    private Rectangle waitingOverlay; // Track the overlay for the waiting popup

    @FXML
    private void initialize() {
        loadCustomFonts();
        applyFonts();
        loadImage(gameRoomBackgroundImage, "/images/testUI/game_room.png");
        setupLetterImages();
        Platform.runLater(() -> {
            setupAlphabet();
            disableAlphabetButtons(); // Disable buttons initially
            setupKeyboardInput(); // Set up keyboard input
        });
        quitButton.setOnAction(this::handleQuitButton);
    }

    // Allow controller to register itself
    public void setController(GameRoomController controller) {
        this.controller = controller;
    }

    private void loadImage(ImageView imageView, String resourcePath) {
        try {
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is != null) {
                imageView.setImage(new Image(is));
                System.out.println("[DEBUG] Successfully loaded resource: " + resourcePath);
            } else {
                System.err.println("[ERROR] Image resource not found: " + resourcePath);
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to load image: " + resourcePath);
            e.printStackTrace();
        }
    }

    private Font loadFont(String path, String fontName) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                System.err.println("[ERROR] Font resource not found: " + path);
                return null;
            }
            Font font = Font.loadFont(is, 10);
            is.close();
            if (font != null) {
                System.out.println("[DEBUG] Successfully loaded font: " + fontName + ", Family: " + font.getFamily() + ", Path: " + path);
            } else {
                System.err.println("[ERROR] Failed to load font: " + fontName + " from " + path);
            }
            return font;
        } catch (IOException e) {
            System.err.println("[ERROR] IO Exception loading font " + fontName + " from " + path + ": " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("[ERROR] Unexpected error loading font " + fontName + " from " + path + ": " + e.getMessage());
            return null;
        }
    }

    private void loadCustomFonts() {
        pencilant = loadFont(PENCILANT_FONT_PATH, "Pencilant Script");
        quickPencil = loadFont(QUICKPENCIL_FONT_PATH, "Quick Pencil Regular");
        amaticSC = loadFont(AMATICSC_FONT_PATH, "AmaticSC Bold");

        if (pencilant == null || !pencilant.getFamily().toLowerCase().contains("pencilant")) {
            System.err.println("[WARNING] Pencilant font not loaded or incorrect family. Using fallback: Arial");
            pencilant = Font.font("Arial", 12);
        }
        if (quickPencil == null || !quickPencil.getFamily().toLowerCase().contains("quick pencil")) {
            System.err.println("[WARNING] QuickPencil font not loaded or incorrect family. Using fallback: Arial");
            quickPencil = Font.font("Arial", 12);
        }
        if (amaticSC == null || !amaticSC.getFamily().toLowerCase().contains("amatic")) {
            System.err.println("[WARNING] AmaticSC font not loaded or incorrect family. Using fallback: Arial");
            amaticSC = Font.font("Arial", 12);
        }

        System.out.println("[DEBUG] Font families - Pencilant: " + pencilant.getFamily() +
                ", QuickPencil: " + quickPencil.getFamily() +
                ", AmaticSC: " + amaticSC.getFamily());
    }

    private void applyFonts() {
        if (roundLabel != null) {
            roundLabel.setFont(Font.font(pencilant.getFamily(), 34));
            System.out.println("[DEBUG] Applied font to roundLabel: " + pencilant.getFamily() + ", size: 34");
        }
        if (playerCountLabel != null) {
            playerCountLabel.setFont(Font.font(pencilant.getFamily(), 36));
            System.out.println("[DEBUG] Applied font to playerCountLabel: " + pencilant.getFamily() + ", size: 36");
        }
        if (timerLabel != null) {
            timerLabel.setFont(Font.font(pencilant.getFamily(), 38));
            System.out.println("[DEBUG] Applied font to timerLabel: " + pencilant.getFamily() + ", size: 38");
        }
        if (timeLabel != null) {
            timeLabel.setFont(Font.font(pencilant.getFamily(), 38));
            System.out.println("[DEBUG] Applied font to timeLabel: " + pencilant.getFamily() + ", size: 38");
        }
        if (playersLabel != null) {
            playersLabel.setFont(Font.font(pencilant.getFamily(), 38));
            System.out.println("[DEBUG] Applied font to playersLabel: " + pencilant.getFamily() + ", size: 38");
        }
        if (quitButton != null) {
            quitButton.setFont(Font.font(pencilant.getFamily(), 34));
            System.out.println("[DEBUG] Applied font to quitButton: " + pencilant.getFamily() + ", size: 34");
        }
        for (Label blankLabel : blankLabels) {
            if (blankLabel != null) {
                blankLabel.setFont(Font.font(pencilant.getFamily(), 88));
                System.out.println("[DEBUG] Applied font to blankLabel: " + pencilant.getFamily() + ", size: 88");
            }
        }
        for (Button btn : alphabetButtons.values()) {
            if (btn != null) {
                btn.setFont(Font.font(pencilant.getFamily(), 20));
                System.out.println("[DEBUG] Applied font to alphabet button: " + pencilant.getFamily() + ", size: 20");
            }
        }
    }

    private void setupLetterImages() {
        correctLetterImages.put('A', correctGuessA);
        correctLetterImages.put('B', correctGuessB);
        correctLetterImages.put('C', correctGuessC);
        correctLetterImages.put('D', correctGuessD);
        correctLetterImages.put('E', correctGuessE);
        correctLetterImages.put('F', correctGuessF);
        correctLetterImages.put('G', correctGuessG);
        correctLetterImages.put('H', correctGuessH);
        correctLetterImages.put('I', correctGuessI);
        correctLetterImages.put('J', correctGuessJ);
        correctLetterImages.put('K', correctGuessK);
        correctLetterImages.put('L', correctGuessL);
        correctLetterImages.put('M', correctGuessM);
        correctLetterImages.put('N', correctGuessN);
        correctLetterImages.put('O', correctGuessO);
        correctLetterImages.put('P', correctGuessP);
        correctLetterImages.put('Q', correctGuessQ);
        correctLetterImages.put('R', correctGuessR);
        correctLetterImages.put('S', correctGuessS);
        correctLetterImages.put('T', correctGuessT);
        correctLetterImages.put('U', correctGuessU);
        correctLetterImages.put('V', correctGuessV);
        correctLetterImages.put('W', correctGuessW);
        correctLetterImages.put('X', correctGuessX);
        correctLetterImages.put('Y', correctGuessY);
        correctLetterImages.put('Z', correctGuessZ);

        wrongLetterImages.put('A', wrongGuessA);
        wrongLetterImages.put('B', wrongGuessB);
        wrongLetterImages.put('C', wrongGuessC);
        wrongLetterImages.put('D', wrongGuessD);
        wrongLetterImages.put('E', wrongGuessE);
        wrongLetterImages.put('F', wrongGuessF);
        wrongLetterImages.put('G', wrongGuessG);
        wrongLetterImages.put('H', wrongGuessH);
        wrongLetterImages.put('I', wrongGuessI);
        wrongLetterImages.put('J', wrongGuessJ);
        wrongLetterImages.put('K', wrongGuessK);
        wrongLetterImages.put('L', wrongGuessL);
        wrongLetterImages.put('M', wrongGuessM);
        wrongLetterImages.put('N', wrongGuessN);
        wrongLetterImages.put('O', wrongGuessO);
        wrongLetterImages.put('P', wrongGuessP);
        wrongLetterImages.put('Q', wrongGuessQ);
        wrongLetterImages.put('R', wrongGuessR);
        wrongLetterImages.put('S', wrongGuessS);
        wrongLetterImages.put('T', wrongGuessT);
        wrongLetterImages.put('U', wrongGuessU);
        wrongLetterImages.put('V', wrongGuessV);
        wrongLetterImages.put('W', wrongGuessW);
        wrongLetterImages.put('X', wrongGuessX);
        wrongLetterImages.put('Y', wrongGuessY);
        wrongLetterImages.put('Z', wrongGuessZ);
    }

    private void setupAlphabet() {
        java.net.URL hoverImageUrl = getClass().getResource(LETTER_HOVER_IMAGE_PATH);
        if (hoverImageUrl == null) {
            System.err.println("[ERROR] Hover image not found: " + LETTER_HOVER_IMAGE_PATH);
        } else {
            System.out.println("[DEBUG] Hover image found: " + hoverImageUrl);
        }

        if (blanksFlowPane.getScene() == null) {
            System.err.println("[ERROR] Scene not set for blanksFlowPane in setupAlphabet");
            return;
        }
        Pane alphabetPane = (Pane) blanksFlowPane.getScene().lookup("#alphabetPane");
        if (alphabetPane == null) {
            System.err.println("[ERROR] alphabetPane not found in Scene");
            return;
        }

        String[] row1 = {"A", "B", "C", "D", "E", "F", "G", "H"};
        String[] row2 = {"I", "J", "K", "L", "M", "N", "O", "P", "Q"};
        String[] row3 = {"R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

        final String baseStyle = "-fx-background-color: transparent; -fx-text-fill: white; -fx-opacity: 0.0;";
        final String hoverStyle = "-fx-background-color: transparent; -fx-background-image: url('" + LETTER_HOVER_IMAGE_PATH + "'); -fx-background-size: cover; -fx-text-fill: white;";
        final String disabledStyle = "-fx-background-color: grey; -fx-text-fill: white; -fx-opacity: 0.0;";

        int buttonIndex = 0;
        for (int i = 0; i < row1.length && buttonIndex < alphabetPane.getChildren().size(); i++) {
            Node node = alphabetPane.getChildren().get(buttonIndex);
            if (node instanceof Button) {
                Button btn = (Button) node;
                char letter = row1[i].charAt(0);
                btn.setUserData(letter);
                btn.setText(row1[i]);
                btn.setStyle(baseStyle);
                btn.setOnAction(e -> handleLetterGuess(letter));
                btn.setOnMouseEntered(e -> {
                    if (!btn.isDisable()) {
                        btn.setStyle(hoverStyle);
                    }
                });
                btn.setOnMouseExited(e -> {
                    if (!btn.isDisable()) {
                        btn.setStyle(baseStyle);
                    }
                });
                alphabetButtons.put(letter, btn);
                buttonIndex++;
            }
        }

        for (int i = 0; i < row2.length && buttonIndex < alphabetPane.getChildren().size(); i++) {
            Node node = alphabetPane.getChildren().get(buttonIndex);
            if (node instanceof Button) {
                Button btn = (Button) node;
                char letter = row2[i].charAt(0);
                btn.setUserData(letter);
                btn.setText(row2[i]);
                btn.setStyle(baseStyle);
                btn.setOnAction(e -> handleLetterGuess(letter));
                btn.setOnMouseEntered(e -> {
                    if (!btn.isDisable()) {
                        btn.setStyle(hoverStyle);
                    }
                });
                btn.setOnMouseExited(e -> {
                    if (!btn.isDisable()) {
                        btn.setStyle(baseStyle);
                    }
                });
                alphabetButtons.put(letter, btn);
                buttonIndex++;
            }
        }

        for (int i = 0; i < row3.length && buttonIndex < alphabetPane.getChildren().size(); i++) {
            Node node = alphabetPane.getChildren().get(buttonIndex);
            if (node instanceof Button) {
                Button btn = (Button) node;
                char letter = row3[i].charAt(0);
                btn.setUserData(letter);
                btn.setText(row3[i]);
                btn.setStyle(baseStyle);
                btn.setOnAction(e -> handleLetterGuess(letter));
                btn.setOnMouseEntered(e -> {
                    if (!btn.isDisable()) {
                        btn.setStyle(hoverStyle);
                    }
                });
                btn.setOnMouseExited(e -> {
                    if (!btn.isDisable()) {
                        btn.setStyle(baseStyle);
                    }
                });
                alphabetButtons.put(letter, btn);
                buttonIndex++;
            }
        }

        if (buttonIndex != 26) {
            System.err.println("[WARNING] Expected 26 alphabet buttons, found " + buttonIndex);
        }
        applyFonts();
    }

    private void setupKeyboardInput() {
        if (blanksFlowPane.getScene() != null) {
            blanksFlowPane.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (!isKeyboardInputEnabled) {
                    System.out.println("[DEBUG] Keyboard input disabled, ignoring key: " + event.getText());
                    event.consume();
                    return;
                }
                String key = event.getText().toUpperCase();
                if (key.length() == 1) {
                    char letter = key.charAt(0);
                    if (letter >= 'A' && letter <= 'Z') {
                        Button button = alphabetButtons.get(letter);
                        if (button != null && !button.isDisabled() && controller != null) {
                            System.out.println("[DEBUG] Keyboard input: Letter " + letter);
                            handleLetterGuess(letter);
                        }
                    }
                }
                event.consume();
            });
        } else {
            System.err.println("[ERROR] Scene not set for blanksFlowPane in setupKeyboardInput");
        }
    }

    public void enableKeyboardInput() {
        isKeyboardInputEnabled = true;
        System.out.println("[DEBUG] Keyboard input enabled");
    }

    public void disableKeyboardInput() {
        isKeyboardInputEnabled = false;
        System.out.println("[DEBUG] Keyboard input disabled");
    }

    private void handleLetterGuess(char letter) {
        System.out.println("[DEBUG] handleLetterGuess called for letter: " + letter);
        if (controller != null) {
            try {
                controller.handleGuess(letter);
            } catch (Exception e) {
                System.err.println("[ERROR] Failed to process guess for letter " + letter + ": " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("[ERROR] Controller not set in GameRoomView");
        }
    }

    public void showCorrectLetter(char letter) {
        ImageView letterImage = correctLetterImages.get(letter);
        if (letterImage != null) {
            letterImage.setVisible(true);
        } else {
            System.err.println("[ERROR] Correct letter image not found for: " + letter);
        }
    }

    public void showWrongLetter(char letter) {
        ImageView letterImage = wrongLetterImages.get(letter);
        if (letterImage != null) {
            letterImage.setVisible(true);
        } else {
            System.err.println("[ERROR] Wrong letter image not found for: " + letter);
        }
    }

    public void disableLetterButton(char letter) {
        Button button = alphabetButtons.get(letter);
        if (button != null) {
            button.setDisable(true);
            button.setStyle("-fx-background-color: grey; -fx-text-fill: white; -fx-opacity: 0.0;");
        } else {
            System.err.println("[ERROR] Button not found for letter: " + letter);
        }
    }

    public void disableAlphabetButtons() {
        System.out.println("[DEBUG] Disabling alphabet buttons");
        alphabetButtons.values().forEach(btn -> {
            btn.setDisable(true);
            btn.setStyle("-fx-background-color: grey; -fx-text-fill: white; -fx-opacity: 0.0;");
        });
    }

    public void enableAlphabetButtons() {
        final String baseStyle = "-fx-background-color: transparent; -fx-text-fill: white; -fx-opacity: 0.0;";
        System.out.println("[DEBUG] Enabling alphabet buttons");
        alphabetButtons.values().forEach(btn -> {
            btn.setDisable(false);
            btn.setStyle(baseStyle);
        });
    }

    public void resetAlphabetButtons() {
        final String baseStyle = "-fx-background-color: transparent; -fx-text-fill: white; -fx-opacity: 0.0;";
        alphabetButtons.values().forEach(btn -> {
            btn.setDisable(false);
            btn.setStyle(baseStyle);
        });
        correctLetterImages.values().forEach(img -> img.setVisible(false));
        wrongLetterImages.values().forEach(img -> img.setVisible(false));
    }

    public void updateLifeCount(int lives) {
        if (playerCountLabel != null) {
            playerCountLabel.setText(String.valueOf(lives));
            updateHeartsImage(lives);
        } else {
            System.err.println("[ERROR] playerCountLabel is null in updateLifeCount");
        }
    }

    private void updateHeartsImage(int lives) {
        String imagePath;
        switch (lives) {
            case 5:
                imagePath = "/images/hearts/5heart.png";
                break;
            case 4:
                imagePath = "/images/hearts/4heart.png";
                break;
            case 3:
                imagePath = "/images/hearts/3heart.png";
                break;
            case 2:
                imagePath = "/images/hearts/2heart.png";
                break;
            case 1:
                imagePath = "/images/hearts/1heart.png";
                break;
            case 0:
                imagePath = "/images/hearts/0heart.png";
                break;
            default:
                imagePath = "/images/hearts/0heart.png";
                System.err.println("[WARNING] Invalid lives count: " + lives + ", defaulting to 0heart.png");
                break;
        }
        loadImage(heartsImageView, imagePath);
    }

    public void updateRoundLabel(int roundNum) {
        roundLabel.setText("R" + roundNum);
    }

    public void setupBlanks(int wordLength) {
        blanksFlowPane.getChildren().clear();
        blankLabels.clear();
        for (int i = 0; i < wordLength; i++) {
            Pane cell = new Pane();
            cell.setPrefSize(58, 68);
            ImageView blankImg = new ImageView();
            blankImg.setFitWidth(58);
            blankImg.setFitHeight(68);
            blankImg.setTranslateY(50);

            Label lbl = new Label("_");
            lbl.setStyle("-fx-text-fill:#61ff82;");
            lbl.setPrefSize(58, 68);
            lbl.setAlignment(javafx.geometry.Pos.CENTER);
            cell.getChildren().addAll(blankImg, lbl);
            blanksFlowPane.getChildren().add(cell);
            blankLabels.add(lbl);
        }
        applyFonts();
        System.out.println("[DEBUG] setupBlanks completed, blankLabels size: " + blankLabels.size());
    }

    public void updateBlanks(List<String> labels) {
        for (int i = 0; i < labels.size(); i++) {
            blankLabels.get(i).setText(labels.get(i));
        }
    }

    public void startCountdown(int durationSeconds) {
        if (roundTimer != null) roundTimer.stop();
        secondsRemaining = durationSeconds;
        updateTimerLabel();

        roundTimer = new Timeline(new javafx.animation.KeyFrame(
                Duration.seconds(1), evt -> {
            secondsRemaining--;
            updateTimerLabel();
            if (secondsRemaining <= 0) {
                roundTimer.stop();
                controller.onRoundTimeExpiredFromView();
            }
        }));
        roundTimer.setCycleCount(durationSeconds);
        roundTimer.play();
    }

    public void stopCountdown() {
        if (roundTimer != null) {
            roundTimer.stop();
            secondsRemaining = 0;
            updateTimerLabel();
            System.out.println("[DEBUG] Countdown stopped");
        }
    }

    private void updateTimerLabel() {
        int minutes = secondsRemaining / 60;
        int seconds = secondsRemaining % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    public void showWaitingMessage(String message) {
        Platform.runLater(() -> {
            try {
                if (waitingPopupShowing) {
                    System.out.println("[DEBUG] Waiting popup already showing, ignoring");
                    return;
                }
                waitingPopupShowing = true;

                Stage mainStage = ViewNavigator.getStage();
                if (mainStage == null) return;

                StackPane mainRoot = (StackPane) mainStage.getScene().getRoot();
                waitingOverlay = new Rectangle();
                waitingOverlay.setFill(Color.rgb(0, 0, 0, 0.5));
                waitingOverlay.widthProperty().bind(mainRoot.widthProperty());
                waitingOverlay.heightProperty().bind(mainRoot.heightProperty());
                mainRoot.getChildren().add(waitingOverlay);

                StackPane container = new StackPane();
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
                container.setPadding(new Insets(20));

                Label messageLabel = new Label(message);
                messageLabel.setFont(Font.font(pencilant.getFamily(), 34));
                messageLabel.setTextFill(Color.web("#8B4513"));
                container.getChildren().add(messageLabel);

                waitingPopupStage = new Stage();
                waitingPopupStage.initOwner(mainStage);
                waitingPopupStage.initStyle(StageStyle.TRANSPARENT);

                Scene popupScene = new Scene(container);
                popupScene.setFill(Color.TRANSPARENT);
                waitingPopupStage.setScene(popupScene);

                waitingPopupStage.addEventHandler(WindowEvent.WINDOW_SHOWN, event -> {
                    container.applyCss();
                    container.layout();
                    double centerX = mainStage.getX() + (mainStage.getWidth() - container.getWidth()) / 2;
                    double centerY = mainStage.getY() + (mainStage.getHeight() - container.getHeight()) / 2;
                    waitingPopupStage.setX(centerX);
                    waitingPopupStage.setY(centerY);
                });

                waitingPopupStage.show();
                System.out.println("[DEBUG] Showing waiting popup with message: " + message);
            } catch (Exception e) {
                System.err.println("[ERROR] Failed to show waiting message: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public void closeWaitingMessage() {
        Platform.runLater(() -> {
            if (waitingPopupStage != null && waitingPopupShowing) {
                waitingPopupStage.close();
                waitingPopupStage = null;
                waitingPopupShowing = false;
                System.out.println("[DEBUG] Closed waiting popup");
            }
            if (waitingOverlay != null) {
                StackPane mainRoot = (StackPane) ViewNavigator.getStage().getScene().getRoot();
                mainRoot.getChildren().remove(waitingOverlay);
                waitingOverlay = null;
            }
        });
    }

    public void showRoundEndPopup(String message, boolean hasMoreRounds) {
        Platform.runLater(() -> {
            try {
                if (endPopupShowing) {
                    System.out.println("[DEBUG] Round end popup already showing, ignoring");
                    return;
                }
                endPopupShowing = true;
                closeWaitingMessage(); // Close waiting popup if open

                Stage mainStage = ViewNavigator.getStage();
                if (mainStage == null) {
                    controller.onEndPopupClosed();
                    return;
                }

                StackPane mainRoot = (StackPane) mainStage.getScene().getRoot();
                Rectangle overlay = new Rectangle();
                overlay.setFill(Color.rgb(0, 0, 0, 0.5));
                overlay.widthProperty().bind(mainRoot.widthProperty());
                overlay.heightProperty().bind(mainRoot.heightProperty());
                mainRoot.getChildren().add(overlay);

                StackPane container = new StackPane();
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
                container.setPadding(new Insets(20));

                Label messageLabel = new Label(message);
                messageLabel.setFont(Font.font(pencilant.getFamily(), 34));
                messageLabel.setTextFill(Color.web("#8B4513"));
                container.getChildren().add(messageLabel);

                Stage popupStage = new Stage();
                popupStage.initOwner(mainStage);
                popupStage.initStyle(StageStyle.TRANSPARENT);

                Scene popupScene = new Scene(container);
                popupScene.setFill(Color.TRANSPARENT);
                popupStage.setScene(popupScene);

                popupStage.addEventHandler(WindowEvent.WINDOW_SHOWN, event -> {
                    container.applyCss();
                    container.layout();
                    double centerX = mainStage.getX() + (mainStage.getWidth() - container.getWidth()) / 2;
                    double centerY = mainStage.getY() + (mainStage.getHeight() - container.getHeight()) / 2;
                    popupStage.setX(centerX);
                    popupStage.setY(centerY);
                });

                popupStage.show();

                PauseTransition pause = new PauseTransition(Duration.seconds(5));
                pause.setOnFinished(e -> {
                    popupStage.close();
                    mainRoot.getChildren().remove(overlay);
                    endPopupShowing = false;
                    if (hasMoreRounds) {
                        controller.onEndPopupClosed();
                    }
                });
                pause.play();
            } catch (Exception e) {
                System.err.println("[ERROR] Failed to show round end popup: " + e.getMessage());
                endPopupShowing = false;
                controller.onEndPopupClosed();
                e.printStackTrace();
            }
        });
    }

    public void showGameEndPopup(String champion) {
        Platform.runLater(() -> {
            try {
                closeWaitingMessage(); // Close waiting popup if open
                Stage stage = ViewNavigator.getStage();
                if (stage == null) {
                    navigateToLobby();
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
                wait.setOnFinished(evt -> navigateToLobby());
                wait.play();
            } catch (IOException e) {
                System.err.println("[ERROR] Failed to show game end popup: " + e.getMessage());
                navigateToLobby();
            }
        });
    }

    private void handleQuitButton(ActionEvent e) {
        if (roundTimer != null) roundTimer.stop();
        closeWaitingMessage();
        navigateToLobby();
    }

    private void navigateToLobby() {
        try {
            ViewNavigator.goToLobby();
        } catch (Exception ex) {
            System.err.println("[ERROR] Failed to return to lobby: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void scheduleRetryRoundStart(int newRound, int seconds) {
        PauseTransition retry = new PauseTransition(Duration.seconds(seconds));
        retry.setOnFinished(e -> controller.notifyRoundStartFromCallback(newRound));
        retry.play();
    }

    public void showRoundStartScene(int roundNum) {
        Platform.runLater(() -> {
            try {
                Stage mainStage = ViewNavigator.getStage();
                if (mainStage == null) {
                    controller.handleServerRoundStart(roundNum);
                    return;
                }

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/player/GameRoundPopup.fxml"));
                Parent popupRoot = loader.load();
                GameRoundPopupView ctrl = loader.getController();
                ctrl.setGameTitle("What's The Word?");
                ctrl.setRoundNumber(roundNum);

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

                Stage popupStage = new Stage();
                popupStage.initModality(Modality.APPLICATION_MODAL);
                popupStage.initOwner(mainStage);
                popupStage.initStyle(StageStyle.TRANSPARENT);

                Scene popupScene = new Scene(container);
                popupScene.setFill(Color.TRANSPARENT);
                popupStage.setScene(popupScene);

                container.layout();
                popupStage.sizeToScene();

                double mainX = mainStage.getX();
                double mainY = mainStage.getY();
                double mainWidth = mainStage.getWidth();
                double mainHeight = mainStage.getHeight();

                double popupWidth = container.getBoundsInLocal().getWidth();
                double popupHeight = container.getBoundsInLocal().getHeight();

                popupStage.setX(mainX + (mainWidth - popupWidth) / 2);
                popupStage.setY(mainY + (mainHeight - popupHeight) / 2);

                popupStage.show();

                PauseTransition wait = new PauseTransition(Duration.seconds(3));
                wait.setOnFinished(e -> {
                    popupStage.close();
                    controller.handleServerRoundStart(roundNum);
                });
                wait.play();
            } catch (Exception e) {
                System.err.println("[ERROR] Failed to show round start popup: " + e.getMessage());
                controller.handleServerRoundStart(roundNum);
            }
        });
    }

    public ImageView getGameRoomBackgroundImage() {
        return gameRoomBackgroundImage;
    }

    public FlowPane getBlanksFlowPane() {
        return blanksFlowPane;
    }

    public Label getRoundLabel() {
        return roundLabel;
    }

    public Label getLifeCountLabel() {
        return lifeCountLabel;
    }

    public Label getLifeCountLabel1() {
        return playerCountLabel;
    }

    public Label getTimerLabel() {
        return timerLabel;
    }

    public Button getQuitButton() {
        return quitButton;
    }
}