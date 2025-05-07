package Client_Java.admin.controller;

import Client_Java.admin.model.AdminCreateAccountPageModel;
import Client_Java.admin.view.AdminCreateAccountPageView;
import AdminIDL.AccountExistsException;
import AdminIDL.NotLoggedInException;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class AdminCreateAccountPageController {

    private final AdminCreateAccountPageView view;
    private final AdminCreateAccountPageModel model;

    public AdminCreateAccountPageController(AdminCreateAccountPageView view, AdminCreateAccountPageModel model) {
        this.view = view;
        this.model = model;
        setupListeners();
    }

    private void setupListeners() {
        view.setActionSaveButton(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleCreatePlayer();
            }
        });

        view.setActionReturnButton(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // Placeholder: navigate back to admin menu, or switch scenes
                view.setNoticeLabelText("Returning...");
                view.setNoticeVisible(true);
            }
        });
    }

    private void handleCreatePlayer() {
        String username = view.getUsernameTextField().getText().trim();
        String password = view.getPasswordTextField().getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            view.setNoticeLabelText("Fields must not be empty.");
            view.setNoticeVisible(true);
            return;
        }

        try {
            model.createPlayer(username, password);
            view.setNoticeLabelText("Player account created successfully.");
            view.setNoticeVisible(true);
        } catch (AccountExistsException e) {
            view.setNoticeLabelText("Error: Account already exists.");
            view.setNoticeVisible(true);
        } catch (NotLoggedInException e) {
            view.setNoticeLabelText("Error: You are not logged in.");
            view.setNoticeVisible(true);
        } catch (Exception e) {
            view.setNoticeLabelText("Unexpected error occurred.");
            view.setNoticeVisible(true);
        }
    }
}