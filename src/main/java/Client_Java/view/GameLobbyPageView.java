package Client_Java.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;

public class GameLobbyPageView {

    @FXML
    private Button aboutButton;

    @FXML
    private Text currentUserLB;

    @FXML
    private Text currentUserPointsLB;

    @FXML
    private Text currentUserRankLB;

    @FXML
    private Button enterGameButton;

    @FXML
    private Text gamePointsLabelLB;

    @FXML
    private ScrollPane leaderboardScrollPane;

    @FXML
    private FlowPane leaderboardsFlowPane;

    @FXML
    private Text playerNameLabelLB;

    @FXML
    private Button quitButton;

    @FXML
    private Text rankLabelLB;

    @FXML
    private Button refreshLeaderboardButton;

}
