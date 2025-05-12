package Client_Java.admin.view.modals;

import Shared_Files.PlayerAccount;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.function.Consumer;

public class EditPlayerPopupView {

    @FXML private TextField userIdTextField;
    @FXML private TextField usernameTextField;
    @FXML private TextField passwordTextField;
    @FXML private TextField fullNameTextField;
    @FXML private TextField gamePointsTextField;
    @FXML private Text titleLabel;
    @FXML private Text userIdLabel;
    @FXML private Text usernameLabel;
    @FXML private Text passwordLabel;
    @FXML private Text fullNameLabel;
    @FXML private Text gameWinsLabel;
    @FXML private Label promptLabel;
    @FXML private GridPane gridPane;
    @FXML private ImageView backgroundImageView;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private PlayerAccount player;
    private Consumer<PlayerAccount> onSaveCallback;
    private Font quickPencil;
    private final String QUICKPENCIL_FONT_PATH = "/css/fonts/QuickPencilRegular-0R59.ttf";

    public void initialize() {
        loadImage(backgroundImageView, "/images/testUI/admin/create-scroll-cropped-2.png");
        loadAndApplyFonts();
        setupButtonHoverEffects(); // Initialize hover animations
        saveButton.setOnAction(event -> savePlayer());
        cancelButton.setOnAction(event -> closeWindow());
    }

    private void setupButtonHoverEffects() {
        // Set up hover effects for save button
        if (saveButton != null) {
            saveButton.setOnMouseEntered(e -> buttonHovered(saveButton));
            saveButton.setOnMouseExited(e -> buttonExited(saveButton));
        }

        // Set up hover effects for cancel button
        if (cancelButton != null) {
            cancelButton.setOnMouseEntered(e -> buttonHovered(cancelButton));
            cancelButton.setOnMouseExited(e -> buttonExited(cancelButton));
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

    public void setPlayer(PlayerAccount player) {
        this.player = player;
        initializePlayerInfo();
    }

    public void setOnSaveCallback(Consumer<PlayerAccount> callback) {
        this.onSaveCallback = callback;
    }

    private void initializePlayerInfo() {
        if (player == null) {
            System.out.println("Player is null, skipping initialization");
            return;
        }
        if (userIdTextField != null) {
            userIdTextField.setText(String.valueOf(player.getPlayerId()));
        }
        if (usernameTextField != null) {
            usernameTextField.setText(player.getUsername() != null ? player.getUsername() : "");
        }
        if (passwordTextField != null) {
            passwordTextField.setText(player.getPassword() != null ? player.getPassword() : "");
        }
        if (fullNameTextField != null) {
            fullNameTextField.setText(player.getName() != null ? player.getName() : "");
        }
        if (gamePointsTextField != null) {
            gamePointsTextField.setText(String.valueOf(player.getGameWins()));
        }
    }

    private void savePlayer() {
        if (player != null) {
            player.setPassword(passwordTextField.getText());
            if (onSaveCallback != null) {
                onSaveCallback.accept(player);
            }
        }
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void loadAndApplyFonts() {
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

        if (titleLabel != null) {
            titleLabel.setFont(Font.font(quickPencil.getFamily(), 39));
        }
        if (userIdLabel != null) {
            userIdLabel.setFont(Font.font(quickPencil.getFamily(), 39));
        }
        if (usernameLabel != null) {
            usernameLabel.setFont(Font.font(quickPencil.getFamily(), 32));
        }
        if (passwordLabel != null) {
            passwordLabel.setFont(Font.font(quickPencil.getFamily(), 32));
        }
        if (fullNameLabel != null) {
            fullNameLabel.setFont(Font.font(quickPencil.getFamily(), 32));
        }
        if (gameWinsLabel != null) {
            gameWinsLabel.setFont(Font.font(quickPencil.getFamily(), 32));
        }
        if (userIdTextField != null) {
            userIdTextField.setFont(Font.font(quickPencil.getFamily(), 32));
        }
        if (usernameTextField != null) {
            usernameTextField.setFont(Font.font(quickPencil.getFamily(), 32));
        }
        if (passwordTextField != null) {
            passwordTextField.setFont(Font.font(quickPencil.getFamily(), 32));
        }
        if (cancelButton != null) {
            cancelButton.setFont(Font.font(quickPencil.getFamily(), 28));
        }
        if (saveButton != null) {
            saveButton.setFont(Font.font(quickPencil.getFamily(), 28));
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
}