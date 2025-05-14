package Client_Java.admin.controller;

import Client_Java.admin.AdminClient_Java;
import Client_Java.admin.model.AdminEditConfigurationsPageModel;
import AdminIDL.NotLoggedInException;
import Client_Java.admin.view.AdminEditConfigurationsPageView;
import javafx.animation.PauseTransition;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import javafx.event.ActionEvent;

public class AdminEditConfigurationsPageController {
    private final AdminEditConfigurationsPageView view;
    private final AdminEditConfigurationsPageModel model;

    public AdminEditConfigurationsPageController(AdminEditConfigurationsPageView view, AdminEditConfigurationsPageModel model) {
        this.view = view;
        this.model = model;
        attachEventHandlers();
        attachCurrentGameConfigurations();
        view.setActionCancelButton(event -> view.showAdminMainMenu(AdminClient_Java.getLoggidInAdmin()));
    }

    public void attachCurrentGameConfigurations() {
        try {
            TextField waitingTimeField = view.getWaitingTimeLabel();
            waitingTimeField.setText(String.valueOf(model.getWaitingTime()));
            TextField roundTimeField = view.getRoundLengthLabel();
            roundTimeField.setText(String.valueOf(model.getRoundDuration()));
        } catch (NotLoggedInException e) {
            view.getWaitingTimeNoticeLabel().setText("Error: You are not logged in.");
            view.getWaitingTimeNoticeLabel().setStyle("-fx-text-fill: red;");
            view.getWaitingTimeNoticeLabel().setVisible(true);
            view.getRoundLengthNoticeLabel().setText("Error: You are not logged in.");
            view.getRoundLengthNoticeLabel().setStyle("-fx-text-fill: red;");
            view.getRoundLengthNoticeLabel().setVisible(true);
        } catch (Exception e) {
            view.getWaitingTimeNoticeLabel().setText("Unexpected error occurred.");
            view.getWaitingTimeNoticeLabel().setStyle("-fx-text-fill: red;");
            view.getWaitingTimeNoticeLabel().setVisible(true);
            view.getRoundLengthNoticeLabel().setText("Unexpected error occurred.");
            view.getRoundLengthNoticeLabel().setStyle("-fx-text-fill: red;");
            view.getRoundLengthNoticeLabel().setVisible(true);
        }
    }

    private void attachEventHandlers() {
        view.setActionIncrementRLButton(e -> adjustRoundLength(1));
        view.setActionDecrementRLButton(e -> adjustRoundLength(-1));
        view.setActionIncrementWTButton(e -> adjustWaitingTime(1));
        view.setActionDecrementWTButton(e -> adjustWaitingTime(-1));
        view.setActionSaveButton(e -> handleSave());
    }

    private void adjustWaitingTime(int delta) {
        TextField waitingTimeField = view.getWaitingTimeLabel();
        Label waitingTimeNoticeLabel = view.getWaitingTimeNoticeLabel();
        try {
            int current = Integer.parseInt(waitingTimeField.getText());
            int newValue = current + delta;
            if (newValue >= 5 && newValue <= 60) {
                waitingTimeField.setText(String.valueOf(newValue));
                waitingTimeNoticeLabel.setVisible(false);
            } else {
                waitingTimeNoticeLabel.setText("Waiting time must be 5-60 seconds");
                waitingTimeNoticeLabel.setStyle("-fx-text-fill: red;");
                waitingTimeNoticeLabel.setVisible(true);
            }
        } catch (NumberFormatException e) {
            waitingTimeNoticeLabel.setText("Please enter a valid number");
            waitingTimeNoticeLabel.setStyle("-fx-text-fill: red;");
            waitingTimeNoticeLabel.setVisible(true);
        }
    }

    private void adjustRoundLength(int delta) {
        TextField roundLengthField = view.getRoundLengthLabel();
        Label roundLengthNoticeLabel = view.getRoundLengthNoticeLabel();
        try {
            int current = Integer.parseInt(roundLengthField.getText());
            int newValue = current + delta;
            if (newValue >= 10 && newValue <= 300) {
                roundLengthField.setText(String.valueOf(newValue));
                roundLengthNoticeLabel.setVisible(false);
            } else {
                roundLengthNoticeLabel.setText("Round duration must be 10-300 seconds");
                roundLengthNoticeLabel.setStyle("-fx-text-fill: red;");
                roundLengthNoticeLabel.setVisible(true);
            }
        } catch (NumberFormatException e) {
            roundLengthNoticeLabel.setText("Please enter a valid number");
            roundLengthNoticeLabel.setStyle("-fx-text-fill: red;");
            roundLengthNoticeLabel.setVisible(true);
        }
    }

    private void handleSave() {
        TextField roundLengthField = view.getRoundLengthLabel();
        TextField waitingTimeField = view.getWaitingTimeLabel();
        Label roundLengthNoticeLabel = view.getRoundLengthNoticeLabel();
        Label waitingTimeNoticeLabel = view.getWaitingTimeNoticeLabel();

        if (roundLengthField == null || waitingTimeField == null ||
                roundLengthNoticeLabel == null || waitingTimeNoticeLabel == null) {
            System.err.println("UI component is null: " +
                    "roundLengthField=" + roundLengthField +
                    ", waitingTimeField=" + waitingTimeField +
                    ", roundLengthNoticeLabel=" + roundLengthNoticeLabel +
                    ", waitingTimeNoticeLabel=" + waitingTimeNoticeLabel);
            if (roundLengthNoticeLabel != null) {
                roundLengthNoticeLabel.setText("Error: UI components not initialized.");
                roundLengthNoticeLabel.setStyle("-fx-text-fill: red;");
                roundLengthNoticeLabel.setVisible(true);
            }
            if (waitingTimeNoticeLabel != null) {
                waitingTimeNoticeLabel.setText("Error: UI components not initialized.");
                waitingTimeNoticeLabel.setStyle("-fx-text-fill: red;");
                waitingTimeNoticeLabel.setVisible(true);
            }
            return;
        }

        try {
            int roundTime = Integer.parseInt(roundLengthField.getText());
            int waitTime = Integer.parseInt(waitingTimeField.getText());

            if (waitTime < 5 || waitTime > 60) {
                waitingTimeNoticeLabel.setText("Waiting time must be 5-60 seconds");
                waitingTimeNoticeLabel.setStyle("-fx-text-fill: red;");
                waitingTimeNoticeLabel.setVisible(true);
                return;
            }

            if (roundTime < 10 || roundTime > 300) {
                roundLengthNoticeLabel.setText("Round duration must be 10-300 seconds");
                roundLengthNoticeLabel.setStyle("-fx-text-fill: red;");
                roundLengthNoticeLabel.setVisible(true);
                return;
            }

            // Show confirmation popup
            boolean confirmed = view.showConfirmationPopup("Save this new configuration?");
            if (!confirmed) {
                waitingTimeNoticeLabel.setText("Configuration save cancelled.");
                waitingTimeNoticeLabel.setStyle("-fx-text-fill: red;");
                waitingTimeNoticeLabel.setVisible(true);
                roundLengthNoticeLabel.setText("Configuration save cancelled.");
                roundLengthNoticeLabel.setStyle("-fx-text-fill: red;");
                roundLengthNoticeLabel.setVisible(true);
                return;
            }

            model.modifyWaitingTime(waitTime);
            model.modifyRoundDuration(roundTime);

            waitingTimeNoticeLabel.setText("Waiting time updated successfully.");
            waitingTimeNoticeLabel.setStyle("-fx-text-fill: green;");
            waitingTimeNoticeLabel.setVisible(true);

            roundLengthNoticeLabel.setText("Round duration updated successfully.");
            roundLengthNoticeLabel.setStyle("-fx-text-fill: green;");
            roundLengthNoticeLabel.setVisible(true);

            // Delay for 3 seconds before redirecting
            PauseTransition delay = new PauseTransition(Duration.seconds(2));
            delay.setOnFinished(event -> view.showAdminMainMenu(AdminClient_Java.getLoggidInAdmin()));
            delay.play();

        } catch (NumberFormatException e) {
            waitingTimeNoticeLabel.setText("Invalid input: must be numeric.");
            waitingTimeNoticeLabel.setStyle("-fx-text-fill: red;");
            waitingTimeNoticeLabel.setVisible(true);
            roundLengthNoticeLabel.setText("Invalid input: must be numeric.");
            roundLengthNoticeLabel.setStyle("-fx-text-fill: red;");
            roundLengthNoticeLabel.setVisible(true);
        } catch (NotLoggedInException e) {
            waitingTimeNoticeLabel.setText("Error: Not logged in.");
            waitingTimeNoticeLabel.setStyle("-fx-text-fill: red;");
            waitingTimeNoticeLabel.setVisible(true);
            roundLengthNoticeLabel.setText("Error: Not logged in.");
            roundLengthNoticeLabel.setStyle("-fx-text-fill: red;");
            roundLengthNoticeLabel.setVisible(true);
        } catch (Exception e) {
            waitingTimeNoticeLabel.setText("Error: " + e.getMessage());
            waitingTimeNoticeLabel.setStyle("-fx-text-fill: red;");
            waitingTimeNoticeLabel.setVisible(true);
            roundLengthNoticeLabel.setText("Error: " + e.getMessage());
            roundLengthNoticeLabel.setStyle("-fx-text-fill: red;");
            roundLengthNoticeLabel.setVisible(true);
        }
    }
}