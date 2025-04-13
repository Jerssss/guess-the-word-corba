package Client_Java.player.view;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.scene.control.Label;


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

    private Font amaticSC;
    private Font maryKate;
    private final String AMATICSC_FONT_PATH = "/Client_Java/player/res/css/fonts/AmaticSC-Bold.ttf";
    private final String MARYKATE_FONT_PATH = "/Client_Java/player/res/css/fonts/FontsFree-Net-Marykate-Regular.ttf";
    private final int FIELD_SIZE = 20;
    private final int BUTTON_FONT_SIZE = 20;
    private final int TITLE_SIZE = 140;
    private final int MISC_SIZE = 43;
    private final int PROMPT_SIZE = 18;


    public void initialize() {
        System.out.println("[DEBUG] Initializing Login View...");

        loadCustomFonts();
        applyFonts();

        if (continueButton == null) {
            System.err.println("[ERROR] continueButton is NULL! Check FXML.");
        }
        if (quitButton == null) {
            System.err.println("[ERROR] quitButton is NULL! Check FXML.");
        }
    }

    private void loadCustomFonts() {
        try {
            // Load fonts to Font objects
            amaticSC = Font.loadFont(getClass().getResourceAsStream(AMATICSC_FONT_PATH), 10);
            maryKate = Font.loadFont(getClass().getResourceAsStream(MARYKATE_FONT_PATH), 10);

            // Fallbacks when the loading should fail
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
        // Font application to fields and labels
        if (usernameField != null) {
            usernameField.setFont(Font.font(maryKate.getFamily(), FIELD_SIZE));
        }
        if (passwordField != null) {
            passwordField.setFont(Font.font(maryKate.getFamily(), FIELD_SIZE));
        }

        // Font application to buttons and titles
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
