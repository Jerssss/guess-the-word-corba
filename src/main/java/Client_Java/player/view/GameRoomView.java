package Client_Java.player.view;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;

import java.io.InputStream;

public class GameRoomView {
    @FXML private BorderPane rootPane;
    @FXML private Pane       gameContentPane;

    @FXML private Label      roundLabel;
    @FXML private Label      lifeCountLabel;
    @FXML private Label      timerLabel;           // ← new timer label

    @FXML private FlowPane   wordFlow;
    @FXML private FlowPane   alphabetFlow;

    @FXML private ImageView  catShadow;
    @FXML private ImageView  catTopHead;
    @FXML private ImageView  catEyes;
    @FXML private ImageView  catSnout;
    @FXML private ImageView  catLeftWhiskers;
    @FXML private ImageView  catRightWhiskers;
    @FXML private ImageView  catBottomHead;

    @FXML
    private ImageView gameRoomBackgroundImage;

    private Font amaticSC;
    private Font pencilant;
    private final String AMATICSC_FONT_PATH = "/css/fonts/AmaticSC-Bold.ttf";
    private final String PENCILANT_FONT_PATH = "/css/fonts/Pencilant Script.ttf";

    public void initialize() {

        loadImage(gameRoomBackgroundImage, "/images/testUI/game_room.png");

        loadImage(catShadow, "/images/cat/cat-outline.png");
        loadImage(catTopHead,"/images/cat/top-head.png");
        loadImage(catEyes,"/images/cat/eyes.png");
        loadImage(catLeftWhiskers,"/images/cat/left-whiskers.png");
        loadImage(catRightWhiskers,"/images/cat/right-whiskers.png");
        loadImage(catSnout,"/images/cat/snout.png");
        loadImage(catBottomHead,"images/cat/bottom-head.png");

        loadCustomFonts();
        applyFonts();

    }

    private void loadCustomFonts() {
        try {
            // Load fonts to Font objects
            amaticSC = Font.loadFont(getClass().getResourceAsStream(AMATICSC_FONT_PATH), 10);
            pencilant = Font.loadFont(getClass().getResourceAsStream(PENCILANT_FONT_PATH), 10);

            // Fallbacks when the loading should fail
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
            amaticSC = Font.font("System", 12);
            pencilant = Font.font("System", 12);
        }
    }

    private void applyFonts() {
        // Font application to fields and labels
//        if (waitingRoomLabel != null) {
//            waitingRoomLabel.setFont(Font.font(maryKate.getFamily(), 72));
//        }
//        if (playerCountLabel != null) {
//            playerCountLabel.setFont(Font.font(maryKate.getFamily(), 48));
//        }
//
//        // Font application to buttons and titles
//        if (cancelButton != null) {
//            cancelButton.setFont(Font.font(amaticSC.getFamily(), 31));
//        }
//
//        if (countdownLabel != null) {
//            countdownLabel.setFont(Font.font(amaticSC.getFamily(), 180));
//        }
//        if (playersLabel != null) {
//            playersLabel.setFont(Font.font(maryKate.getFamily(), 50));
//        }
//        if (secondsLabel != null) {
//            secondsLabel.setFont(Font.font(amaticSC.getFamily(), 46));
//        }

    }

    private void loadImage(ImageView imageView, String resourcePath) {
        try {
            // Try from resources first
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is != null) {
                imageView.setImage(new Image(is));
                System.out.println("Loaded image: " + resourcePath);
            } else {
                // backup
                String absPath = "file:src/main/resources" + resourcePath;
                imageView.setImage(new Image(absPath));
            }
        } catch (Exception e) {
            System.err.println("Failed to load image: " + resourcePath);
            e.printStackTrace();
        }
    }

    // getters for Controller
    public BorderPane getRootPane()        { return rootPane;       }
    public Pane       getGameContentPane() { return gameContentPane;}
    public Label      getRoundLabel()      { return roundLabel;     }
    public Label      getLifeCountLabel()  { return lifeCountLabel; }
    public Label      getTimerLabel()      { return timerLabel;     } // ← expose it

    public FlowPane   getWordFlow()        { return wordFlow;       }
    public FlowPane   getAlphabetFlow()    { return alphabetFlow;   }

    public ImageView getCatShadow()        { return catShadow;       }
    public ImageView getCatTopHead()       { return catTopHead;      }
    public ImageView getCatEyes()          { return catEyes;         }
    public ImageView getCatSnout()         { return catSnout;        }
    public ImageView getCatLeftWhiskers()  { return catLeftWhiskers; }
    public ImageView getCatRightWhiskers() { return catRightWhiskers;}
    public ImageView getCatBottomHead()    { return catBottomHead;   }
}
