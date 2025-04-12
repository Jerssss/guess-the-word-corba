package Client_Java.player.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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

    public void initialize() {
        System.out.println("[DEBUG] Initializing Game Lobby View...");

        if (enterGameButton == null) {
            System.err.println("[ERROR] enterGameButton is NULL! Check FXML.");
        }
        if (quitButton == null) {
            System.err.println("[ERROR] quitButton is NULL! Check FXML.");
        }
    }

    public void setActionEnterGameButton(EventHandler<ActionEvent> event) {
        enterGameButton.setOnAction(event);
    }

    public void setActionQuitButton(EventHandler<ActionEvent> event) {
        if (quitButton == null) {
            System.err.println("[VIEW ERROR] quitButton is NULL!");
        } else {
            System.out.println("[DEBUG] Quit button bound with event handler.");
            quitButton.setOnAction(event);
        }
    }


    public void setActionRefreshLeaderboardButton(EventHandler<ActionEvent> event) {
        refreshLeaderboardButton.setOnAction(event);
    }

    public void setActionAboutButton(EventHandler<ActionEvent> event) {
        aboutButton.setOnAction(event);
    }

    public Text getCurrentUserLB() {
        return currentUserLB;
    }

    public Text getCurrentUserPointsLB() {
        return currentUserPointsLB;
    }

    public Text getCurrentUserRankLB() {
        return currentUserRankLB;
    }

    public FlowPane getLeaderboardsFlowPane() {
        return leaderboardsFlowPane;
    }

    public ScrollPane getLeaderboardScrollPane() {
        return leaderboardScrollPane;
    }

    public Text getGamePointsLabelLB() {
        return gamePointsLabelLB;
    }

    public Text getPlayerNameLabelLB() {
        return playerNameLabelLB;
    }

    public Text getRankLabelLB() {
        return rankLabelLB;
    }

}
