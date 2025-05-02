package Client_Java.player.view.modals;

import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class GameWinnerPopupView {
    @FXML private Text gameTitleLabel1;
    @FXML private Text winningUsernameLabel;

    /** Called by your callback to set the big “Game Winner!” text */
    public void setGameTitle(String title) {
        gameTitleLabel1.setText(title);
    }

    /** Called by your callback to set the actual username */
    public void setWinningUsername(String username) {
        winningUsernameLabel.setText(username);
    }
}
