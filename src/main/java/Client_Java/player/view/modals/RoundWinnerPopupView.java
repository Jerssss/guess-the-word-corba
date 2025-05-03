// File: Client_Java/player/view/modals/RoundWinnerPopupView.java
package Client_Java.player.view.modals;

import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class RoundWinnerPopupView {

    @FXML
    private Text winningUsernameLabel;

    public void setWinnerName(String name) {
        winningUsernameLabel.setText(name);
    }
}
