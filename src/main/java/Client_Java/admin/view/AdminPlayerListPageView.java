package Client_Java.admin.view;

import Client_Java.admin.AdminClient_Java;
import Client_Java.admin.controller.AdminMainMenuPageController;
import Client_Java.admin.model.AdminClientModel;
import Client_Java.admin.model.AdminMainMenuPageModel;
import Client_Java.admin.view.modals.EditPlayerPopupView;
import Shared_Files.AdminAccount;
import Shared_Files.PlayerAccount;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

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
    private TableColumn<PlayerAccount, String> fullNameColumn;
    @FXML
    private TableColumn<PlayerAccount, Integer> gameWinsColumn;
    @FXML
    public TableColumn<PlayerAccount, String> editColumn;
    @FXML
    private TableColumn<PlayerAccount, String> deleteColumn;

    @FXML
    public Button returnButton;
    @FXML
    private Text titleLabel;
    @FXML
    private ImageView backgroundImageView;
    private ObservableList<PlayerAccount> playerData = FXCollections.observableArrayList();
    private Consumer<PlayerAccount> onEditPlayerCallback; // Callback to notify controller

    private Font maryKate;
    private final String MARYKATE_FONT_PATH = "/css/fonts/bryndan-write.ttf";

    public void initialize() {
        loadImage(backgroundImageView, "/images/testUI/admin/mirror-zoomed-cropped.png");
        loadCustomFonts();
        applyFonts();
        initializeTableColumns();
        playersTable.setItems(playerData);
    }

    public void setActionReturnButton(EventHandler<ActionEvent> event) {
        returnButton.setOnAction(event);
    }

    public void setOnEditPlayerCallback(Consumer<PlayerAccount> callback) {
        this.onEditPlayerCallback = callback;
    }

    public void initializeTableColumns() {
        userIdColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPlayerId()));
        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        passwordColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPassword()));
        fullNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        gameWinsColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getGameWins()));
        editColumn.setCellFactory(column -> createEditButtonCellFactory());
        // deleteColumn.setCellFactory(column -> createDeleteButtonCellFactory());
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
            view.setOnSaveCallback(updatedPlayer -> {
                // Notify controller of the updated player
                if (onEditPlayerCallback != null) {
                    onEditPlayerCallback.accept(updatedPlayer);
                }
            });

            Stage stage = new Stage();
            stage.setTitle("Edit Player");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load EditPlayerPopup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private TableCell<PlayerAccount, String> createDeleteButtonCellFactory() {
        return null; // Implement if needed
    }

    public void showAdminMainMenu(AdminAccount admin) {
        try {
            // Load FXML using File, consistent with original implementation
            File fxmlFile = new File("src/main/resources/fxml/admin/AdminMainMenuPage.fxml");
            if (!fxmlFile.exists()) {
                throw new IOException("FXML file not found: " + fxmlFile.getAbsolutePath());
            }
            System.out.println("[DEBUG] Loading FXML: " + fxmlFile.getAbsolutePath());

            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
            Scene scene = new Scene(loader.load());

            AdminMainMenuPageView pageView = loader.getController();
            if (pageView == null) {
                throw new IOException("AdminMainMenuPageView is NULL after FXML load");
            }
            System.out.println("[DEBUG] AdminMainMenuPageView loaded successfully.");

            AdminMainMenuPageModel pageModel = new AdminMainMenuPageModel(
                    AdminClientModel.adminService,
                    AdminClient_Java.getSessionToken(),
                    admin.getAdmin_id()
            );

            new AdminMainMenuPageController(
                    pageView,
                    pageModel,
                    AdminClientModel.adminService,
                    AdminClient_Java.getSessionToken(),
                    admin.getAdmin_id()
            );

            URL css = getClass().getClassLoader().getResource("css/styles.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }

            Stage stage = AdminClient_Java.getStage();
            Platform.runLater(() -> {
                stage.setScene(scene);
                stage.centerOnScreen();
                stage.setResizable(false);
                stage.setTitle("Admin Main Menu");
                stage.show();
            });

            System.out.println("[Client] Admin Main Menu GUI loaded successfully.");

        } catch (IOException e) {
            System.err.println("[ERROR] IOException while loading AdminMainMenuPage: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[ERROR] Unexpected exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateTable(List<PlayerAccount> data) {
        playerData.setAll(data);
        playersTable.setItems(null);
        playersTable.setItems(playerData);
        playersTable.refresh();
        System.out.println("[ADMIN CLIENT | " + new Date() + "] Player data updated. New table size: " + playerData.size());
    }

    private void loadCustomFonts() {
        try {
            maryKate = Font.loadFont(getClass().getResourceAsStream(MARYKATE_FONT_PATH), 30);
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
        if (titleLabel != null) {
            titleLabel.setFont(Font.font(maryKate.getFamily(), 29));
        }
    }

    private void loadImage(ImageView imageView, String resourcePath) {
        try {
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is != null) {
                imageView.setImage(new Image(is));
                System.out.println("Loaded image: " + resourcePath);
            } else {
                String absPath = " química file:src/main/resources" + resourcePath;
                imageView.setImage(new Image(absPath));
            }
        } catch (Exception e) {
            System.err.println("Failed to load image: " + resourcePath);
            e.printStackTrace();
        }
    }
}