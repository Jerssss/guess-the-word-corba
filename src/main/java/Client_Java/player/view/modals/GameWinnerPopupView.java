package Client_Java.player.view.modals;

import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class GameWinnerPopupView {

    // NOTE: this must match the fx:id in your FXML
    @FXML
    private Text gameTitleLabel1;

    @FXML
    private Text winningUsernameLabel;

    /** Called by FXMLLoader after fields are injected. */
    @FXML
    private void initialize() {
        // you can do any styling or default text here if you like
    }

    /** Sets the popup title (e.g. "Game Winner!"). */
    public void setGameTitle(String title) {
        gameTitleLabel1.setText(title);
    }

    /** Sets the winner’s username to display. */
    public void setWinningUsername(String username) {
        winningUsernameLabel.setText(username);
    }

    /** If you ever need direct access to the Text node in code. */
    public Text getGameTitleLabel() {
        return gameTitleLabel1;
    }

    /** If you ever need direct access to the Text node in code. */
    public Text getWinningUsernameLabel() {
        return winningUsernameLabel;
    }
}
