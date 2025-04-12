package Client_Java.admin.view.modals;

import javafx.event.EventHandler;
import javafx.event.ActionEvent;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class EditPlayerPopupView {

    @FXML
    private Button cancelButton;

    @FXML
    private TextField fullNameTextField;

    @FXML
    private TextField gamePointsTextField;

    @FXML
    private Label gamesPlayedLabel;

    @FXML
    private GridPane gridPane;

    @FXML
    private TextField passwordTextField;

    @FXML
    private Label promptLabel;

    @FXML
    private Button saveButton;

    @FXML
    private TextField usernameTextfield;

    /** Buttons */
    public void setActionCancelButton(EventHandler<ActionEvent> event) {

    }

    public void setActionSaveButton(EventHandler<ActionEvent> event) {

    }

    /** Getters and Setters */
    public TextField getFullNameTextField() {
        return fullNameTextField;
    }

    public TextField getUsernameTextfield() {
        return usernameTextfield;
    }

    public TextField getPasswordTextField() {
        return passwordTextField;
    }

    public TextField getGamePointsTextField() {
        return gamePointsTextField;
    }

    public void setGamesPlayedLabel(Label gamesPlayedLabel) {
        this.gamesPlayedLabel = gamesPlayedLabel;
    }

    public void setPromptLabel(Label promptLabel) {
        this.promptLabel = promptLabel;
    }

}