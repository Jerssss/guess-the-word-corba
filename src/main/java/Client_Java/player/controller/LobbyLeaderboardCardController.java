package Client_Java.player.controller;

import Client_Java.player.view.cards.LobbyLeaderboardCardView;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import java.io.File;
import java.io.IOException;


public class LobbyLeaderboardCardController {

    public static Node createCard (int rank, String player) {
        try{
            FXMLLoader loader = new FXMLLoader(
                    new File("/src/main/java/Client_Java/player/res/fxml/WWLobbyLeaderboardCard.fxml").toURI().toURL());

            Node card = loader.load(); //load the fxml into a node named card

            LobbyLeaderboardCardView view = loader.getController();


            //this method expects a player value in the format of "username-points"
            String username = player.split("-")[0];
            String points = player.split("-")[1];

            view.setRankLabel(rank);
            view.setUsernameLabel(username);
            view.setPointsLabel(points);

            return card;
        } catch (RuntimeException | IOException rio) {
            rio.printStackTrace();
        }
        return null;
    }
}
