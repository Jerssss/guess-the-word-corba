package Client_Java.player.controller;

import Client_Java.player.PlayerClient_Java;
import Client_Java.player.PlayerClient_Model;
import Client_Java.player.model.GameLobbyModel;
import Client_Java.player.model.WaitingRoomModel;
import Client_Java.player.view.GameLobbyView;
import Client_Java.player.view.WaitingRoomView;
import Client_Java.player.controller.WaitingRoomController;
import Shared_Files.PlayerAccount;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Controller for the game lobby screen.
 */
public class GameLobbyController {
    private final GameLobbyModel model;
    private final GameLobbyView view;
    private final PlayerAccount player;
    private final int gameId;
    private final String sessionToken;

    public GameLobbyController(GameLobbyModel model, GameLobbyView view,
                               PlayerAccount player, int gameId, String sessionToken) {
        this.model = model;
        this.view = view;
        this.player = player;
        this.gameId = gameId;
        this.sessionToken = sessionToken;

        initialize();
    }

    private void initialize() {
        // Bind button actions
        view.setActionEnterGameButton(this::handleEnterGame);
        view.setActionQuitButton(this::handleQuit);
        view.setActionAboutButton(this::handleAbout);
        view.setActionRefreshLeaderboardButton(this::refreshLeaderboard);

        // Initial Leaderboard Load
        loadLeaderboard();
    }

    private void handleEnterGame(ActionEvent event) {
        System.out.println("[Client] Enter Game button pressed.");

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/player/WaitingRoomPage.fxml"));
            Parent root = loader.load();

            // Setup WaitingRoom MVC
            WaitingRoomView waitingView = loader.getController();
            WaitingRoomModel waitingModel = new WaitingRoomModel(
                    PlayerClient_Model.gameService);
            // Assume PlayerAccount has getPlayerId(), else use PlayerClient_Java.getLoggedInPlayerID()
            int playerId = player.getPlayerId();
            new WaitingRoomController(waitingModel, waitingView,
                    playerId, sessionToken);

            Scene waitingScene = new Scene(root);
            URL stylesheet = getClass().getClassLoader()
                    .getResource("css/styles.css");
            if (stylesheet != null) {
                waitingScene.getStylesheets().add(stylesheet.toExternalForm());
                System.out.println("[Client] Waiting Room Stylesheet loaded.");
            } else {
                System.out.println("[Client] No Waiting Room stylesheet found.");
            }

            Platform.runLater(() -> {
                Stage stage = PlayerClient_Java.getStage();
                stage.setScene(waitingScene);
                stage.setTitle("What's The Word - Waiting Room");
                stage.show();
            });

        } catch (IOException e) {
            System.err.println("[Client ERROR] Failed to load WaitingRoomPage.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleQuit(ActionEvent event) {
        System.out.println("[Client] Quit button pressed.");
        Platform.exit();
    }

    private void handleAbout(ActionEvent event) {
        System.out.println("Showing about dialog");
        // TODO: Implement about dialog
    }

    private void refreshLeaderboard(ActionEvent event) {
        System.out.println("[Client] Refresh Leaderboard pressed.");
        loadLeaderboard();
    }

    private void loadLeaderboard() {
        FlowPane pane = view.getLeaderboardsFlowPane();
        pane.getChildren().clear();

        model.fetchLeaderboard().forEach(entry -> {
            Label label = new Label(entry);
            label.setStyle("-fx-font-size: 16px; -fx-text-fill: black;");
            pane.getChildren().add(label);
        });
    }
}
