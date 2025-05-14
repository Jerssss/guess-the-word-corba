package Client_Java.admin.view;

import Client_Java.admin.AdminClient_Java;
import Client_Java.admin.controller.AdminMainMenuPageController;
import Client_Java.admin.model.AdminClientModel;
import Client_Java.admin.model.AdminMainMenuPageModel;
import Client_Java.admin.view.modals.DeletePlayerConfirmationPopupView;
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
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.StageStyle;
import javafx.scene.effect.DropShadow;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

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
    private Consumer<PlayerAccount> onEditPlayerCallback;
    private Consumer<PlayerAccount> onDeletePlayerCallback;

    private Font maryKate;
    private final String MARYKATE_FONT_PATH = "/css/fonts/bryndan-write.ttf";

    public void initialize() {
        loadImage(backgroundImageView, "/images/testUI/admin/mirror-zoomed-cropped.png");
        loadCustomFonts();
        applyFonts();
        initializeTableColumns();
        playersTable.setPlaceholder(new Label("No players found"));
        setupButtonHoverEffects();
    }

    private void setupButtonHoverEffects() {
        if (returnButton != null) {
            returnButton.setOnMouseEntered(e -> returnButtonHovered());
            returnButton.setOnMouseExited(e -> returnButtonExited());
        }
    }

    public void returnButtonHovered() {
        ScaleTransition st = new ScaleTransition(Duration.millis(200), returnButton);
        st.setToX(0.9);
        st.setToY(0.9);
        st.play();
    }

    public void returnButtonExited() {
        ScaleTransition st = new ScaleTransition(Duration.millis(200), returnButton);
        st.setToX(1.0);
        st.setToY(1.0);
        st.play();
    }

    public void setActionReturnButton(EventHandler<ActionEvent> event) {
        returnButton.setOnAction(event);
    }

    public void setOnEditPlayerCallback(Consumer<PlayerAccount> callback) {
        this.onEditPlayerCallback = callback;
    }

    public void setOnDeletePlayerCallback(Consumer<PlayerAccount> callback) {
        this.onDeletePlayerCallback = callback;
    }

    public void initializeTableColumns() {
        userIdColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPlayerId()));
        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        passwordColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPassword()));
        fullNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        gameWinsColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getGameWins()));
        editColumn.setCellFactory(column -> createEditButtonCellFactory());
        deleteColumn.setCellFactory(column -> createDeleteButtonCellFactory());
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

                modifyButton.setOnMouseEntered(e -> {
                    ScaleTransition st = new ScaleTransition(Duration.millis(200), modifyButton);
                    st.setToX(1.2);
                    st.setToY(1.2);
                    st.play();
                });

                modifyButton.setOnMouseExited(e -> {
                    ScaleTransition st = new ScaleTransition(Duration.millis(200), modifyButton);
                    st.setToX(1.0);
                    st.setToY(1.0);
                    st.play();
                });

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
            Rectangle overlay = new Rectangle();
            overlay.setFill(Color.rgb(0, 0, 0, 0.5));
            overlay.setWidth(1146);
            overlay.setHeight(763);

            StackPane mainRoot = (StackPane) backgroundImageView.getParent();
            mainRoot.getChildren().add(0, overlay);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/EditPlayerPopup.fxml"));
            Parent root = loader.load();

            StackPane container = new StackPane(root);
            container.setBackground(new Background(new BackgroundFill(
                    Color.web("#F5F5DC"),
                    new CornerRadii(12),
                    Insets.EMPTY)));

            root.setStyle("-fx-border-width: 0; -fx-background-radius: 12;");

            container.setBorder(new Border(new BorderStroke(
                    Color.web("#8B4513", 0.3),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(12),
                    new BorderWidths(0.75))));

            container.setEffect(new DropShadow(8, Color.rgb(0, 0, 0, 0.15)));
            StackPane.setMargin(root, new Insets(12));

            Scene scene = new Scene(container);
            scene.setFill(Color.TRANSPARENT);

            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);

            EditPlayerPopupView view = loader.getController();
            view.setPlayer(player);
            view.setOnSaveCallback(updatedPlayer -> {
                if (onEditPlayerCallback != null) {
                    onEditPlayerCallback.accept(updatedPlayer);
                }
            });

            stage.setOnHidden(e -> {
                mainRoot.getChildren().remove(overlay);
            });

            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load EditPlayerPopup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private TableCell<PlayerAccount, String> createDeleteButtonCellFactory() {
        return new TableCell<PlayerAccount, String>() {
            private final Button deleteButton = new Button();

            {
                Image img = new Image(getClass().getResourceAsStream("/images/testUI/admin/delete_icon.png"));
                ImageView iv = new ImageView(img);
                iv.setFitWidth(16);
                iv.setFitHeight(16);
                deleteButton.setGraphic(iv);
                deleteButton.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-padding: 0;" +
                                "-fx-cursor: hand;"
                );

                deleteButton.setOnMouseEntered(e -> {
                    ScaleTransition st = new ScaleTransition(Duration.millis(200), deleteButton);
                    st.setToX(1.2);
                    st.setToY(1.2);
                    st.play();
                });

                deleteButton.setOnMouseExited(e -> {
                    ScaleTransition st = new ScaleTransition(Duration.millis(200), deleteButton);
                    st.setToX(1.0);
                    st.setToY(1.0);
                    st.play();
                });

                deleteButton.setOnAction(event -> {
                    PlayerAccount player = (PlayerAccount) getTableRow().getItem();
                    if (player != null) {
                        boolean confirmed = showDeleteConfirmationPane(player);
                        if (confirmed && onDeletePlayerCallback != null) {
                            onDeletePlayerCallback.accept(player);
                        }
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        };
    }

    private boolean showDeleteConfirmationPane(PlayerAccount player) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/DeletePlayerConfirmationPopup.fxml"));
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Delete Player");
            dialogStage.setScene(new Scene(loader.load()));

            DeletePlayerConfirmationPopupView controller = loader.getController();
            if (controller == null) {
                throw new IOException("Controller not initialized for DeletePlayerConfirmationPopupView.fxml");
            }

            controller.setPlayerName(player.getUsername());

            dialogStage.showAndWait();

            return controller.isConfirmed();
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load DeletePlayerConfirmationPopupView: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void showAdminMainMenu(AdminAccount admin) {
        try {
            File fxmlFile = new File("src/main/resources/fxml/admin/AdminMainMenuPage.fxml");
            if (!fxmlFile.exists()) {
                throw new IOException("FXML file not found: " + fxmlFile.getAbsolutePath());
            }
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

//    public void updateTable(List<PlayerAccount> data) {
//        playerData.setAll(data);
//        playersTable.setItems(null);
//        playersTable.setItems(playerData);
//        playersTable.refresh();
//        System.out.println("[ADMIN CLIENT | " + new Date() + "] Player data updated. New table size: " + playerData.size());
//        /*
//        public void updateTable(List<PlayerAccount> data) {
//    Platform.runLater(() -> {
//        playerData.setAll(data); // Update the source data
//        playersTable.refresh(); // Refresh the UI
//        System.out.println("[ADMIN CLIENT | " + new Date() + "] Player data updated. New table size: " + playerData.size());
//    });
//}
//         */
//    }

    public void updateTable(List<PlayerAccount> data) {
        Platform.runLater(() -> {
            playerData.setAll(data);
            playersTable.refresh();
            System.out.println("[ADMIN CLIENT | " + new Date() + "] Player data updated. Table size: " + playerData.size());
        });
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
                String absPath = "file:src/main/resources" + resourcePath;
                imageView.setImage(new Image(absPath));
            }
        } catch (Exception e) {
            System.err.println("Failed to load image: " + resourcePath);
            e.printStackTrace();
        }
    }
}