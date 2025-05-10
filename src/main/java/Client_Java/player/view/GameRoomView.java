// File: Client_Java/player/view/GameRoomView.java
package Client_Java.player.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;

import java.io.InputStream;

public class GameRoomView {
    @FXML private BorderPane rootPane;
    @FXML private Pane       gameContentPane;

    @FXML private Label      roundLabel;
    @FXML private Label      lifeCountLabel;
    @FXML private Label      timerLabel;

    @FXML private Button quitButton;

    @FXML private FlowPane   wordFlow;
    @FXML private FlowPane   alphabetFlow;

    @FXML private ImageView  catShadow;
    @FXML private ImageView  catTopHead;
    @FXML private ImageView  catEyes;
    @FXML private ImageView  catSnout;
    @FXML private ImageView  catLeftWhiskers;
    @FXML private ImageView  catRightWhiskers;
    @FXML private ImageView  catBottomHead;

    @FXML private ImageView  gameRoomBackgroundImage;

    private Font amaticSC;
    private Font pencilant;
    private final String AMATICSC_FONT_PATH  = "/css/fonts/AmaticSC-Bold.ttf";
    private final String PENCILANT_FONT_PATH = "/css/fonts/Pencilant Script.ttf";

    @FXML
    private void initialize() {
        loadImage(gameRoomBackgroundImage, "/images/testUI/game_room.png");

        loadImage(catShadow, "/images/cat/cat-outline.png");
        loadImage(catTopHead, "/images/cat/top-head.png");
        loadImage(catEyes, "/images/cat/eyes.png");
        loadImage(catLeftWhiskers, "/images/cat/left-whiskers.png");
        loadImage(catRightWhiskers, "/images/cat/right-whiskers.png");
        loadImage(catSnout, "/images/cat/snout.png");
        loadImage(catBottomHead, "/images/cat/bottom-head.png");

//        loadImage()

        loadCustomFonts();
        applyFonts();
    }

    private void loadCustomFonts() {
        try {
            amaticSC  = Font.loadFont(getClass().getResourceAsStream(AMATICSC_FONT_PATH), 10);
            pencilant = Font.loadFont(getClass().getResourceAsStream(PENCILANT_FONT_PATH), 10);

            if (amaticSC == null) {
                System.err.println("AmaticSC font not loaded. Using system font.");
                amaticSC = Font.font("System", 12);
            }
            if (pencilant == null) {
                System.err.println("Pencilant font not loaded. Using system font.");
                pencilant = Font.font("System", 12);
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Font loading exception: " + e.getMessage());
            amaticSC  = Font.font("System", 12);
            pencilant = Font.font("System", 12);
        }
    }

    private void applyFonts() {
        // Apply fonts as needed to labels/buttons when desired
    }

    private void loadImage(ImageView imageView, String resourcePath) {
        try {
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is != null) {
                imageView.setImage(new Image(is));
            } else {
                String absPath = "file:src/main/resources" + resourcePath;
                imageView.setImage(new Image(absPath));
            }
        } catch (Exception e) {
            System.err.println("Failed to load image: " + resourcePath);
            e.printStackTrace();
        }
    }

    @FXML
    public void handleQuitButton(ActionEvent event) {
        // wired by controller
    }

    // Getters for Controller
    public BorderPane getRootPane()        { return rootPane;       }
    public Pane       getGameContentPane() { return gameContentPane;}
    public Label      getRoundLabel()      { return roundLabel;     }
    public Label      getLifeCountLabel()  { return lifeCountLabel; }
    public Label      getTimerLabel()      { return timerLabel;     }
    public Button     getQuitButton()      { return quitButton;     }
    public FlowPane   getWordFlow()        { return wordFlow;       }
    public FlowPane   getAlphabetFlow()    { return alphabetFlow;   }
    public ImageView  getCatShadow()       { return catShadow;      }
    public ImageView  getCatTopHead()      { return catTopHead;     }
    public ImageView  getCatEyes()         { return catEyes;        }
    public ImageView  getCatSnout()        { return catSnout;       }
    public ImageView  getCatLeftWhiskers(){ return catLeftWhiskers;}
    public ImageView  getCatRightWhiskers(){ return catRightWhiskers;}
    public ImageView  getCatBottomHead()   { return catBottomHead;  }

    // Convenience methods for enabling/disabling/resetting alphabet
    public void disableAlphabetButtons() {
        alphabetFlow.getChildren().forEach(n -> n.setDisable(true));
    }

    public void enableAlphabetButtons() {
        alphabetFlow.getChildren().forEach(n -> n.setDisable(false));
    }

    public void resetAlphabetButtons() {
        alphabetFlow.getChildren().forEach(n -> {
            n.setDisable(false);
            // no-op text reset to refresh style if needed
        });
    }

    public void disableLetterButton(char letter) {
        alphabetFlow.getChildren().stream()
                .map(n -> (Button)n)
                .filter(b -> b.getText().charAt(0) == letter)
                .findFirst()
                .ifPresent(b -> b.setDisable(true));
    }
}
