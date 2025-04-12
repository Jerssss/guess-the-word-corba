package Client_Java.admin.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.beans.EventHandler;

public class AdminCreateAccountPageView {

    @FXML
    private TextField fullnameTextField;

    @FXML
    private Label noticeLabel;

    @FXML
    private TextField passwordTextField;

    @FXML
    private Button returnButton;

    @FXML
    private Button saveButton;

    @FXML
    private TextField usernameTextField;

//    setActionReturnButton (EventHandler <ActionEvent> event) {
//
//    }

//    setActionSaveButton (EventHandler <ActionEvent> event) {
//
//    }

    public Label getNoticeLabel() {
        return noticeLabel;
    }

    public TextField getFullnameTextField() {
        return fullnameTextField;
    }

    public TextField getPasswordTextField() {
        return passwordTextField;
    }

    public TextField getUsernameTextField() {
        return usernameTextField;
    }

    public void setNoticeLabel(Label noticeLabel) {
        this.noticeLabel = noticeLabel;
    }
}