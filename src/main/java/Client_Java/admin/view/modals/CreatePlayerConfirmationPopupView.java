package Client_Java.admin.view.modals;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.InputStream;

public class CreatePlayerConfirmationPopupView {

    @FXML
    private Text label;

    @FXML
    private Text playerToCreateLabel;

    @FXML
    private Button yesButton;

    @FXML
    private Button noButton;

    @FXML
    private ImageView backgroundImageView;

    @FXML
    private ImageView orb;

    @FXML
    private ImageView flame1;

    @FXML
    private ImageView flame2;

    private boolean confirmed = false;

    private Font quickPencil;
    private final String QUICKPENCIL_FONT_PATH  = "/css/fonts/QuickPencilRegular-0R59.ttf";

    @FXML
    private void initialize() {
        loadImage(backgroundImageView, "/images/testUI/admin/create-scroll-cropped.png");
        loadImage(orb, "/images/testUI/admin/energy-ball.gif");
        loadImage(flame1, "/images/testUI/fire-nobg.gif");
        loadImage(flame2, "/images/testUI/fire-nobg.gif");

        yesButton.setOnAction(event -> handleYes());
        noButton.setOnAction(event -> handleNo());

        loadCustomFonts();
        applyFonts();
    }

    private void loadCustomFonts() {
        try {
            quickPencil  = Font.loadFont(getClass().getResourceAsStream(QUICKPENCIL_FONT_PATH), 10);

            if (quickPencil == null) {
                System.err.println("AmaticSC font not loaded. Using system font.");
                quickPencil = Font.font("System", 12);
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Font loading exception: " + e.getMessage());
            quickPencil  = Font.font("System", 12);
        }
    }

    private void applyFonts() {
        if (label != null) {
            label.setFont(Font.font(quickPencil.getFamily(), 32));
        }
        if (playerToCreateLabel != null) {
            playerToCreateLabel.setFont(Font.font(quickPencil.getFamily(), 38));
        }
        if (yesButton != null) {
            yesButton.setFont(Font.font(quickPencil.getFamily(), 29));
        }
        if (noButton != null) {
            noButton.setFont(Font.font(quickPencil.getFamily(), 29));
        }
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

    private void handleYes() {
        confirmed = true;
        closeDialog();
    }

    private void handleNo() {
        confirmed = false;
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) yesButton.getScene().getWindow();
        stage.close();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmationMessage(String message) {
        label.setText(message);
    }

    public void setPlayerName(String playerName) {
        playerToCreateLabel.setText(playerName);
    }

}