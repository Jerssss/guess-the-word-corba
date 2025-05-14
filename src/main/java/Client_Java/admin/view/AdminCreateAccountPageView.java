package Client_Java.admin.view;

import Client_Java.admin.AdminClient_Java;
import Client_Java.admin.controller.AdminMainMenuPageController;
import Client_Java.admin.AdminClient_Model;
import Client_Java.admin.model.AdminMainMenuPageModel;
import Client_Java.admin.view.modals.CreatePlayerConfirmationPopupView;
import Shared_Files.AdminAccount;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class AdminCreateAccountPageView {

    @FXML
    private TextField fullNameTextField;

    @FXML
    private TextField passwordTextField;

    @FXML
    private Button returnButton;
    @FXML
    private Button saveButton;

    @FXML
    private TextField usernameTextField;

    @FXML
    private Label noticeLabel;

    @FXML
    private Label titleLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label fullNameLabel;

    @FXML
    private Label passwordLabel;


    @FXML private ImageView backgroundImageView;

    private Font pencilant;
    private final String PENCILANT_FONT_PATH = "/css/fonts/Pencilant Script.ttf";

    public void initialize(){
        loadImage(backgroundImageView, "/images/testUI/admin/create--player-view1.png");
        loadCustomFonts();
        applyFonts();
        setupButtonHoverEffects(); // Add this line
    }

    private void setupButtonHoverEffects() {
        if (returnButton != null) {
            returnButton.setOnMouseEntered(e -> returnButtonHovered());
            returnButton.setOnMouseExited(e -> returnButtonExited());
        }
        if (saveButton != null) {
            saveButton.setOnMouseEntered(e -> saveButtonHovered());
            saveButton.setOnMouseExited(e -> saveButtonExited());
        }
    }

    // Animation methods for returnButton
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

    // Animation methods for saveButton
    public void saveButtonHovered() {
        ScaleTransition st = new ScaleTransition(Duration.millis(200), saveButton);
        st.setToX(0.9);
        st.setToY(0.9);
        st.play();
    }

    public void saveButtonExited() {
        ScaleTransition st = new ScaleTransition(Duration.millis(200), saveButton);
        st.setToX(1.0);
        st.setToY(1.0);
        st.play();
    }

    public boolean showConfirmationPopup(String message, String playerName) {
        try {
            // Create the overlay
            Rectangle overlay = new Rectangle();
            overlay.setFill(Color.rgb(0, 0, 0, 0.5)); // Semi-transparent black
            overlay.setWidth(1146); // parent interface width
            overlay.setHeight(763); // parent interface height

            // Root node of parent interface
            StackPane mainRoot = (StackPane) backgroundImageView.getParent();

            // Add overlay to parent interface (temporarily)
            mainRoot.getChildren().add(overlay);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/CreatePlayerConfirmationPopup.fxml"));
            Parent root = loader.load();

            // Create container with parchment-like styling
            StackPane container = new StackPane(root);
            container.setBackground(new Background(new BackgroundFill(
                    Color.web("#F5F5DC"),
                    new CornerRadii(12),
                    Insets.EMPTY)));

            // Remove any conflicting borders from child nodes
            root.setStyle("-fx-border-width: 0; -fx-background-radius: 12;");

            // Single subtle border
            container.setBorder(new Border(new BorderStroke(
                    Color.web("#8B4513", 0.3),  // 30% opacity antique brown
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(12),
                    new BorderWidths(0.75))));   // Ultra-thin border

            // Vintage shadow effect
            container.setEffect(new DropShadow(8, Color.rgb(0, 0, 0, 0.15)));

            // Padding
            StackPane.setMargin(root, new Insets(12));


            Scene scene = new Scene(container);
            scene.setFill(Color.TRANSPARENT);

            Stage dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.TRANSPARENT);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(scene);

            CreatePlayerConfirmationPopupView controller = loader.getController();
            if (controller == null) {
                throw new IOException("Controller not initialized for CreatePlayerConfirmationPopup.fxml");
            }

            controller.setConfirmationMessage(message);
            controller.setPlayerName(playerName);

            // Remove overlay when popup is closed
            dialogStage.setOnHidden(e -> {
                mainRoot.getChildren().remove(overlay);
            });

            dialogStage.showAndWait();

            return controller.isConfirmed();
        } catch (IOException e) {
            setNoticeLabelText("Error displaying confirmation popup: " + e.getMessage());
            setNoticeVisible(true);
            System.err.println("[ERROR] Failed to load CreatePlayerConfirmationPopup.fxml: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
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
                    AdminClient_Model.adminService,
                    AdminClient_Java.getSessionToken(),
                    admin.getAdmin_id()
            );

            new AdminMainMenuPageController(
                    pageView,
                    pageModel,
                    AdminClient_Model.adminService,
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
            setNoticeLabelText("Failed to load Admin Main Menu: " + e.getMessage());
            setNoticeVisible(true);
            System.err.println("[ERROR] IOException while loading AdminMainMenuPage: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            setNoticeLabelText("Unexpected error loading Admin Main Menu.");
            setNoticeVisible(true);
            System.err.println("[ERROR] Unexpected exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadCustomFonts() {
        try {
            // Load fonts to Font objects
            pencilant = Font.loadFont(getClass().getResourceAsStream(PENCILANT_FONT_PATH), 10);

            // Fallbacks when the loading should fail
            if (pencilant == null) {
                System.err.println("Pencilant font not loaded. Using system font.");
                pencilant = Font.font("System", 12);
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Font loading exception: " + e.getMessage());
            pencilant = Font.font("System", 12);
        }
    }

    private void applyFonts() {
        // Font application to fields and labels
        if (fullNameTextField != null) {
            fullNameTextField.setFont(Font.font(pencilant.getFamily(), 29));
        }
        if (usernameTextField != null) {
            usernameTextField.setFont(Font.font(pencilant.getFamily(), 29));
        }
        if (passwordTextField != null) {
            passwordTextField.setFont(Font.font(pencilant.getFamily(), 29));
        }
        if (titleLabel != null) {
            titleLabel.setFont(Font.font(pencilant.getFamily(), 31));
        }

        if (usernameLabel != null) {
            usernameLabel.setFont(Font.font(pencilant.getFamily(), 31));
        }
        if (fullNameLabel != null) {
            fullNameLabel.setFont(Font.font(pencilant.getFamily(), 31));
        }
        if (passwordLabel != null) {
            passwordLabel.setFont(Font.font(pencilant.getFamily(), 31));
        }

        if (noticeLabel != null) {
            noticeLabel.setFont(Font.font(pencilant.getFamily(), 31));
        }
        if (returnButton != null) {
            returnButton.setFont(Font.font(pencilant.getFamily(), 31));
        }
        if (saveButton != null) {
            saveButton.setFont(Font.font(pencilant.getFamily(), 31));
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

    public void setActionReturnButton(EventHandler<ActionEvent> event) {
        returnButton.setOnAction(event);
    }

    public void setActionSaveButton(EventHandler<ActionEvent> event) {
        saveButton.setOnAction(event);
    }

    public Label getNoticeLabel() {
        return noticeLabel;
    }

    public TextField getFullnameTextField() {
        return fullNameTextField;
    }

    public TextField getPasswordTextField() {
        return passwordTextField;
    }

    public TextField getUsernameTextField() {
        return usernameTextField;
    }

    public void setNoticeLabelText(String message) {
        noticeLabel.setText(message);
    }

    public void setNoticeVisible(boolean visible) {
        noticeLabel.setVisible(visible);
    }

    public void handleClearInputFields() {
        fullNameTextField.clear();
        usernameTextField.clear();
        passwordTextField.clear();
    }


}