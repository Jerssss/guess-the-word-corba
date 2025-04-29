package Client_Java.admin.view.modals;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;


public class CreatePlayerConfirmationPopupView {

    @FXML
    private Text label;

    @FXML
    private Button noButton;

    @FXML
    private Text playerToCreateLabel;

    @FXML
    private Button yesButton;

    /** Buttons */
    public void setActionYesButton (EventHandler <ActionEvent> event){

    }

    public void setActionNoButton (EventHandler<ActionEvent> event) {

    }

    /** Setters */
    public void setLabel(Text label) {
        this.label = label;
    }

    public void setPlayerToCreateLabel(Text playerToCreateLabel) {
        this.playerToCreateLabel = playerToCreateLabel;
    }
}
