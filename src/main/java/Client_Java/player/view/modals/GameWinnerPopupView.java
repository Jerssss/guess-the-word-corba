package Client_Java.player.view.modals;

import Client_Java.player.view.ViewNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

public class GameWinnerPopupView {
    @FXML private Text gameTitleLabel1;
    @FXML private Text winningUsernameLabel;
    @FXML private Button returnButton;

    public void setGameTitle(String title) {
        gameTitleLabel1.setText(title);
    }

    public void setWinningUsername(String username) {
        winningUsernameLabel.setText(username);
    }

    @FXML
    private void onReturn() {
        try {
            ViewNavigator.goToLobby();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
