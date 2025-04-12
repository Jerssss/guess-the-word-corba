package Client_Java.player.view.cards;

import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class LobbyLeaderboardCardView {

    @FXML
    private Text pointsLabel;

    @FXML
    private Text rankLabel;

    @FXML
    private Text usernameLabel;

    public void setRankLabel(Text rankLabel) {
        this.rankLabel = rankLabel;
    }

    public void setUsernameLabel(Text usernameLabel) {
        this.usernameLabel = usernameLabel;
    }

    public void setPointsLabel(Text pointsLabel) {
        this.pointsLabel = pointsLabel;
    }

}
