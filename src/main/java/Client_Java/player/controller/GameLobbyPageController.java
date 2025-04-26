package Client_Java.player.controller;

import Client_Java.PlayerClient_Java;
import Client_Java.player.model.GameLobbyModel;
import Client_Java.player.view.GameLobbyPageView;
import Shared_Files.PlayerAccount;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;

public class GameLobbyPageController {
    private final GameLobbyModel model;
    private final GameLobbyPageView view;
    private final PlayerAccount player;
    private final int gameId;
    private final String sessionToken;

    public GameLobbyPageController(GameLobbyModel model, GameLobbyPageView view, PlayerAccount player, int gameId, String sessionToken) {
        this.model = model;
        this.view = view;
        this.player = player;
        this.gameId = gameId;
        this.sessionToken = sessionToken;

        initialize();
    }

    private void initialize() {
        // Bind button actions
        view.setActionEnterGameButton(this::onEnterGame);
        view.setActionQuitButton(this::onQuit);
        view.setActionRefreshLeaderboardButton(this::onRefreshLeaderboard);

        // Display player info
        view.getCurrentUserLB().setText(player.getUsername());
        view.getPlayerNameLabelLB().setText(player.getUsername());
        view.getCurrentUserPointsLB().setText(Integer.toString(player.getGame_wins())); // Temporary

        // Initial Leaderboard Load
        loadLeaderboard();
    }

    private void onEnterGame(ActionEvent event) {
        System.out.println("[Client] Enter Game button pressed (not yet implemented).");
        // Later: connect to server, join lobby, etc.
    }

    private void onQuit(ActionEvent event) {
        System.out.println("[Client] Quit button pressed.");
        Platform.exit();
    }

    private void onRefreshLeaderboard(ActionEvent event) {
        System.out.println("[Client] Refresh Leaderboard pressed.");
        loadLeaderboard();
    }

    private void loadLeaderboard() {
        FlowPane pane = view.getLeaderboardsFlowPane();
        pane.getChildren().clear(); // Clear old entries

        model.fetchLeaderboard().forEach(entry -> {
            Label label = new Label(entry);
            label.setStyle("-fx-font-size: 16px; -fx-text-fill: black;");
            pane.getChildren().add(label);
        });
    }
}
