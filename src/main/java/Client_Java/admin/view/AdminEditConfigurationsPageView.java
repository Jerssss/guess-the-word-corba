package Client_Java.admin.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class AdminEditConfigurationsPageView {

    @FXML
    private Button decrementRLButton;

    @FXML
    private Button decrementWTButton;

    @FXML
    private Button incrementRLButton;

    @FXML
    private Button incrementWTButton;

    @FXML
    private Button saveButton;

    @FXML
    private TextField roundLengthLabel;

    @FXML
    private TextField waitingTimeLabel;

    @FXML
    private Label noticeLabel;

    @FXML
    private Label roundLengthNoticeLabel;

    @FXML
    private Label waitingTimeNoticeLabel;

    /** Round Length Buttons */
    public void setActionDecrementRLwButton (EventHandler <ActionEvent> event) {

    }

    public void setActionIncrementRLButton (EventHandler <ActionEvent> event) {

    }

    /** Waiting Time Buttons */
    public void setActionDecrementWTButton (EventHandler <ActionEvent> event) {

    }

    public void setActionIncrementWTButton (EventHandler <ActionEvent> event) {

    }

    /** Save Button */
    public void setActionSaveButton (EventHandler<ActionEvent> event) {

    }

    /** Getters and Setters */
    public Label getNoticeLabel() {
        return noticeLabel;
    }

    public Label getRoundLengthNoticeLabel() {
        return roundLengthNoticeLabel;
    }

    public TextField getRoundLengthLabel() {
        return roundLengthLabel;
    }

    public Label getWaitingTimeNoticeLabel() {
        return waitingTimeNoticeLabel;
    }

    public TextField getWaitingTimeLabel() {
        return waitingTimeLabel;
    }

    public void setRoundLengthLabel(TextField roundLengthLabel) {
        this.roundLengthLabel = roundLengthLabel;
    }

    public void setRoundLengthNoticeLabel(Label roundLengthNoticeLabel) {
        this.roundLengthNoticeLabel = roundLengthNoticeLabel;
    }

    public void setWaitingTimeLabel(TextField waitingTimeLabel) {
        this.waitingTimeLabel = waitingTimeLabel;
    }

    public void setWaitingTimeNoticeLabel(Label waitingTimeNoticeLabel) {
        this.waitingTimeNoticeLabel = waitingTimeNoticeLabel;
    }

    public void setNoticeLabel(Label noticeLabel) {
        this.noticeLabel = noticeLabel;
    }
}
