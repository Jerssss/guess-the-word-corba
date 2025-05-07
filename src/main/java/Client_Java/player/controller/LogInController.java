// File: Client_Java/player/controller/LogInController.java
package Client_Java.player.controller;

import Client_Java.player.PlayerClient_Java;
import Client_Java.player.SessionManager;
import Client_Java.player.model.LogInPageModel;
import Client_Java.player.model.LoginResult;
import Client_Java.player.view.LoginPageView;
import Client_Java.player.view.ViewNavigator;
import Client_Java.player.implementation.LoginCallBackServiceImpl;
import PlayerCallBackIDL.LoginCallbackService;
import PlayerCallBackIDL.LoginCallbackServiceHelper;
import javafx.application.Platform;
import javafx.event.ActionEvent;

public class LogInController {
    private final LogInPageModel model;
    private final LoginPageView  view;

    public LogInController(
            LogInPageModel model,
            LoginPageView view
    ) {
        this.model = model;
        this.view  = view;

        // bind view actions
        view.setActionContinueButton(this::onContinue);
        view.setActionQuitButton(e -> Platform.exit());
    }

    private void onContinue(ActionEvent e) {
        String user = view.getUsernameField().getText();
        String pass = view.getPasswordField().getText();

        if (user.isEmpty() || pass.isEmpty()) {
            view.setPromptLabel("Username/password cannot be empty!");
            view.setPromptLabelVisible(true);
            return;
        }

        try {
            // 1) create & register callback servant (no Stage needed)
            LoginCallBackServiceImpl cbServant = new LoginCallBackServiceImpl();

            // ← use your helper instead of raw POA calls
            org.omg.CORBA.Object cbObjRef =
                    PlayerClient_Java.getClientModel()
                            .registerLoginCallback(cbServant);

            LoginCallbackService cbStub =
                    LoginCallbackServiceHelper.narrow(cbObjRef);

            // 2) invoke Model
            LoginResult result = model.login(user, pass, cbStub);

            // 3) store session
            SessionManager.setSessionToken(result.getSessionToken());
            SessionManager.setLoggedInPlayer(result.getAccount());

            // 4) UX feedback + navigation
            view.setPromptLabel("Login successful! Redirecting...");
            view.setPromptLabelVisible(true);
            ViewNavigator.goToLobby();

        } catch (Exception ex) {
            view.setPromptLabel("Login failed: " + ex.getMessage());
            view.setPromptLabelVisible(true);
            ex.printStackTrace();
        }
    }
}
