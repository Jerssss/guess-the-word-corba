package Client_Java.admin.view.modals;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class CreatePlayerConfirmationPopupView {

    @FXML
    private Text label;

    @FXML
    private Text playerToCreateLabel;

    @FXML
    private Button yesButton;

    @FXML
    private Button noButton;

    private boolean confirmed = false;

    public void setConfirmationMessage(String message) {
        label.setText(message);
    }

    public void setPlayerName(String playerName) {
        playerToCreateLabel.setText(playerName);
    }

    @FXML
    private void initialize() {
        yesButton.setOnAction(event -> handleYes());
        noButton.setOnAction(event -> handleNo());
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