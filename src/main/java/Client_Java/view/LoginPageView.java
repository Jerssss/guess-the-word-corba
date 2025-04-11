package Client_Java.view;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.awt.*;

public class LoginPageView {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button quitButton;

    @FXML
    private Button continueButton;

    public void initialize() {
        System.out.println("[DEBUG] Initializing Login View...");

        if (continueButton == null) {
            System.err.println("[ERROR] continueButton is NULL! Check FXML.");
        }
        if (quitButton == null) {
            System.err.println("[ERROR] quitButton is NULL! Check FXML.");
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

//    public Label getPromptLabel() {
//       return promptLabel;
//    }
//
//    public void setPromptLabel(String text) {
//        promptLabel.setText(text);
//    }
//
//    public void setPromptLabelVisible(boolean visible) {
//        promptLabel.setVisible(visible);
//    }

    public void setActionContinueButton(EventHandler<ActionEvent> event) {
        continueButton.setOnAction(event);
    }

    public void setActionQuitButton(EventHandler<ActionEvent> event) {
        quitButton.setOnAction(event);
    }
}
