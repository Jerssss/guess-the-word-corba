package Client_Java.admin.view.modals;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

public class DeletePlayerConfirmationPopupView {

    @FXML
    private Text label;

    @FXML
    private Button noButton;

    @FXML
    private Text playerToDeleteLabel;

    @FXML
    private Button yesButton;

    public void setActionYesButton (EventHandler<ActionEvent> event){

    }

    public void setActionNoButton (EventHandler<ActionEvent> event) {

    }

    public void setPlayerToDeleteLabel(Text playerToDeleteLabel) {
        this.playerToDeleteLabel = playerToDeleteLabel;
    }
}
