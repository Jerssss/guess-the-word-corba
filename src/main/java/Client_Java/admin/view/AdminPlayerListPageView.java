package Client_Java.admin.view;

import Client_Java.admin.view.modals.EditPlayerPopupView;
import com.mysql.cj.conf.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import Shared_Files.PlayerAccount;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class AdminPlayerListPageView {

    @FXML
    public TextField searchTextField;
    @FXML
    public TableView<PlayerAccount> playersTable;
    @FXML
    private TableColumn<PlayerAccount, Integer> userIdColumn;
    @FXML
    private TableColumn<PlayerAccount, String> usernameColumn;
    @FXML
    private TableColumn<PlayerAccount, String> passwordColumn;
    @FXML
    private TableColumn<PlayerAccount, String> fullNameColumn;  // Assuming you'll extend PlayerAccount or provide separately.
    @FXML
    private TableColumn<PlayerAccount, Integer> gameWinsColumn;
    @FXML
    private TableColumn<PlayerAccount, String> editColumn;
    @FXML
    private TableColumn<PlayerAccount, String> deleteColumn;

    @FXML
    public Button returnButton;
    @FXML
    private Text titleLabel;
    @FXML
    private ImageView backgroundImageView;

    private Font maryKate;
    private final String MARYKATE_FONT_PATH = "/css/fonts/bryndan-write.ttf";

    public void initialize(){
        loadImage(backgroundImageView, "/images/testUI/admin/mirror-zoomed-cropped.png");
        loadCustomFonts();
        applyFonts();
        initializeTableColumns();
    }

    public void initializeTableColumns() {
        userIdColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPlayerId()));
        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        passwordColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPassword()));
        fullNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        gameWinsColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getGameWins()));
        editColumn.setCellFactory(column -> createEditButtonCellFactory());
        //deleteColumn.setCellFactory(column -> createDeleteButtonCellFactory());
    }


    private TableCell<PlayerAccount, String> createEditButtonCellFactory() {
        return new TableCell<PlayerAccount, String>() {
            private final Button modifyButton = new Button();
            {
                Image img = new Image(getClass().getResourceAsStream("/images/testUI/admin/edit_button.png"));
                ImageView iv = new ImageView(img);
                iv.setFitWidth(16);
                iv.setFitHeight(16);
                modifyButton.setGraphic(iv);
                modifyButton.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-padding: 0;" +
                                "-fx-cursor: hand;"
                );
                modifyButton.setOnAction(event -> {
                    PlayerAccount player = (PlayerAccount) getTableRow().getItem();
                    if (player != null) {
                        System.out.println("MAGLAGAY KA POP UP");
                        showModifyPane(player);
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : modifyButton);
            }
        };
    }

    public void showModifyPane(PlayerAccount player) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/EditPlayerPopup.fxml"));
            Parent root = loader.load();

            EditPlayerPopupView view = loader.getController();
            view.setPlayer(player);

            Stage stage = new Stage();
            stage.setTitle("Edit Player");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Blocks interaction with other windows
            stage.showAndWait(); // Waits for the window to be closed before resuming

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private TableCell<PlayerAccount, String> createDeleteButtonCellFactory() {
        return null;
    }


    private void loadCustomFonts() {
        try {
            // Load fonts to Font objects
            maryKate = Font.loadFont(getClass().getResourceAsStream(MARYKATE_FONT_PATH), 30);

            // Fallbacks when the loading should fail

            if (maryKate == null) {
                System.err.println("MaryKate font not loaded. Using system font.");
                maryKate = Font.font("System", 12);
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Font loading exception: " + e.getMessage());
            maryKate = Font.font("System", 12);
        }
    }

    private void applyFonts() {
        // Font application to fields and labels
        if (titleLabel != null) {
            titleLabel.setFont(Font.font(maryKate.getFamily(), 29));
        }
    }

    private void loadImage(ImageView imageView, String resourcePath) {
        try {
            // Try from resources first
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is != null) {
                imageView.setImage(new Image(is));
                System.out.println("Loaded image: " + resourcePath);
            } else {
                // backup
                String absPath = "file:src/main/resources" + resourcePath;
                imageView.setImage(new Image(absPath));
            }
        } catch (Exception e) {
            System.err.println("Failed to load image: " + resourcePath);
            e.printStackTrace();
        }
    }
}
