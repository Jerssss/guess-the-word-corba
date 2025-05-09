package Client_Java.admin.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.io.InputStream;

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
    private StackPane pane;

    @FXML
    private ImageView quillGlow;

    @FXML
    private ImageView mirrorGlow;

    @FXML
    private ImageView doorGlow;

    @FXML
    private ImageView shelfGlow;

    public void initialize() {
        System.out.println("[DEBUG] Initializing Admin Main Menu View...");

        // Load glow images (ensure paths match your project structure)
        loadImage(quillGlow, "/images/testUI/admin/pen_and_paper.png");
        quillGlow.setVisible(false);

        loadImage(mirrorGlow, "/images/testUI/admin/mirror.png");
        mirrorGlow.setVisible(false);

        loadImage(doorGlow, "/images/testUI/admin/door.png");
        doorGlow.setVisible(false);

        loadImage(shelfGlow, "/images/testUI/admin/shelf.png");
        shelfGlow.setVisible(false);

        // Setup hover effects for buttons
        setupHoverEffects();
    }

    private void loadImage(ImageView imageView, String resourcePath) {
        try {
            // Try loading from resources
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is != null) {
                imageView.setImage(new Image(is));
                System.out.println("[DEBUG] Loaded image: " + resourcePath);
            } else {
                // Fallback to absolute path (adjust as needed)
                String absPath = "file:src/main/resources" + resourcePath;
                imageView.setImage(new Image(absPath));
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to load image: " + resourcePath);
            e.printStackTrace();
        }
    }

    private void setupHoverEffects() {
        // Create Account Button hover
        createAccountButton.setOnMouseEntered(e -> quillGlow.setVisible(true));
        createAccountButton.setOnMouseExited(e -> quillGlow.setVisible(false));

        // View Players Button hover
        viewPlayersButton.setOnMouseEntered(e -> mirrorGlow.setVisible(true));
        viewPlayersButton.setOnMouseExited(e -> mirrorGlow.setVisible(false));

        // Quit Button hover
        quitButton.setOnMouseEntered(e -> doorGlow.setVisible(true));
        quitButton.setOnMouseExited(e -> doorGlow.setVisible(false));

        // Edit Config Button hover
        editConfigButton.setOnMouseEntered(e -> shelfGlow.setVisible(true));
        editConfigButton.setOnMouseExited(e -> shelfGlow.setVisible(false));
    }

    public Button getQuitButton() {
        return quitButton;
    }

    public StackPane getPane() {
        return pane;
    }

    public void setActionCreateAccountButton(EventHandler<ActionEvent> event) {
        if (createAccountButton == null) {
            System.err.println("[ERROR] createAccountButton is NULL!");
        } else {
            createAccountButton.setOnAction(event);
            System.out.println("[DEBUG] Create Account button bound with event handler.");
        }
    }

    public void setActionEditConfigButton(EventHandler<ActionEvent> event) {
        if (editConfigButton == null) {
            System.err.println("[ERROR] editConfigButton is NULL!");
        } else {
            editConfigButton.setOnAction(event);
            System.out.println("[DEBUG] Edit Config button bound with event handler.");
        }
    }

    public void setActionViewPlayersButton(EventHandler<ActionEvent> event) {
        if (viewPlayersButton == null) {
            System.err.println("[ERROR] viewPlayersButton is NULL!");
        } else {
            viewPlayersButton.setOnAction(event);
            System.out.println("[DEBUG] View Players button bound with event handler.");
        }
    }

    public void setActionQuitButton(EventHandler<ActionEvent> event) {
        if (quitButton == null) {
            System.err.println("[ERROR] quitButton is NULL!");
        } else {
            quitButton.setOnAction(event);
            System.out.println("[DEBUG] Quit button bound with event handler.");
        }
    }
}