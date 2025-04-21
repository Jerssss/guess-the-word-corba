package Client_Java.player.controller;

import Client_Java.PlayerClient_Java;
import Client_Java.player.view.GameLobbyPageView;
import Client_Java.player.model.*;
import Client_Java.player.view.GameRoomPageView;
import Client_Java.player.view.WaitingRoomSectionView;
import PlayerGame.Player;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class GameLobbyPageController {
    private final GameLobbyModel model;
    private final GameLobbyPageView view;
    private final Player player;
    private final int gid;
    private final String sessionToken;

    public GameLobbyPageController(GameLobbyModel model, GameLobbyPageView view, Player player, int gid, String sessionToken) {
        this.model = model;
        this.view = view;
        this.player = player;
        this.gid = gid;
        this.sessionToken = sessionToken;
        initialize();
    }

    private void initialize() {
        view.setActionEnterGameButton(event -> handleEnterGame());
        view.setActionQuitButton(event -> handleQuit());
        view.setActionRefreshLeaderboardButton(event -> refreshLeaderboard());
        view.setActionAboutButton(event -> handleAbout());

        // Load player info and leaderboard
        loadPlayerInfo();
        refreshLeaderboard();
    }

    private void loadPlayerInfo() {
        Platform.runLater(() -> {
            view.getCurrentUserLB().setText(player.username);
            view.getCurrentUserLB().setText(player.fullName);
            view.getCurrentUserPointsLB().setText(String.valueOf(player.gameWins));
        });
    }

    private void handleEnterGame() {
        System.out.println("[GAME] Waiting for Players...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_Java/player/res/fxml/WaitingRoomPage.fxml"));
            Parent root = loader.load();

            // Get the view and controller
            WaitingRoomSectionView roomSectionView = loader.getController();

            // Ensure these values are available in the class or method
            int gid = this.gid; // or fetch/set appropriately
            String sessionToken = this.sessionToken; // must be accessible

            WaitingRoomModel roomModel = new WaitingRoomModel(player, gid, sessionToken);
            new WaitingRoomController(roomModel, roomSectionView, player);

            // Create scene with stylesheet
            Scene lobbyScene = new Scene(root);

            try {
                URL stylesheet = getClass().getResource("/Client_Java/player/res/css/styles.css");
                if (stylesheet != null) {
                    lobbyScene.getStylesheets().add(stylesheet.toExternalForm());
                } else {
                    System.out.println("Stylesheet not found, using default styling");
                }
            } catch (Exception e) {
                System.err.println("Error loading stylesheet: " + e.getMessage());
            }

            // Update stage
            Stage stage = PlayerClient_Java.getStage();
            Platform.runLater(() -> {
                stage.setScene(lobbyScene);
                stage.setTitle("What's the Word - Waiting Room");
                stage.show();
            });

        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load WaitingRoom FXML.");
            e.printStackTrace();
        }

//        System.out.println("[GAME] Entering game...");
//        try {
//            // Load the GameRoomPageView FXML file
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_Java/player/res/fxml/WWGameRoomPage.fxml"));
//            Parent root = loader.load();
//
//            // Set the scene
//            Scene scene = new Scene(root);
//            PlayerClient_Java.getStage().setScene(scene);
//            PlayerClient_Java.getStage().setTitle("What's the Word - Game Room");
//            PlayerClient_Java.getStage().show();
//
//        } catch (IOException e) {
//            System.err.println("[ERROR] Failed to load GameRoomPageView: " + e.getMessage());
//            e.printStackTrace();
//        }
    }

    private void handleQuit() {
        System.out.println("[INFO] Logging out...");
        try {
            PlayerClient_Java.setLoggedInPlayer(null);

            // Return to login screen
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Client_Java/player/res/fxml/WWLoginPage.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            PlayerClient_Java.getStage().setScene(scene);
            PlayerClient_Java.getStage().setTitle("What's the Word - Login");
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to logout");
            e.printStackTrace();
        }
    }

    private void handleAbout() {
        // TODO: Implement about dialog
        System.out.println("Showing about dialog");
    }

    private void refreshLeaderboard() {
        Platform.runLater(() -> {
            try {
                FlowPane leaderboardPane = view.getLeaderboardsFlowPane();
                leaderboardPane.getChildren().clear();

                String[] topPlayers = model.fetchTopPlayers();
                int rank = 1;
                int previousScore = -1;
                int actualPosition = 0;

                for (String playerEntry : topPlayers) {
                    actualPosition++;
                    String[] parts = playerEntry.split("-");
                    if (parts.length != 2) continue;

                    int currentScore = Integer.parseInt(parts[1]);
                    if (currentScore != previousScore) {
                        rank = actualPosition;
                    }
                    previousScore = currentScore;

                    Node card = LobbyLeaderboardCardController.createCard(rank, playerEntry);
                    if (card != null) {
                        leaderboardPane.getChildren().add(card);
                    }
                }

                // Update current player's rank display
                updateCurrentPlayerRank();

            } catch (Exception e) {
                System.err.println("Error refreshing leaderboard:");
                e.printStackTrace();

                // Fallback UI
                Label errorLabel = new Label("Error loading leaderboard");
                errorLabel.setStyle("-fx-text-fill: red;");
                view.getLeaderboardsFlowPane().getChildren().add(errorLabel);
            }
        });
    }

    private void updateCurrentPlayerRank() {
        String[] topPlayers = model.fetchTopPlayers();
        int rank = 1;
        int previousScore = -1;
        int actualPosition = 0;

        for (String playerEntry : topPlayers) {
            actualPosition++;
            String[] parts = playerEntry.split("-");
            if (parts.length != 2) continue;

            int score = Integer.parseInt(parts[1]);
            if (score != previousScore) {
                rank = actualPosition;
            }
            previousScore = score;

            if (parts[0].equals(player.username)) {
                view.getCurrentUserRankLB().setText("#" + rank);
                view.getCurrentUserPointsLB().setText(String.valueOf(score));
                break;
            }
        }
    }
}