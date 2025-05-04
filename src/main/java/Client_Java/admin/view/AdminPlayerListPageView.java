package Client_Java.admin.view;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import Shared_Files.PlayerAccount;
import javafx.scene.control.cell.PropertyValueFactory;

public class AdminPlayerListPageView {

    @FXML
    private TableColumn<PlayerAccount, String> usernameColumn;

    @FXML
    private TableColumn<PlayerAccount, String> passwordColumn;

    @FXML
    private TableColumn<PlayerAccount, String> fullNameColumn;  // Assuming you'll extend PlayerAccount or provide separately.

    @FXML
    private TableColumn<PlayerAccount, Integer> totalGamesColumn;

    @FXML
    private TableColumn<PlayerAccount, Integer> totalPointsColumn;

    @FXML
    private TableColumn<PlayerAccount, Void> editColumn;

    @FXML
    private TableColumn<PlayerAccount, Void> deleteColumn;

    @FXML
    public TextField searchTextField;

    @FXML
    public TableView<PlayerAccount> playersTable;

    public void initializeTableColumns() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        passwordColumn.setCellValueFactory(new PropertyValueFactory<>("password"));
        totalGamesColumn.setCellValueFactory(new PropertyValueFactory<>("gameWins"));
        // You can map totalPointsColumn if PlayerAccount has a points field later.

        // Edit and delete will require custom cell factories for button actions.
    }
}
