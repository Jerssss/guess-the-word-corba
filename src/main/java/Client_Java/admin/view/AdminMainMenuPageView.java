package Client_Java.admin.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

public class AdminMainMenuPageView {

    @FXML
    private Button createAccountButton;

    @FXML
    private Button editConfigButton;

    @FXML
    private Button quitButton;

    @FXML
    private Button viewPlayersButton;

    @FXML
    private BorderPane pane;

    public void initialize() {
        System.out.println("[DEBUG] Initializing Admin Main Menu View...");

        if (createAccountButton == null || editConfigButton == null ||
                viewPlayersButton == null || quitButton == null || pane == null) {
            System.err.println("[ERROR] One or more FXML components are null! Check FXML bindings.");
        }
    }

    public Button getCreateAccountButton() {
        return createAccountButton;
    }

    public Button getEditConfigButton() {
        return editConfigButton;
    }

    public Button getQuitButton() {
        return quitButton;
    }

    public Button getViewPlayersButton() {
        return viewPlayersButton;
    }

    public BorderPane getPane() {
        return pane;
    }

    public void setActionCreateAccountButton(EventHandler<ActionEvent> event) {
        createAccountButton.setOnAction(event);
    }

    public void setActionEditConfigButton(EventHandler<ActionEvent> event) {
        editConfigButton.setOnAction(event);
    }

    public void setActionViewPlayersButton(EventHandler<ActionEvent> event) {
        viewPlayersButton.setOnAction(event);
    }

    public void setActionQuitButton(EventHandler<ActionEvent> event) {
        quitButton.setOnAction(event);
    }
}
