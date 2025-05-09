package Client_Java.player.view.modals;

import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class RoundWinnerPopupView {

    @FXML
    private Text winningUsernameLabel;

    @FXML
    private Text winningWordLabel;

    public void setWinnerName(String name) {
        winningUsernameLabel.setText(name);
    }

    public void setWinningWord(String word) {
        winningWordLabel.setText(word);
    }
}