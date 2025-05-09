package Client_Java.player.view.modals;

import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class NoWinnerPopupView {

    @FXML
    private Text secretWordLabel;

    public void setSecretWord(String word) {
        secretWordLabel.setText(word);
    }
}