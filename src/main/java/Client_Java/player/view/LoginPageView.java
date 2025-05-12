package Client_Java.player.view;

import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.io.InputStream;

public class LoginPageView {

    @FXML
    private Label loginLabel;
    @FXML
    private Label title1Label;
    @FXML
    private Label title2Label;
    @FXML
    private Label promptLabel;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button quitButton;
    @FXML
    private Button continueButton;
    @FXML
    private ImageView backgroundImageView;

    private Font amaticSC;
    private Font maryKate;
    private final String AMATICSC_FONT_PATH = "/css/fonts/AmaticSC-Bold.ttf";
    private final String MARYKATE_FONT_PATH = "/css/fonts/bryndan-write.ttf";
    private final int FIELD_SIZE = 20;
    private final int BUTTON_FONT_SIZE = 20;
    private final int TITLE_SIZE = 140;
    private final int MISC_SIZE = 43;
    private final int PROMPT_SIZE = 18;

    public void initialize() {
        loadImage(backgroundImageView, "/images/testUI/shack-with-sign.png");
        loadCustomFonts();
        applyFonts();
        setupButtonHoverEffects(); // Initialize button hover animations

        if (continueButton == null) {
            System.err.println("[ERROR] continueButton is NULL! Check FXML.");
        }
        if (quitButton == null) {
            System.err.println("[ERROR] quitButton is NULL! Check FXML.");
        }
    }

    private void setupButtonHoverEffects() {
        // Set up hover effects for continue button
        if (continueButton != null) {
            continueButton.setOnMouseEntered(e -> buttonHovered(continueButton));
            continueButton.setOnMouseExited(e -> buttonExited(continueButton));
        }

        // Set up hover effects for quit button
        if (quitButton != null) {
            quitButton.setOnMouseEntered(e -> buttonHovered(quitButton));
            quitButton.setOnMouseExited(e -> buttonExited(quitButton));
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

    private void applyFonts() {
        if (usernameField != null) {
            usernameField.setFont(Font.font(maryKate.getFamily(), FIELD_SIZE));
        }
        if (passwordField != null) {
            passwordField.setFont(Font.font(maryKate.getFamily(), FIELD_SIZE));
        }
        if (continueButton != null) {
            continueButton.setFont(Font.font(amaticSC.getFamily(), BUTTON_FONT_SIZE));
        }
        if (quitButton != null) {
            quitButton.setFont(Font.font(amaticSC.getFamily(), BUTTON_FONT_SIZE));
        }
        if (title1Label != null) {
            title1Label.setFont(Font.font(maryKate.getFamily(), TITLE_SIZE));
        }
        if (title2Label != null) {
            title2Label.setFont(Font.font(maryKate.getFamily(), TITLE_SIZE));
        }
        if (loginLabel != null) {
            loginLabel.setFont(Font.font(amaticSC.getFamily(), MISC_SIZE));
        }
        if (promptLabel != null) {
            promptLabel.setFont(Font.font(maryKate.getFamily(), PROMPT_SIZE));
        }
    }

    public TextField getUsernameField() {
        return usernameField;
    }

    public void setUsernameField(TextField usernameField) {
        this.usernameField = usernameField;
    }

    public PasswordField getPasswordField() {
        return passwordField;
    }

    public void setPasswordField(PasswordField passwordField) {
        this.passwordField = passwordField;
    }

    public Label getPromptLabel() {
        return promptLabel;
    }

    public void setPromptLabel(String text) {
        promptLabel.setText(text);
    }

    public void setPromptLabelVisible(boolean visible) {
        promptLabel.setVisible(visible);
    }

    public void setActionContinueButton(EventHandler<ActionEvent> event) {
        continueButton.setOnAction(event);
    }

    public void setActionQuitButton(EventHandler<ActionEvent> event) {
        quitButton.setOnAction(event);
    }
}