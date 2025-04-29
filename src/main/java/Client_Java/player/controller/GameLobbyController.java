package Client_Java.player.controller;

import Client_Java.player.PlayerClient_Java;
import Client_Java.player.model.GameLobbyModel;
import Client_Java.player.view.GameLobbyView;
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

public class GameLobbyController {
    private final GameLobbyModel model;
    private final GameLobbyView view;
    private final PlayerAccount player;
    private final int gameId;
    private final String sessionToken;

    public GameLobbyController(GameLobbyModel model, GameLobbyView view, PlayerAccount player, int gameId, String sessionToken) {
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
        view.setActionAboutButton(this:: handleAbout);
        view.setActionRefreshLeaderboardButton(this::refreshLeaderboard);

        // Display player info
//        view.getCurrentUserLB().setText(player.getUsername());
  //      view.getPlayerNameLabelLB().setText(player.getUsername());
  //      view.getCurrentUserPointsLB().setText(Integer.toString(player.getGame_wins())); // Temporary

        // Initial Leaderboard Load
        loadLeaderboard();
    }


    private void handleEnterGame(ActionEvent event) {
        System.out.println("[Client] Enter Game button pressed.");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/player/WaitingRoomPage.fxml"));
            Parent root = loader.load();

            // TODO: Setup WaitingRoom controller

            Scene waitingScene = new Scene(root);

            try {
                URL stylesheet = getClass().getClassLoader().getResource("css/styles.css");
                if (stylesheet != null) {
                    waitingScene.getStylesheets().add(stylesheet.toExternalForm());
                    System.out.println("[Client] Waiting Room Stylesheet loaded.");
                } else {
                    System.out.println("[Client] No Waiting Room stylesheet found.");
                }
            } catch (Exception e) {
                System.err.println("[Client ERROR] Error loading Waiting Room stylesheet: " + e.getMessage());
            }

            // Update the Stage
            Platform.runLater(() -> {
                Stage stage = PlayerClient_Java.getStage();
                stage.setScene(waitingScene);
                stage.setTitle("What's The Word - Waiting Room");
                stage.show();
            });

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("[Client ERROR] Failed to load WaitingRoomPage.fxml");
        }
    }


    private void handleQuit(ActionEvent event) {
        System.out.println("[Client] Quit button pressed.");
        Platform.exit();
    }

    private void handleAbout(ActionEvent event) {
        // TODO: Implement about dialog
        System.out.println("Showing about dialog");
    }

    private void refreshLeaderboard(ActionEvent event) {
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
