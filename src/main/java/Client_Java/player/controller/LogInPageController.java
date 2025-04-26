package Client_Java.player.controller;

import Client_Java.PlayerClient_Java;
import Client_Java.player.model.LogInPageModel;
import Client_Java.player.view.LoginPageView;
import javafx.application.Platform;

public class LogInPageController {
    private final LogInPageModel model;
    private final LoginPageView view;

    public LogInPageController(LogInPageModel model, LoginPageView view) {
        this.model = model;
        this.view = view;

        this.view.setActionContinueButton(event -> onLoginPressed());
        this.view.setActionQuitButton(event -> Platform.exit());
    }

    private void onLoginPressed() {
        String username = view.getUsernameField().getText();
        String password = view.getPasswordField().getText();

        boolean success = model.login(username, password);

        if (success) {
            view.setPromptLabel("Login Successful!");
            view.setPromptLabelVisible(true);

            System.out.println("[Client] Logged In: " + PlayerClient_Java.getSessionToken());

            // TODO: move to next scene (home screen) here
        } else {
            view.setPromptLabel("Login Failed. Try again!");
            view.setPromptLabelVisible(true);
        }
    }
}
