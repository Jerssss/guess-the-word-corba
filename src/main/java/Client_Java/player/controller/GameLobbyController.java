// File: Client_Java/player/controller/GameLobbyController.java
package Client_Java.player.controller;

import Client_Java.player.model.GameLobbyModel;
import Client_Java.player.view.GameLobbyView;
import Client_Java.player.view.ViewNavigator;
import Shared_Files.PlayerAccount;
import javafx.event.ActionEvent;

/**
 * Controller for the game lobby screen.
 * Strict MVC: all navigation goes through ViewNavigator.
 */
public class GameLobbyController {
    private final GameLobbyModel model;
    private final GameLobbyView  view;
    private final PlayerAccount  player;

    public GameLobbyController(
            GameLobbyModel model,
            GameLobbyView view,
            PlayerAccount player
    ) {
        this.model  = model;
        this.view   = view;
        this.player = player;
        initialize();
    }

    private void initialize() {
        view.setActionEnterGameButton(this::handleEnterGame);
        view.setActionQuitButton(e -> System.exit(0));
        view.setActionAboutButton(this::handleAbout);
    }

    private void handleEnterGame(ActionEvent event) {
        try {
            ViewNavigator.goToWaitingRoom();
        } catch (Exception e) {
            e.printStackTrace();
       //     view.showError("Could not open waiting room: " + e.getMessage());
        }
    }

    private void handleAbout(ActionEvent event) {
     //   view.showAboutDialog();
    }
}
