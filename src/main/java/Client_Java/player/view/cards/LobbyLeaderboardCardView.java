package Client_Java.player.view.cards;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class LobbyLeaderboardCardView {
    @FXML private Label rankLabel;
    @FXML private Label usernameLabel;
    @FXML private Label pointsLabel;

    public LobbyLeaderboardCardView() {}

    public void setRank(int rank) {
        rankLabel.setText(String.valueOf(rank));
    }

    public void setUsername(String username) {
        usernameLabel.setText(username);
    }

    public void setPoints(int points) {
        pointsLabel.setText(String.valueOf(points));
    }
}