package Client_Java.admin.view.modals;

import javafx.animation.ScaleTransition;
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

public class EditConfigConfirmationPopupView {

    @FXML
    private Text label;
    @FXML
    private Button yesButton;
    @FXML
    private Button noButton;
    @FXML
    private ImageView backgroundImageView;

    private boolean confirmed = false;
    private Font quickPencil;
    private final String QUICKPENCIL_FONT_PATH = "/css/fonts/QuickPencilRegular-0R59.ttf";

    @FXML
    private void initialize() {
        loadImage(backgroundImageView, "/images/testUI/admin/edit_tapestry_cropped.png");
        loadCustomFonts();
        applyFonts();
        setupButtonHoverEffects(); // Initialize hover animations

        yesButton.setOnAction(event -> handleYes());
        noButton.setOnAction(event -> handleNo());
    }

    private void setupButtonHoverEffects() {
        // Set up hover effects for yes button
        if (yesButton != null) {
            yesButton.setOnMouseEntered(e -> buttonHovered(yesButton));
            yesButton.setOnMouseExited(e -> buttonExited(yesButton));
        }

        // Set up hover effects for no button
        if (noButton != null) {
            noButton.setOnMouseEntered(e -> buttonHovered(noButton));
            noButton.setOnMouseExited(e -> buttonExited(noButton));
        }
    }

    private void buttonHovered(Button button) {
        ScaleTransition st = new ScaleTransition(Duration.millis(200), button);
        st.setToX(0.9);  // Scale down to 90% width
        st.setToY(0.9);  // Scale down to 90% height
        st.play();
    }

    private void buttonExited(Button button) {
        ScaleTransition st = new ScaleTransition(Duration.millis(200), button);
        st.setToX(1.0);  // Return to normal size
        st.setToY(1.0);  // Return to normal size
        st.play();
    }

    private void loadCustomFonts() {
        try {
            quickPencil = Font.loadFont(getClass().getResourceAsStream(QUICKPENCIL_FONT_PATH), 10);

            if (quickPencil == null) {
                System.err.println("QuickPencil font not loaded. Using system font.");
                quickPencil = Font.font("System", 12);
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Font loading exception: " + e.getMessage());
            quickPencil = Font.font("System", 12);
        }
    }

    private void applyFonts() {
        if (label != null) {
            label.setFont(Font.font(quickPencil.getFamily(), 36));
        }
        if (yesButton != null) {
            yesButton.setFont(Font.font(quickPencil.getFamily(), 36));
        }
        if (noButton != null) {
            noButton.setFont(Font.font(quickPencil.getFamily(), 36));
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

    public void setConfirmationMessage(String message) {
        label.setText(message);
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
}