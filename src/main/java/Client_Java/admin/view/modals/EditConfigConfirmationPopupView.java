package Client_Java.admin.view.modals;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
public class EditConfigConfirmationPopupView {

    @FXML
    private Text label;

    @FXML
    private Button noButton;

    @FXML
    private Button yesButton;

    /** Buttons */
    public void setActionYesButton(EventHandler<ActionEvent> event) {

    }

    public void setActionNoButton(EventHandler<ActionEvent> event) {

    }

    public void setLabel(Text label) {
        this.label = label;
    }
}
