package Client_Java.admin.view.modals;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

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

    // Font paths
    private final String QUICKPENCIL_FONT_PATH = "/css/fonts/QuickPencilRegular-0R59.ttf";

    // Font objects
    private Font quickPencil;

    /**
     * Called automatically after FXML loading.
     */
    @FXML
    private void initialize() {
        loadImage(backgroundImageView, "/images/testUI/admin/delete_slab_cropped.png");
        loadCustomFonts();
        applyFonts();

        yesButton.setOnAction(event -> handleYes());
        noButton.setOnAction(event -> handleNo());
    }

    /**
     * Loads custom fonts from resources.
     */
    private void loadCustomFonts() {
        try {
            quickPencil = Font.loadFont(getClass().getResourceAsStream(QUICKPENCIL_FONT_PATH), 10);


            // Fallback if fonts fail to load
            if (quickPencil == null) {
                System.err.println("Pencilant font not loaded. Using system font.");
                quickPencil = Font.font("System", 12);
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Font loading exception: " + e.getMessage());
            quickPencil = Font.font("System", 12);
        }
    }

    /**
     * Applies loaded fonts to UI elements.
     */
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

    /**
     * Loads an image into an ImageView.
     * @param imageView The target ImageView.
     * @param resourcePath The path to the image resource.
     */
    private void loadImage(ImageView imageView, String resourcePath) {
        try {
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is != null) {
                imageView.setImage(new Image(is));
                System.out.println("Loaded image: " + resourcePath);
            } else {
                // Fallback: Try loading from absolute path (for debugging)
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

    // --- Button Action Handlers ---
    public void setActionYesButton(EventHandler<ActionEvent> event) {
        yesButton.setOnAction(event);
    }

    public void setActionNoButton(EventHandler<ActionEvent> event) {
        noButton.setOnAction(event);
    }

    // --- Setters ---
    public void setPlayerToDeleteLabel(String playerName) {
        playerToDeleteLabel.setText(playerName);
    }
}