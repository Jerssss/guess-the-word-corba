package Client_Java.player.view.modals;

import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class GameRoundPopupView {
    @FXML
    private Text gameTitleLabel;

    @FXML
    private Text roundNumber;

    public void setGameTitleLabel(Text gameTitleLabel) {
        this.gameTitleLabel = gameTitleLabel;
    }

    public void setRoundNumber(Text roundNumber) {
        this.roundNumber = roundNumber;
    }
}
