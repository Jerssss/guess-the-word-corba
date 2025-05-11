package Client_Java.admin.view.modals;

import Shared_Files.PlayerAccount;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.InputStream;

public class EditPlayerPopupView {
    @FXML
    private TextField usernameTextfield;
    @FXML
    private TextField passwordTextField;
    @FXML
    private TextField gamePointsTextField;
    @FXML
    private TextField fullNameTextField;
    @FXML
    private Text titleLabel;
    @FXML
    private Text usernameLabel;
    @FXML
    private Text passwordLabel;
    @FXML
    private Text fullNameLabel;
    @FXML
    private Text gameWinsLabel;
    @FXML
    private Label promptLabel;
    @FXML
    private GridPane gridPane;
    @FXML
    private ImageView backgroundImageView;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    private PlayerAccount player;

    private Font quickPencil;
    private final String QUICKPENCIL_FONT_PATH  = "/css/fonts/QuickPencilRegular-0R59.ttf";

    public void initialize(){
        loadImage(backgroundImageView, "/images/testUI/admin/create-scroll-cropped-2.png");
        loadCustomFonts();
        applyFonts();
    }

    public void setPlayer(PlayerAccount player) {
        this.player = player;
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
        // Font application to fields and labels
        if (titleLabel != null) {
            titleLabel.setFont(Font.font(quickPencil.getFamily(), 39));
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
        if (cancelButton != null) {
            cancelButton.setFont(Font.font(quickPencil.getFamily(), 28));
        }
        if (saveButton != null) {
            saveButton.setFont(Font.font(quickPencil.getFamily(), 28));
        }

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

    /** Buttons */
    public void setActionCancelButton(EventHandler<ActionEvent> event) {

    }

    public void setActionSaveButton(EventHandler<ActionEvent> event) {

    }

    /** Getters and Setters */
    public TextField getFullNameTextField() {
        return fullNameTextField;
    }

    public TextField getUsernameTextfield() {
        return usernameTextfield;
    }

    public TextField getPasswordTextField() {
        return passwordTextField;
    }

    public TextField getGamePointsTextField() {
        return gamePointsTextField;
    }

    public void setGameWinsLabel(Text gameWinsLabel) {
        this.gameWinsLabel = gameWinsLabel;
    }

    public void setPromptLabel(Label promptLabel) {
        this.promptLabel = promptLabel;
    }

}