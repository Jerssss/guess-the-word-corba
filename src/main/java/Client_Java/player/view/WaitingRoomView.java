package Client_Java.player.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class WaitingRoomView {

    @FXML
    private Button cancelButton;

    @FXML
    private Label countdownLabel;

    @FXML
    private Label secondsLabel;

    @FXML
    private Label playerCountLabel;

    @FXML
    public void setActionCancelButton(EventHandler<ActionEvent> event) {
        cancelButton.setOnAction(event);
    }

    public void setRemainingTime(int remainingTime) {
        countdownLabel.setText(String.valueOf(remainingTime));
    }

    public void setWaitingPlayersCount(int playerCount) {
        playerCountLabel.setText(String.valueOf(playerCount));
    }

    public void setSecondsLabel(Label secondsLabel) {
        this.secondsLabel = secondsLabel;
    }
}