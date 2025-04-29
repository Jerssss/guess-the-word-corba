package Client_Java.player.view.modals;

import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class RoundWinnerPopupView {
    @FXML
    private Text winningUsernameLabel;

    public void setWinningUsernameLabel(Text winningUsernameLabel) {
        this.winningUsernameLabel = winningUsernameLabel;
    }
}
