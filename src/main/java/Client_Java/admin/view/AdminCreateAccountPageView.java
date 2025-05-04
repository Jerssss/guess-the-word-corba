package Client_Java.admin.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

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


    public void setActionReturnButton(EventHandler<ActionEvent> event) {
        returnButton.setOnAction(event);
    }

    public void setActionSaveButton(EventHandler<ActionEvent> event) {
        saveButton.setOnAction(event);
    }

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

    public void setNoticeLabelText(String message) {
        noticeLabel.setText(message);
    }

    public void setNoticeVisible(boolean visible) {
        noticeLabel.setVisible(visible);
    }
}
