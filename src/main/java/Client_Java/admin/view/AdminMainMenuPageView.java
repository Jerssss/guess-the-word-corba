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

    @FXML
    private ImageView quitText;

    @FXML
    private ImageView editConfigText;

    @FXML
    private ImageView showPlayersText;

    @FXML
    private ImageView createPlayerText;

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

        // Load and initialize text images
        loadImage(quitText, "/images/testUI/admin/quit.png");
        quitText.setVisible(false);

        loadImage(editConfigText, "/images/testUI/admin/edit_config.png");
        editConfigText.setVisible(false);

        loadImage(showPlayersText, "/images/testUI/admin/show_players.png");
        showPlayersText.setVisible(false);

        loadImage(createPlayerText, "/images/testUI/admin/create_player.png");
        createPlayerText.setVisible(false);

        // Setup hover effects for buttons
        setupHoverEffects();
    }

    private void loadImage(ImageView imageView, String resourcePath) {
        try {
            // Try loading from resources
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is != null) {
                imageView.setImage(new Image(is));
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
        createAccountButton.setOnMouseEntered(e -> {
            quillGlow.setVisible(true);
            createPlayerText.setVisible(true); // Show text on hover
        });
        createAccountButton.setOnMouseExited(e -> {
            quillGlow.setVisible(false);
            createPlayerText.setVisible(false); // Hide text when not hovering
        });

        // View Players Button hover
        viewPlayersButton.setOnMouseEntered(e -> {
            mirrorGlow.setVisible(true);
            showPlayersText.setVisible(true); // Show text on hover
        });
        viewPlayersButton.setOnMouseExited(e -> {
            mirrorGlow.setVisible(false);
            showPlayersText.setVisible(false); // Hide text when not hovering
        });

        // Quit Button hover
        quitButton.setOnMouseEntered(e -> {
            doorGlow.setVisible(true);
            quitText.setVisible(true); // Show text on hover
        });
        quitButton.setOnMouseExited(e -> {
            doorGlow.setVisible(false);
            quitText.setVisible(false); // Hide text when not hovering
        });

        // Edit Config Button hover
        editConfigButton.setOnMouseEntered(e -> {
            shelfGlow.setVisible(true);
            editConfigText.setVisible(true); // Show text on hover
        });
        editConfigButton.setOnMouseExited(e -> {
            shelfGlow.setVisible(false);
            editConfigText.setVisible(false); // Hide text when not hovering
        });
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
        }
    }

    public void setActionEditConfigButton(EventHandler<ActionEvent> event) {
        if (editConfigButton == null) {
            System.err.println("[ERROR] editConfigButton is NULL!");
        } else {
            editConfigButton.setOnAction(event);
        }
    }

    public void setActionViewPlayersButton(EventHandler<ActionEvent> event) {
        if (viewPlayersButton == null) {
            System.err.println("[ERROR] viewPlayersButton is NULL!");
        } else {
            viewPlayersButton.setOnAction(event);
        }
    }

    public void setActionQuitButton(EventHandler<ActionEvent> event) {
        if (quitButton == null) {
            System.err.println("[ERROR] quitButton is NULL!");
        } else {
            quitButton.setOnAction(event);
        }
    }
}