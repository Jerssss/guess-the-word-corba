package Client_Java.player.controller;


import Client_Java.PlayerClient_Java;
import Client_Java.player.model.GameLobbyModel;
import Client_Java.player.view.GameLobbyPageView;
import Client_Java.player.view.cards.LobbyLeaderboardCardView;
import GameApp.Player;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;

import java.io.IOException;

public class GameLobbyPageController {
    private final GameLobbyModel model;
    private final GameLobbyPageView view;
    private final Player player;

    public GameLobbyPageController(GameLobbyModel model, GameLobbyPageView view, Player player) {
        this.model = model;
        this.view = view;
        this.player = player;

        view.setActionEnterGameButton(event -> handleEnterGame());
        view.setActionQuitButton(event -> handleQuit());
        view.setActionRefreshLeaderboardButton(event -> refreshLeaderboard());

        loadPlayerInfo();
        refreshLeaderboard();
    }

    private void loadPlayerInfo() {
        view.getCurrentUserLB().setText(player.fullName);
        view.getCurrentUserPointsLB().setText(String.valueOf(player.gameWins));
        view.getPlayerNameLabelLB().setText(player.username);
    }

    private void handleEnterGame() {
        System.out.println("[GAME] Entering game...");
        // Implement scene switch to game screen if needed
        //TODO: implement the entergame functionality
    }

    private void handleQuit() {
        System.out.println("[INFO] Logging out...");

        try {
            // Log out directly via database call
            Server_Java.database.PlayerQueries.logout(player.playerID);
            PlayerClient_Java.setLoggedInPlayer(null);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_Java/res/fxml/WWLoginPage.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            PlayerClient_Java.getStage().setScene(scene);
            PlayerClient_Java.getStage().setTitle("What's the Word - Login");
            PlayerClient_Java.getStage().show();

            System.out.println("[INFO] Successfully logged out and returned to login screen.");
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to logout or redirect to login.");
            e.printStackTrace();
        }
    }


    private void refreshLeaderboard() {
        FlowPane leaderboardPane = view.getLeaderboardsFlowPane();
        leaderboardPane.getChildren().clear();

        String[] topPlayers = model.fetchTopPlayers();
        int rank = 1;

        for (String entry : topPlayers) {
            String[] split = entry.split("-");
            if (split.length < 2) continue;

            String username = split[0];
            String points = split[1];

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("src/main/java/Client_Java/player/res/fxml/WWLobbyLeaderboardCard.fxml"));
                Node card = loader.load();

                LobbyLeaderboardCardView cardView = loader.getController();
                ((Text) card.lookup("#usernameLabel")).setText(username);
                ((Text) card.lookup("#pointsLabel")).setText(points);
                ((Text) card.lookup("#rankLabel")).setText(String.valueOf(rank++));

                leaderboardPane.getChildren().add(card);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
