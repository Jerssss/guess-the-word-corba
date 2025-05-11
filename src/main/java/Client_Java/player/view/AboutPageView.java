package Client_Java.player.view;

import javafx.fxml.FXML;
import javafx.geometry.BoundingBox;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.text.Font;

import java.io.InputStream;

public class AboutPageView {
    @FXML private ImageView backgroundImageView;
    @FXML private Button returnButton;
    @FXML private Label titleLabel;
    @FXML private ScrollPane aboutScrollPane;
    @FXML private ScrollPane htpScrollPane;
    @FXML private ScrollPane rulesScrollPane;
    @FXML private AnchorPane aboutAnchorPane;
    @FXML private AnchorPane htpAnchorPane;
    @FXML private AnchorPane rulesAnchorPane;
    @FXML private Button aboutButton;
    @FXML private Button htpButton;
    @FXML private Button rulesButton;
    @FXML private TextFlow aboutTextFlow;
    @FXML private TextFlow htpTextFlow;
    @FXML private TextFlow rulesTextFlow;

    private Font quickPencil;
    private final String QUICKPENCIL_FONT_PATH = "/css/fonts/QuickPencilRegular-0R59.ttf";

    private static final String ABOUT_IMAGE = "/images/testUI/purple_about_crystal.png";
    private static final String ABOUT_SELECTED_IMAGE = "/images/testUI/purple_about_crystal_selected.png";
    private static final String HTP_IMAGE = "/images/testUI/red_about_crystal.png";
    private static final String HTP_SELECTED_IMAGE = "/images/testUI/red_about_crystal_selected.png";
    private static final String RULES_IMAGE = "/images/testUI/pink_about_crystal.png";
    private static final String RULES_SELECTED_IMAGE = "/images/testUI/pink_about_crystal_selected.png";

    private Runnable returnAction;

    public void initialize() {
        loadCustomFonts();
        verifyResources();
        applyFonts();
        setupButtons();
        setupScrollPanes();
        setupReturnButton();
        populateContent();
        showAboutPane();
    }

    private void loadCustomFonts() {
        try {
            quickPencil = Font.loadFont(getClass().getResourceAsStream(QUICKPENCIL_FONT_PATH), 10);
            if (quickPencil == null) {
                System.err.println("Quick Pencil font not loaded. Using system font.");
                quickPencil = Font.font("System", 12);
            } else {
                System.out.println("Quick Pencil font loaded successfully.");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Font loading exception: " + e.getMessage());
            quickPencil = Font.font("System", 12);
        }
    }

    private void applyFonts() {
        if (returnButton != null) {
            returnButton.setFont(Font.font(quickPencil.getFamily(), 40.0));
        } else {
            System.err.println("returnButton is null - cannot apply font");
        }

        if (titleLabel != null) {
            titleLabel.setFont(Font.font(quickPencil.getFamily(), 37.0));
        } else {
            System.err.println("titleLabel is null - cannot apply font");
        }

        if (htpButton != null) {
            htpButton.setFont(Font.font(quickPencil.getFamily(), 32.0));
        } else {
            System.err.println("htpButton is null - cannot apply font");
        }

        if (aboutButton != null) {
            aboutButton.setFont(Font.font(quickPencil.getFamily(), 39.0));
        } else {
            System.err.println("aboutButton is null - cannot apply font");
        }

        if (rulesButton != null) {
            rulesButton.setFont(Font.font(quickPencil.getFamily(), 45.0));
        } else {
            System.err.println("rulesButton is null - cannot apply font");
        }
    }

    private void verifyResources() {
        checkImage(ABOUT_IMAGE);
        checkImage(ABOUT_SELECTED_IMAGE);
        checkImage(HTP_IMAGE);
        checkImage(HTP_SELECTED_IMAGE);
        checkImage(RULES_IMAGE);
        checkImage(RULES_SELECTED_IMAGE);
        checkImage("/images/testUI/about.png");
        checkImage("/images/testUI/about_pane_background.png");
    }

    private void checkImage(String path) {
        try {
            if (getClass().getResource(path) == null) {
                System.err.println("Image resource not found: " + path);
            } else {
                System.out.println("Image resource found: " + path);
            }
        } catch (Exception e) {
            System.err.println("Error checking image resource: " + path);
            e.printStackTrace();
        }
    }

    private void setupButtons() {
        if (aboutButton == null || htpButton == null || rulesButton == null) {
            System.err.println("One or more buttons are null - check FXML fx:id");
            return;
        }

        // Set button actions
        aboutButton.setOnAction(e -> showAboutPane());
        htpButton.setOnAction(e -> showHtpPane());
        rulesButton.setOnAction(e -> showRulesPane());

        // Set initial images (About selected by default)
        setButtonGraphic(aboutButton, ABOUT_SELECTED_IMAGE);
        setButtonGraphic(htpButton, HTP_IMAGE);
        setButtonGraphic(rulesButton, RULES_IMAGE);
    }

    private void setButtonGraphic(Button button, String imagePath) {
        try {
            StackPane stackPane = (StackPane) button.getParent();
            if (stackPane == null) {
                System.err.println("Button parent is not a StackPane: " + button);
                return;
            }

            ImageView imageView = (ImageView) stackPane.getChildren().stream()
                    .filter(node -> node instanceof ImageView)
                    .findFirst()
                    .orElse(null);

            if (imageView == null) {
                System.err.println("No ImageView found in StackPane for button: " + button);
                return;
            }

            InputStream imageStream = getClass().getResourceAsStream(imagePath);
            if (imageStream == null) {
                System.err.println("Failed to load button image (null stream): " + imagePath);
                imageView.setImage(null);
                return;
            }

            Image image = new Image(imageStream);
            imageView.setImage(image);
            imageView.setFitWidth(253);
            imageView.setFitHeight(204);
            imageView.setPreserveRatio(true);
            System.out.println("Successfully loaded button image: " + imagePath);
        } catch (Exception e) {
            System.err.println("Error loading button image: " + imagePath);
            e.printStackTrace();
        }
    }

    private void setupScrollPanes() {
        makeScrollPaneTransparent(aboutScrollPane);
        aboutScrollPane.setViewportBounds(new BoundingBox(0, 0, 650, 286));
        makeScrollPaneTransparent(htpScrollPane);
        htpScrollPane.setViewportBounds(new BoundingBox(0, 0, 650, 286));
        makeScrollPaneTransparent(rulesScrollPane);
        rulesScrollPane.setViewportBounds(new BoundingBox(0, 0, 650, 286));
    }

    private void makeScrollPaneTransparent(ScrollPane scrollPane) {
        if (scrollPane != null) {
            scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
            scrollPane.setBackground(Background.EMPTY);
            if (scrollPane.getContent() instanceof AnchorPane) {
                ((AnchorPane) scrollPane.getContent()).setBackground(Background.EMPTY);
            }
        }
    }

    private void configureScrollPane(ScrollPane scrollPane) {
        if (scrollPane != null) {
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
            scrollPane.setBackground(Background.EMPTY);

            if (scrollPane.getContent() instanceof AnchorPane) {
                AnchorPane content = (AnchorPane) scrollPane.getContent();
                content.setBackground(Background.EMPTY);
                content.setMinHeight(Region.USE_PREF_SIZE);
            }
        }
    }

    private void setupReturnButton() {
        if (returnButton != null) {
            returnButton.setOnAction(event -> handleReturn());
        } else {
            System.err.println("returnButton is null - check FXML");
        }
    }

    private void populateContent() {
        Glow glow = new Glow(0.7);

        // About Us content
        if (aboutAnchorPane != null) {
            TextFlow aboutTextFlow = new TextFlow();
            Text aboutText = new Text("WHAT'S THE WORD?!\n" +
                    "Created by Team 1:\n" +
                    "\n" +
                    "ANG-ANGCO, JEREMIAH\n" +
                    "\n" +
                    "ESPERANZATE, ANJELO\n" +
                    "\n" +
                    "FLORES, GABRIEL ALYSON\n" +
                    "\n" +
                    "GRABANZOR, GIANA KRISTY\n" +
                    "\n" +
                    "MOLINA, BERNARD SEBASTHIAN\n" +
                    "\n" +
                    "TENORIO, KRISTELLE MAYE\n" +
                    "\n" +
                    "Game Description\n" +
                    "A fast-paced multiplayer word-guessing game that reinvents classic Hangman with competitive elements. " +
                    "Race against friends to solve the mystery word first in this quick-thinking challenge!");
            aboutText.setFont(Font.font(quickPencil.getFamily(), 20));
            aboutText.setFill(javafx.scene.paint.Color.WHITE);
            aboutText.setEffect(glow);
            aboutText.setFill(javafx.scene.paint.Color.BLACK);
            aboutTextFlow.getChildren().add(aboutText);
            aboutAnchorPane.getChildren().add(aboutTextFlow);
            aboutTextFlow.setLayoutX(10);
            aboutTextFlow.setLayoutY(10);
            aboutTextFlow.setPrefWidth(610);
        }

        // How To Play content
        if (htpAnchorPane != null) {
            TextFlow htpTextFlow = new TextFlow();
            Text htpText = new Text("How to Play\n" +
                    "Starting the Game\n" +
                    "Initiate a game from the lobby.\n" +
                    "\n" +
                    "Play starts if ≥1 player joins within 10 seconds.\n" +
                    "\n" +
                    "Round Structure\n" +
                    "Goal: First to 3 round wins wins the game.\n" +
                    "\n" +
                    "Clue: Word length is revealed at the start.\n" +
                    "\n" +
                    "Guessing:\n" +
                    "\n" +
                    "Submit one letter at a time (reveals all positions if correct).\n" +
                    "\n" +
                    "Guess the full word anytime for an instant win.\n" +
                    "\n" +
                    "Winning a Round\n" +
                    "Fastest correct guess wins.\n" +
                    "\n" +
                    "Tiebreaker: Earliest submission.\n" +
                    "\n" +
                    "No winner if no one guesses correctly.\n" +
                    "\n" +
                    "Word Selection\n" +
                    "Randomly chosen from a curated list.\n" +
                    "\n" +
                    "No repeats within a game.\n" +
                    "\n" +
                    "Game End\n" +
                    "First to 3 round victories wins the match.\n" +
                    "\n" +
                    "All players return to the lobby post-game.");
            htpText.setFont(Font.font(quickPencil.getFamily(), 20));
            htpText.setFill(javafx.scene.paint.Color.WHITE);
            htpText.setEffect(glow);
            htpText.setFill(javafx.scene.paint.Color.BLACK);
            htpTextFlow.getChildren().add(htpText);
            htpAnchorPane.getChildren().add(htpTextFlow);
            htpTextFlow.setLayoutX(10);
            htpTextFlow.setLayoutY(10);
            htpTextFlow.setPrefWidth(610);
        }

        // Rules content
        if (rulesAnchorPane != null) {
            TextFlow rulesTextFlow = new TextFlow();
            Text rulesText = new Text("Game Rules\n" +
                    "1. Basic Mechanics\n" +
                    "Each round features a hidden word of random length.\n" +
                    "\n" +
                    "Players compete simultaneously to guess the word first.\n" +
                    "\n" +
                    "2. Attempts & Guessing\n" +
                    "5 incorrect guesses allowed per round.\n" +
                    "\n" +
                    "Two ways to guess:\n" +
                    "\n" +
                    "Single letter: Reveals all instances of that letter.\n" +
                    "\n" +
                    "Full word: Instantly wins the round if correct.\n" +
                    "\n" +
                    "3. Round Flow\n" +
                    "30-second timer per round.\n" +
                    "\n" +
                    "No turns: All players guess at the same time.\n" +
                    "\n" +
                    "Instant win: First correct full-word guess ends the round.\n" +
                    "\n" +
                    "4. Winning Conditions\n" +
                    "Round winner: Fastest player to guess the word correctly.\n" +
                    "\n" +
                    "Game winner: First to win 3 rounds.\n" +
                    "\n" +
                    "Tiebreaker: Earliest submission if multiple correct guesses.\n" +
                    "\n" +
                    "5. Penalties & Restrictions\n" +
                    "Invalid inputs (numbers/symbols) don’t count as guesses.\n" +
                    "\n" +
                    "Repeated letters: No penalty, but no new information.\n" +
                    "\n" +
                    "6. Game End\n" +
                    "Match ends when a player reaches 3 round wins.\n" +
                    "\n" +
                    "All players return to the lobby automatically.\n" +
                    "\n" +
                    "Fair Play Guidelines\n" +
                    "No cheating: Collaborating with others ruins the fun!\n" +
                    "\n" +
                    "Respect the timer: Delays may forfeit your turn.");
            rulesText.setFont(Font.font(quickPencil.getFamily(), 20));
            rulesText.setFill(javafx.scene.paint.Color.WHITE);
            rulesText.setEffect(glow);
            rulesText.setFill(javafx.scene.paint.Color.BLACK);
            rulesTextFlow.getChildren().add(rulesText);
            rulesAnchorPane.getChildren().add(rulesTextFlow);
            rulesTextFlow.setLayoutX(10);
            rulesTextFlow.setLayoutY(10);
            rulesTextFlow.setPrefWidth(610);
            rulesTextFlow.setMinHeight(Region.USE_PREF_SIZE);
            rulesAnchorPane.setMinHeight(Region.USE_PREF_SIZE);
        }
    }

    @FXML
    private void showAboutPane() {
        aboutScrollPane.setVisible(true);
        htpScrollPane.setVisible(false);
        rulesScrollPane.setVisible(false);
        setButtonGraphic(aboutButton, ABOUT_SELECTED_IMAGE);
        setButtonGraphic(htpButton, HTP_IMAGE);
        setButtonGraphic(rulesButton, RULES_IMAGE);
    }

    @FXML
    private void showHtpPane() {
        aboutScrollPane.setVisible(false);
        htpScrollPane.setVisible(true);
        rulesScrollPane.setVisible(false);
        setButtonGraphic(aboutButton, ABOUT_IMAGE);
        setButtonGraphic(htpButton, HTP_SELECTED_IMAGE);
        setButtonGraphic(rulesButton, RULES_IMAGE);
    }

    @FXML
    private void showRulesPane() {
        aboutScrollPane.setVisible(false);
        htpScrollPane.setVisible(false);
        rulesScrollPane.setVisible(true);
        setButtonGraphic(aboutButton, ABOUT_IMAGE);
        setButtonGraphic(htpButton, HTP_IMAGE);
        setButtonGraphic(rulesButton, RULES_SELECTED_IMAGE);
    }

    @FXML
    private void handleReturn() {
        if (returnAction != null) {
            returnAction.run();
        } else {
            System.err.println("Return action not set - cannot navigate back to GameLobbyView");
        }
    }

    public void setReturnAction(Runnable action) {
        this.returnAction = action;
    }
}