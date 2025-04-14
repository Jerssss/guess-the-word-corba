package Client_Java.player.view.cards;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.text.Text;

public class LobbyLeaderboardCardView {

    @FXML
    private Label pointsLabel;

    @FXML
    private Label rankLabel;

    @FXML
    private Label usernameLabel;

    public void setRankLabel(int rank) {
        rankLabel.setText(String.valueOf(rank));
    }

    public void setUsernameLabel(String username) {
        rankLabel.setText(String.valueOf(username));
    }

    public void setPointsLabel(String points) {
        rankLabel.setText(String.valueOf(points));
    }

}
