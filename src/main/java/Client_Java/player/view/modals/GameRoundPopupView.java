package Client_Java.player.view.modals;

import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class GameRoundPopupView {
    @FXML private Text roundNumber;
    @FXML private Text gameTitleLabel;

    /** Called by your callback to set “ROUND N” */
    public void setRoundNumber(int round) {
        roundNumber.setText("ROUND " + round);
    }

    /** (Optional) if you want a custom title; can hard‑code “What’s The Word?” too */
    public void setGameTitle(String title) {
        gameTitleLabel.setText(title);
    }
}
