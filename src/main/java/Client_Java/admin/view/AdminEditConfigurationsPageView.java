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
    private Label roundLengthNoticeLabel;

    @FXML
    private Label waitingTimeNoticeLabel;

    @FXML
    private void initialize() {
        // Log initialization status for debugging
        System.out.println("AdminEditConfigurationsPageView initialized: " +
                "saveButton=" + saveButton +
                ", roundLengthLabel=" + roundLengthLabel +
                ", waitingTimeLabel=" + waitingTimeLabel +
                ", roundLengthNoticeLabel=" + roundLengthNoticeLabel +
                ", waitingTimeNoticeLabel=" + waitingTimeNoticeLabel);
    }

    // Round Length Buttons
    public void setActionDecrementRLwButton(EventHandler<ActionEvent> event) {
        if (decrementRLButton != null) {
            decrementRLButton.setOnAction(event);
        } else {
            System.err.println("decrementRLButton is null");
        }
    }

    public void setActionIncrementRLButton(EventHandler<ActionEvent> event) {
        if (incrementRLButton != null) {
            incrementRLButton.setOnAction(event);
        } else {
            System.err.println("incrementRLButton is null");
        }
    }

    // Waiting Time Buttons
    public void setActionDecrementWTButton(EventHandler<ActionEvent> event) {
        if (decrementWTButton != null) {
            decrementWTButton.setOnAction(event);
        } else {
            System.err.println("decrementWTButton is null");
        }
    }

    public void setActionIncrementWTButton(EventHandler<ActionEvent> event) {
        if (incrementWTButton != null) {
            incrementWTButton.setOnAction(event);
        } else {
            System.err.println("incrementWTButton is null");
        }
    }

    // Save Button
    public void setActionSaveButton(EventHandler<ActionEvent> event) {
        if (saveButton != null) {
            saveButton.setOnAction(e -> {
                System.out.println("Save button clicked in view");
                event.handle(e);
            });
        } else {
            System.err.println("saveButton is null");
        }
    }

    // Getters
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

    // Setters
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
}