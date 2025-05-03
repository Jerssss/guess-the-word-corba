// File: Client_Java/player/view/modals/GameRoundPopupView.java
package Client_Java.player.view.modals;

import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class GameRoundPopupView {
    @FXML
    private Text roundNumber;
    @FXML
    private Text gameTitleLabel;

    /** Sets the “ROUND N” text */
    public void setRoundNumber(int round) {
        roundNumber.setText("ROUND " + round);
    }

    /** Sets the title text */
    public void setGameTitle(String title) {
        gameTitleLabel.setText(title);
    }
}
