package Client_Java.admin.view;

import Client_Java.admin.AdminClient_Java;
import Client_Java.admin.controller.AdminMainMenuPageController;
import Client_Java.admin.model.AdminClientModel;
import Client_Java.admin.model.AdminMainMenuPageModel;
import Client_Java.admin.view.modals.EditConfigConfirmationPopupView;
import Shared_Files.AdminAccount;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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

public class AdminEditConfigurationsPageView {

    @FXML
    private Button decrementRLButton;

    @FXML
    private Button decrementWTButton;

    @FXML
    private Button incrementRLButton;

    @FXML
    private Button incrementWTButton;

    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    @FXML
    private TextField roundLengthLabel;

    @FXML
    private TextField waitingTimeLabel;

    @FXML
    private Label roundLengthNoticeLabel;

    @FXML
    private Label waitingTimeNoticeLabel;

    @FXML
    private Text titleLabel;

    @FXML
    private Text waitingTitleLabel;

    @FXML
    private Text roundTitleLabel;

    @FXML
    private Text noteLabel;

    @FXML
    private ImageView backgroundImageView;

    private Font quickPencil;
    private Font maryKate;
    private Font pencilant;
    private final String PENCILANT_FONT_PATH = "/css/fonts/Pencilant Script.ttf";
    private final String MARYKATE_FONT_PATH = "/css/fonts/FontsFree-Net-Marykate-Regular.ttf";
    private final String QUICKPENCIL_FONT_PATH = "/css/fonts/QuickPencilRegular-0R59.ttf";

    @FXML
    private void initialize() {
        loadImage(backgroundImageView, "/images/testUI/admin/scroll.png");
        loadCustomFonts();
        applyFonts();
        System.out.println("AdminEditConfigurationsPageView initialized");
    }

    private void loadCustomFonts() {
        try {
            // Load fonts to Font objects
            quickPencil = Font.loadFont(getClass().getResourceAsStream(QUICKPENCIL_FONT_PATH), 10);
            maryKate = Font.loadFont(getClass().getResourceAsStream(MARYKATE_FONT_PATH), 30);
            pencilant = Font.loadFont(getClass().getResourceAsStream(PENCILANT_FONT_PATH), 30);

            // Fallbacks when the loading should fail
            if (quickPencil == null) {
                System.err.println("QuickPencil font not loaded. Using system font.");
                quickPencil = Font.font("System", 12);
            }
            if (maryKate == null) {
                System.err.println("MaryKate font not loaded. Using system font.");
                maryKate = Font.font("System", 12);
            }
            if (pencilant == null) {
                System.err.println("Pencilant font not loaded. Using system font.");
                pencilant = Font.font("System", 12);
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Font loading exception: " + e.getMessage());
            quickPencil = Font.font("System", 12);
            maryKate = Font.font("System", 12);
        }
    }

    private void applyFonts() {
        // Font application to fields and labels
        if (titleLabel != null) {
            titleLabel.setFont(Font.font(maryKate.getFamily(), 60));
        }
        if (waitingTitleLabel != null) {
            waitingTitleLabel.setFont(Font.font(quickPencil.getFamily(), 48));
        }
        if (roundTitleLabel != null) {
            roundTitleLabel.setFont(Font.font(quickPencil.getFamily(), 48));
        }
        if (decrementWTButton != null) {
            decrementWTButton.setFont(Font.font(maryKate.getFamily(), 30));
        }
        if (incrementWTButton != null) {
            incrementWTButton.setFont(Font.font(maryKate.getFamily(), 30));
        }
        if (decrementRLButton != null) {
            decrementRLButton.setFont(Font.font(maryKate.getFamily(), 30));
        }
        if (incrementRLButton != null) {
            incrementRLButton.setFont(Font.font(maryKate.getFamily(), 30));
        }
        if (noteLabel != null) {
            noteLabel.setFont(Font.font(quickPencil.getFamily(), 22));
        }

        if (waitingTimeLabel != null) {
            waitingTimeLabel.setFont(Font.font(pencilant.getFamily(), 35));
        }

        if (roundLengthLabel != null) {
            roundLengthLabel.setFont(Font.font(pencilant.getFamily(), 35));
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

    // Round Length Buttons
    public void setActionDecrementRLButton(EventHandler<ActionEvent> event) {
        if (decrementRLButton != null) {
            decrementRLButton.setOnAction(event);
        } else {
            System.err.println("decrementRLButton is null");
        }
    }

    public void setActionIncrementRLButton(EventHandler<ActionEvent> event) {
        if (incrementRLButton != null) {
            incrementRLButton.setOnAction(event);
        } else {
            System.err.println("incrementRLButton is null");
        }
    }

    // Waiting Time Buttons
    public void setActionDecrementWTButton(EventHandler<ActionEvent> event) {
        if (decrementWTButton != null) {
            decrementWTButton.setOnAction(event);
        } else {
            System.err.println("decrementWTButton is null");
        }
    }

    public void setActionIncrementWTButton(EventHandler<ActionEvent> event) {
        if (incrementWTButton != null) {
            incrementWTButton.setOnAction(event);
        } else {
            System.err.println("incrementWTButton is null");
        }
    }

    // Save Button
    public void setActionSaveButton(EventHandler<ActionEvent> event) {
        if (saveButton != null) {
            saveButton.setOnAction(e -> {
                System.out.println("Save button clicked in view");
                event.handle(e);
            });
        } else {
            System.err.println("saveButton is null");
        }
    }

    // Getters
    public Label getRoundLengthNoticeLabel() {
        return roundLengthNoticeLabel;
    }

    public TextField getRoundLengthLabel() {
        return roundLengthLabel;
    }

    public Label getWaitingTimeNoticeLabel() {
        return waitingTimeNoticeLabel;
    }

    public TextField getWaitingTimeLabel() {
        return waitingTimeLabel;
    }

    // Setters
    public void setRoundLengthLabel(TextField roundLengthLabel) {
        this.roundLengthLabel = roundLengthLabel;
    }

    public void setRoundLengthNoticeLabel(Label roundLengthNoticeLabel) {
        this.roundLengthNoticeLabel = roundLengthNoticeLabel;
    }

    public void setWaitingTimeLabel(TextField waitingTimeLabel) {
        this.waitingTimeLabel = waitingTimeLabel;
    }

    public void setWaitingTimeNoticeLabel(Label waitingTimeNoticeLabel) {
        this.waitingTimeNoticeLabel = waitingTimeNoticeLabel;
    }

    public boolean showConfirmationPopup(String message) {
        try {
            String fxmlPath = "/fxml/admin/EditConfigConfirmationPopup.fxml";
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new IOException("FXML resource not found: " + fxmlPath);
            }
            System.out.println("[DEBUG] Loading FXML: " + fxmlUrl);

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Confirm Configuration Save");
            dialogStage.setScene(new Scene(loader.load()));

            EditConfigConfirmationPopupView controller = loader.getController();
            if (controller == null) {
                throw new IOException("Controller not initialized for EditConfigConfirmationPopup.fxml");
            }

            controller.setConfirmationMessage(message);

            dialogStage.showAndWait();

            return controller.isConfirmed();
        } catch (IOException e) {
            setNoticeLabelText("Error displaying confirmation popup: " + e.getMessage());
            setNoticeVisible(true);
            System.err.println("[ERROR] Failed to load EditConfigConfirmationPopup.fxml: " + e.getMessage());
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

    private void setNoticeLabelText(String message) {
        if (waitingTimeNoticeLabel != null) {
            waitingTimeNoticeLabel.setText(message);
        }
        if (roundLengthNoticeLabel != null) {
            roundLengthNoticeLabel.setText(message);
        }
    }

    private void setNoticeVisible(boolean visible) {
        if (waitingTimeNoticeLabel != null) {
            waitingTimeNoticeLabel.setVisible(visible);
        }
        if (roundLengthNoticeLabel != null) {
            roundLengthNoticeLabel.setVisible(visible);
        }
    }
}