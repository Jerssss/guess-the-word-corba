package Client_Java.player.view.modals;

import javafx.fxml.FXML;
import javafx.scene.text.Text;

/**
 * Controller for RoundWinnerPopupView.fxml.
 */
public class RoundWinnerPopupView {
    @FXML private Text winningUsernameLabel;

    /** Called by FXMLLoader */
    @FXML
    private void initialize() {
        // nothing yet
    }

    /**
     * Set the displayed winner name.
     */
    public void setWinnerName(String name) {
        winningUsernameLabel.setText(name);
    }
}
