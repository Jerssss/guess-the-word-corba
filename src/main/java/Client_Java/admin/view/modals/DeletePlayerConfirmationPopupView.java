package Client_Java.admin.view.modals;

import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.InputStream;

public class DeletePlayerConfirmationPopupView {

    @FXML
    private Text label;
    @FXML
    private Button noButton;
    @FXML
    private Text playerToDeleteLabel;
    @FXML
    private Button yesButton;
    @FXML
    private ImageView backgroundImageView;

    private boolean confirmed = false;
    private final String QUICKPENCIL_FONT_PATH = "/css/fonts/QuickPencilRegular-0R59.ttf";
    private Font quickPencil;

    @FXML
    private void initialize() {
        loadImage(backgroundImageView, "/images/testUI/admin/delete_slab_cropped.png");
        loadCustomFonts();
        applyFonts();
        setupButtonHoverEffects(); // Add hover effects setup

        yesButton.setOnAction(event -> handleYes());
        noButton.setOnAction(event -> handleNo());
    }

    private void setupButtonHoverEffects() {
        if (yesButton != null) {
            yesButton.setOnMouseEntered(e -> buttonHovered(yesButton));
            yesButton.setOnMouseExited(e -> buttonExited(yesButton));
        }
        if (noButton != null) {
            noButton.setOnMouseEntered(e -> buttonHovered(noButton));
            noButton.setOnMouseExited(e -> buttonExited(noButton));
        }
    }

    private void buttonHovered(Button button) {
        ScaleTransition st = new ScaleTransition(Duration.millis(200), button);
        st.setToX(0.9);
        st.setToY(0.9);
        st.play();
    }

    private void buttonExited(Button button) {
        ScaleTransition st = new ScaleTransition(Duration.millis(200), button);
        st.setToX(1.0);
        st.setToY(1.0);
        st.play();
    }

    private void loadCustomFonts() {
        try {
            quickPencil = Font.loadFont(getClass().getResourceAsStream(QUICKPENCIL_FONT_PATH), 10);

            if (quickPencil == null) {
                System.err.println("Pencilant font not loaded. Using system font.");
                quickPencil = Font.font("System", 12);
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Font loading exception: " + e.getMessage());
            quickPencil = Font.font("System", 12);
        }
    }

    private void applyFonts() {
        if (label != null) {
            label.setFont(Font.font(quickPencil.getFamily(), 28));
        }
        if (playerToDeleteLabel != null) {
            playerToDeleteLabel.setFont(Font.font(quickPencil.getFamily(), 29));
        }
        if (yesButton != null) {
            yesButton.setFont(Font.font(quickPencil.getFamily(), 38));
        }
        if (noButton != null) {
            noButton.setFont(Font.font(quickPencil.getFamily(), 38));
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

    public void setPlayerName(String playerName) {
        playerToDeleteLabel.setText(playerName);
    }

    public void setActionYesButton(EventHandler<ActionEvent> event) {
        yesButton.setOnAction(event);
    }

    public void setActionNoButton(EventHandler<ActionEvent> event) {
        noButton.setOnAction(event);
    }

    public void setPlayerToDeleteLabel(String playerName) {
        playerToDeleteLabel.setText(playerName);
    }
}