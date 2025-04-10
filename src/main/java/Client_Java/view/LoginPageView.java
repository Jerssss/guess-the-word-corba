package Client_Java.view;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

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
        quitButton.setOnAction(e -> Platform.exit());
    }

    public LoginPageView() {
    }

    public String getUsernameFieldValue() {
        return usernameField.getText();
    }

    public String getPasswordFieldValue() {
        return passwordField.getText();
    }

//    public void initialize(){
//        messageText.setTex("");
//    }

//    public  void setMessageText(String message){
//        messageText.setText(message);
//    }

    public Button getContinueButton() {
        return continueButton;
    }
}
