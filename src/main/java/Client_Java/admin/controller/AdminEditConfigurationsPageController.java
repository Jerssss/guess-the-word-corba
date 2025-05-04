package Client_Java.admin.controller;

import Client_Java.admin.model.AdminEditConfigurationsPageModel;
import Client_Java.admin.view.AdminEditConfigurationsPageView;

import Server_Java.idls.AdminIDL.NotLoggedInException;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class AdminEditConfigurationsPageController {
    private final AdminEditConfigurationsPageView view;
    private final AdminEditConfigurationsPageModel model;

    public AdminEditConfigurationsPageController(AdminEditConfigurationsPageView view, AdminEditConfigurationsPageModel model) {
        this.view = view;
        this.model = model;
        attachEventHandlers();
    }

    private void attachEventHandlers() {
        view.setActionIncrementRLButton(e -> adjustValue(view.getRoundLengthLabel(), 1));
        view.setActionDecrementRLwButton(e -> adjustValue(view.getRoundLengthLabel(), -1));

        view.setActionIncrementWTButton(e -> adjustValue(view.getWaitingTimeLabel(), 1));
        view.setActionDecrementWTButton(e -> adjustValue(view.getWaitingTimeLabel(), -1));

        view.setActionSaveButton(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleSave();
            }
        });
    }

    private void adjustValue(javafx.scene.control.TextField textField, int delta) {
        try {
            long current = Long.parseLong(textField.getText());
            current += delta;
            if (current < 0) current = 0;
            textField.setText(String.valueOf(current));
        } catch (NumberFormatException e) {
            textField.setText("0");
        }
    }

    private void handleSave() {
        try {
            int roundTime = Integer.parseInt((view.getRoundLengthLabel().getText()));
            int waitTime = Integer.parseInt((view.getWaitingTimeLabel().getText()));

            model.modifyRoundDuration(roundTime);
            model.modifyWaitingTime(waitTime);

            view.getNoticeLabel().setText("Configuration updated successfully.");
        } catch (NumberFormatException e) {
            view.getNoticeLabel().setText("Invalid input: must be numeric.");
        } catch (NotLoggedInException e) {
            view.getNoticeLabel().setText("Error: Not logged in.");
        }
    }
}