package Client_Java.player.view.modals;

import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class GameWinnerPopupView {

    @FXML
    private Text gameTitleLabel;

    @FXML
    private Text winningUsernameLabel;

    public void setGameTitleLabel(Text gameTitleLabel) {
        this.gameTitleLabel = gameTitleLabel;
    }

    public void setWinningUsernameLabel(Text winningUsernameLabel) {
        this.winningUsernameLabel = winningUsernameLabel;
    }
}
