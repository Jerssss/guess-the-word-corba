package Server_Java.view;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class AdminPlayerListPageView {
    @FXML
    private TableColumn<?, ?> deleteColumn;

    @FXML
    private TableColumn<?, ?> editColumn;

    @FXML
    private TableColumn<?, ?> fullNameColumn;

    @FXML
    private TableColumn<?, ?> passwordColumn;

    @FXML
    private TableView<?> playersTable;

    @FXML
    private TextField searchTextField;

    @FXML
    private TableColumn<?, ?> totalGamesColumn;

    @FXML
    private TableColumn<?, ?> totalPointsColumn;

    @FXML
    private TableColumn<?, ?> usernameColumn;

}
