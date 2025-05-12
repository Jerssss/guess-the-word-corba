package Client_Java.player.view;

import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.io.InputStream;

public class WaitingRoomView {

    @FXML
    private Button cancelButton;
    @FXML
    private Label countdownLabel;
    @FXML
    private Label secondsLabel;
    @FXML
    private Label playerCountLabel;
    @FXML
    private Label playersLabel;
    @FXML
    private Label waitingRoomLabel;
    @FXML
    private ImageView waitingRoomBackgroundView;
    @FXML
    private ImageView flame1, flame2, flame3, flame4;

    private Font amaticSC;
    private Font maryKate;
    private final String AMATICSC_FONT_PATH = "/css/fonts/AmaticSC-Bold.ttf";
    private final String MARYKATE_FONT_PATH = "/css/fonts/FontsFree-Net-Marykate-Regular.ttf";

    public void initialize() {
        loadImage(waitingRoomBackgroundView, "/images/testUI/waiting_room.png");
        loadImage(flame1, "/images/testUI/fire-nobg.gif");
        loadImage(flame2, "/images/testUI/fire-nobg.gif");
        loadImage(flame3, "/images/testUI/fire-nobg.gif");
        loadImage(flame4, "/images/testUI/fire-nobg.gif");

        loadCustomFonts();
        applyFonts();
        setupCancelButtonHover(); // Add hover effect for cancel button
    }

    private void setupCancelButtonHover() {
        if (cancelButton != null) {
            cancelButton.setOnMouseEntered(e -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(200), cancelButton);
                st.setToX(0.9);
                st.setToY(0.9);
                st.play();
            });

            cancelButton.setOnMouseExited(e -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(200), cancelButton);
                st.setToX(1.0);
                st.setToY(1.0);
                st.play();
            });
        }
    }

    private void applyFonts() {
        if (waitingRoomLabel != null) {
            waitingRoomLabel.setFont(Font.font(maryKate.getFamily(), 72));
        }
        if (playerCountLabel != null) {
            playerCountLabel.setFont(Font.font(maryKate.getFamily(), 48));
        }
        if (cancelButton != null) {
            cancelButton.setFont(Font.font(amaticSC.getFamily(), 31));
        }
        if (countdownLabel != null) {
            countdownLabel.setFont(Font.font(amaticSC.getFamily(), 180));
        }
        if (playersLabel != null) {
            playersLabel.setFont(Font.font(maryKate.getFamily(), 50));
        }
        if (secondsLabel != null) {
            secondsLabel.setFont(Font.font(amaticSC.getFamily(), 46));
        }
    }

    private void loadCustomFonts() {
        try {
            amaticSC = Font.loadFont(getClass().getResourceAsStream(AMATICSC_FONT_PATH), 10);
            maryKate = Font.loadFont(getClass().getResourceAsStream(MARYKATE_FONT_PATH), 10);

            if (amaticSC == null) {
                System.err.println("AmaticSC font not loaded. Using system font.");
                amaticSC = Font.font("System", 12);
            }
            if (maryKate == null) {
                System.err.println("MaryKate font not loaded. Using system font.");
                maryKate = Font.font("System", 12);
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Font loading exception: " + e.getMessage());
            amaticSC = Font.font("System", 12);
            maryKate = Font.font("System", 12);
        }
    }

    private void loadImage(ImageView imageView, String resourcePath) {
        try {
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is != null) {
                imageView.setImage(new Image(is));
                System.out.println("Loaded image: " + resourcePath);
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
    public void setActionCancelButton(EventHandler<ActionEvent> event) {
        cancelButton.setOnAction(event);
    }

    public void setRemainingTime(int remainingTime) {
        countdownLabel.setText(String.valueOf(remainingTime));
    }

    public void setWaitingPlayersCount(int playerCount) {
        playerCountLabel.setText(String.valueOf(playerCount));
    }

    public void setSecondsLabel(Label secondsLabel) {
        this.secondsLabel = secondsLabel;
    }
}