package Client_Java.player.view.cards;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class LobbyLeaderboardCardView {
    @FXML private HBox root;  // This should match your FXML root element
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

    public HBox getRoot() {
        return root;
    }
}