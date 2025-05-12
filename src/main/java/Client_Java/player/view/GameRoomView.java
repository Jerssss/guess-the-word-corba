package Client_Java.player.view;

import Client_Java.player.controller.GameRoomController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class GameRoomView {
    @FXML private ImageView gameRoomBackgroundImage;
    @FXML private FlowPane blanksFlowPane;
    @FXML private Label roundLabel;
    @FXML private Label lifeCountLabel;
    @FXML private Label lifeCountLabel1; // Players count
    @FXML private Label timerLabel;
    @FXML private Button quitButton;

    // Cat face components (not present in current FXML, kept for compatibility)
    @FXML private ImageView catShadow;
    @FXML private ImageView catTopHead;
    @FXML private ImageView catEyes;
    @FXML private ImageView catSnout;
    @FXML private ImageView catLeftWhiskers;
    @FXML private ImageView catRightWhiskers;
    @FXML private ImageView catBottomHead;

    // Correct guess ImageViews
    @FXML private ImageView correctGuessA;
    @FXML private ImageView correctGuessB;
    @FXML private ImageView correctGuessC;
    @FXML private ImageView correctGuessD;
    @FXML private ImageView correctGuessE;
    @FXML private ImageView correctGuessF;
    @FXML private ImageView correctGuessG;
    @FXML private ImageView correctGuessH;
    @FXML private ImageView correctGuessI;
    @FXML private ImageView correctGuessJ;
    @FXML private ImageView correctGuessK;
    @FXML private ImageView correctGuessL;
    @FXML private ImageView correctGuessM;
    @FXML private ImageView correctGuessN;
    @FXML private ImageView correctGuessO;
    @FXML private ImageView correctGuessP;
    @FXML private ImageView correctGuessQ;
    @FXML private ImageView correctGuessR;
    @FXML private ImageView correctGuessS;
    @FXML private ImageView correctGuessT;
    @FXML private ImageView correctGuessU;
    @FXML private ImageView correctGuessV;
    @FXML private ImageView correctGuessW;
    @FXML private ImageView correctGuessX;
    @FXML private ImageView correctGuessY;
    @FXML private ImageView correctGuessZ;

    // Wrong guess ImageViews
    @FXML private ImageView wrongGuessA;
    @FXML private ImageView wrongGuessB;
    @FXML private ImageView wrongGuessC;
    @FXML private ImageView wrongGuessD;
    @FXML private ImageView wrongGuessE;
    @FXML private ImageView wrongGuessF;
    @FXML private ImageView wrongGuessG;
    @FXML private ImageView wrongGuessH;
    @FXML private ImageView wrongGuessI;
    @FXML private ImageView wrongGuessJ;
    @FXML private ImageView wrongGuessK;
    @FXML private ImageView wrongGuessL;
    @FXML private ImageView wrongGuessM;
    @FXML private ImageView wrongGuessN;
    @FXML private ImageView wrongGuessO;
    @FXML private ImageView wrongGuessP;
    @FXML private ImageView wrongGuessQ;
    @FXML private ImageView wrongGuessR;
    @FXML private ImageView wrongGuessS;
    @FXML private ImageView wrongGuessT;
    @FXML private ImageView wrongGuessU;
    @FXML private ImageView wrongGuessV;
    @FXML private ImageView wrongGuessW;
    @FXML private ImageView wrongGuessX;
    @FXML private ImageView wrongGuessY;
    @FXML private ImageView wrongGuessZ;

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

    @FXML
    private void initialize() {
        loadCustomFonts();
        loadImage(gameRoomBackgroundImage, "/images/testUI/game_room.png");
        setupLetterImages();
        Platform.runLater(this::setupAlphabet);
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
            } else {
                System.err.println("[ERROR] Image resource not found: " + resourcePath);
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to load image: " + resourcePath);
            e.printStackTrace();
        }
    }

    private void loadCustomFonts() {
        try {
            amaticSC = Font.loadFont(getClass().getResourceAsStream(AMATICSC_FONT_PATH), 10);
            pencilant = Font.loadFont(getClass().getResourceAsStream(PENCILANT_FONT_PATH), 10);

            if (amaticSC == null) {
                System.err.println("[ERROR] AmaticSC font not loaded. Using system font.");
                amaticSC = Font.font("System", 12);
            }
            if (pencilant == null) {
                System.err.println("[ERROR] Pencilant font not loaded. Using system font.");
                pencilant = Font.font("System", 12);
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Font loading exception: " + e.getMessage());
            amaticSC = Font.font("System", 12);
            pencilant = Font.font("System", 12);
        }
    }

    private void setupLetterImages() {
        // Map correct letter ImageViews
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

        // Map wrong letter ImageViews
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
        // Debug hover image (kept for reference, but not used in hover style)
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

        // Define the letters for each row based on the 26 buttons in FXML order
        String[] row1 = {"A", "B", "C", "D", "E", "F", "G", "H"}; // First 8 buttons
        String[] row2 = {"I", "J", "K", "L", "M", "N", "O", "P", "Q"}; // Next 9 buttons
        String[] row3 = {"R", "S", "T", "U", "V", "W", "X", "Y", "Z"}; // Last 9 buttons

        // Base style for buttons (invisible)
        final String baseStyle = "-fx-font-size: 20; -fx-font-family: 'Pencilant Script'; -fx-background-color: transparent; -fx-text-fill: white; -fx-opacity: 0.0;";
        // Hover style (invisible, no background image)
        final String hoverStyle = "-fx-font-size: 20; -fx-font-family: 'Pencilant Script'; -fx-background-color: transparent; -fx-text-fill: white; -fx-opacity: 0.0;";
        // Disabled style (still invisible but greyed out)
        final String disabledStyle = "-fx-font-size: 20; -fx-font-family: 'Pencilant Script'; -fx-background-color: grey; -fx-text-fill: white; -fx-opacity: 0.0;";

        int buttonIndex = 0;
        // First row (A to H) - first 8 buttons
        for (int i = 0; i < row1.length && buttonIndex < alphabetPane.getChildren().size(); i++) {
            Node node = alphabetPane.getChildren().get(buttonIndex);
            if (node instanceof Button) {
                Button btn = (Button) node;
                char letter = row1[i].charAt(0);
                btn.setUserData(letter);
                btn.setText(row1[i]);
                btn.setStyle(baseStyle);
                btn.setOnAction(e -> handleLetterGuess(letter));
                // Add hover effect (no visual change, just logging for debug)
                btn.setOnMouseEntered(e -> {
                    if (!btn.isDisable()) {
                        System.out.println("[DEBUG] Hovering over letter: " + letter);
                        btn.setStyle(hoverStyle); // Remains invisible
                    }
                });
                btn.setOnMouseExited(e -> {
                    if (!btn.isDisable()) {
                        btn.setStyle(baseStyle); // Remains invisible
                    }
                });
                alphabetButtons.put(letter, btn);
                buttonIndex++;
            }
        }

        // Second row (I to Q) - next 9 buttons
        for (int i = 0; i < row2.length && buttonIndex < alphabetPane.getChildren().size(); i++) {
            Node node = alphabetPane.getChildren().get(buttonIndex);
            if (node instanceof Button) {
                Button btn = (Button) node;
                char letter = row2[i].charAt(0);
                btn.setUserData(letter);
                btn.setText(row2[i]);
                btn.setStyle(baseStyle);
                btn.setOnAction(e -> handleLetterGuess(letter));
                // Add hover effect (no visual change)
                btn.setOnMouseEntered(e -> {
                    if (!btn.isDisable()) {
                        System.out.println("[DEBUG] Hovering over letter: " + letter);
                        btn.setStyle(hoverStyle); // Remains invisible
                    }
                });
                btn.setOnMouseExited(e -> {
                    if (!btn.isDisable()) {
                        btn.setStyle(baseStyle); // Remains invisible
                    }
                });
                alphabetButtons.put(letter, btn);
                buttonIndex++;
            }
        }

        // Third row (R to Z) - last 9 buttons
        for (int i = 0; i < row3.length && buttonIndex < alphabetPane.getChildren().size(); i++) {
            Node node = alphabetPane.getChildren().get(buttonIndex);
            if (node instanceof Button) {
                Button btn = (Button) node;
                char letter = row3[i].charAt(0);
                btn.setUserData(letter);
                btn.setText(row3[i]);
                btn.setStyle(baseStyle);
                btn.setOnAction(e -> handleLetterGuess(letter));
                // Add hover effect (no visual change)
                btn.setOnMouseEntered(e -> {
                    if (!btn.isDisable()) {
                        System.out.println("[DEBUG] Hovering over letter: " + letter);
                        btn.setStyle(hoverStyle); // Remains invisible
                    }
                });
                btn.setOnMouseExited(e -> {
                    if (!btn.isDisable()) {
                        btn.setStyle(baseStyle); // Remains invisible
                    }
                });
                alphabetButtons.put(letter, btn);
                buttonIndex++;
            }
        }

        if (buttonIndex != 26) {
            System.err.println("[WARNING] Expected 26 alphabet buttons, found " + buttonIndex);
        }
    }

    private void handleLetterGuess(char letter) {
        System.out.println("[DEBUG] Letter pressed: " + letter);
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
            button.setStyle("-fx-font-size: 20; -fx-font-family: 'Pencilant Script'; -fx-background-color: grey; -fx-text-fill: white; -fx-opacity: 0.0;");
        } else {
            System.err.println("[ERROR] Button not found for letter: " + letter);
        }
    }

    public void disableAlphabetButtons() {
        alphabetButtons.values().forEach(btn -> {
            btn.setDisable(true);
            btn.setStyle("-fx-font-size: 20; -fx-font-family: 'Pencilant Script'; -fx-background-color: grey; -fx-text-fill: white; -fx-opacity: 0.0;");
        });
    }

    public void enableAlphabetButtons() {
        final String baseStyle = "-fx-font-size: 20; -fx-font-family: 'Pencilant Script'; -fx-background-color: transparent; -fx-text-fill: white; -fx-opacity: 0.0;";
        alphabetButtons.values().forEach(btn -> {
            btn.setDisable(false);
            btn.setStyle(baseStyle);
        });
    }

    public void resetAlphabetButtons() {
        final String baseStyle = "-fx-font-size: 20; -fx-font-family: 'Pencilant Script'; -fx-background-color: transparent; -fx-text-fill: white; -fx-opacity: 0.0;";
        alphabetButtons.values().forEach(btn -> {
            btn.setDisable(false);
            btn.setStyle(baseStyle);
        });
        correctLetterImages.values().forEach(img -> img.setVisible(false));
        wrongLetterImages.values().forEach(img -> img.setVisible(false));
    }

    // Getters
    public ImageView getGameRoomBackgroundImage() { return gameRoomBackgroundImage; }
    public FlowPane getBlanksFlowPane() { return blanksFlowPane; }
    public Label getRoundLabel() { return roundLabel; }
    public Label getLifeCountLabel() { return lifeCountLabel; }
    public Label getLifeCountLabel1() { return lifeCountLabel1; }
    public Label getTimerLabel() { return timerLabel; }
    public Button getQuitButton() { return quitButton; }
    public ImageView getCatShadow() { return catShadow; }
    public ImageView getCatTopHead() { return catTopHead; }
    public ImageView getCatEyes() { return catEyes; }
    public ImageView getCatSnout() { return catSnout; }
    public ImageView getCatLeftWhiskers() { return catLeftWhiskers; }
    public ImageView getCatRightWhiskers() { return catRightWhiskers; }
    public ImageView getCatBottomHead() { return catBottomHead; }
}