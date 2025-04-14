package Client_Java.player.controller;

import Client_Java.player.view.cards.LobbyLeaderboardCardView;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import java.io.IOException;

public class LobbyLeaderboardCardController {

    public static Node createCard(int rank, String player) {
        try {
            // Use classloader instead of File for more reliable resource loading
            FXMLLoader loader = new FXMLLoader(
                    LobbyLeaderboardCardController.class.getResource("/Client_Java/player/res/fxml/WWLobbyLeaderboardCard.fxml"));

            Node card = loader.load();
            LobbyLeaderboardCardView view = loader.getController();

            String[] parts = player.split("-");
            if (parts.length == 2) {
                view.setRank(rank);
                view.setUsername(parts[0]);
                view.setPoints(Integer.parseInt(parts[1]));
            }

            return card;
        } catch (IOException e) {
            System.err.println("Error loading leaderboard card:");
            e.printStackTrace();
            return null;
        }
    }
}