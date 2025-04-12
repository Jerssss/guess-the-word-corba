package Client_Java.player.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class WaitingRoomSectionView {

    @FXML
    private Button cancelButton;

    @FXML
    private Label countdownLabel;

    @FXML
    private Label secondsLabel;

    @FXML
    private Label playerCountLabel;

    @FXML
    public void handleCancelButtonAction() {
        // Handle the cancel button action
    }

    public void setCountdownLabel(Label countdownLabel) {
        this.countdownLabel = countdownLabel;
    }

    public void setPlayerCountLabel(Label playerCountLabel) {
        this.playerCountLabel = playerCountLabel;
    }

    public void setSecondsLabel(Label secondsLabel) {
        this.secondsLabel = secondsLabel;
    }
}