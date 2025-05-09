package Client_Java.player.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.InputStream;

public class GameLobbyView {

    @FXML
    private Button aboutButton;

    @FXML
    private Text currentUserLB;

    @FXML
    private Text currentUserPointsLB;

    @FXML
    private Text currentUserRankLB;

    @FXML
    private Button enterGameButton;

    @FXML
    private Text gamePointsLabelLB;

    @FXML
    private ScrollPane leaderboardScrollPane;

    @FXML
    private FlowPane leaderboardsFlowPane;

    @FXML
    private Text playerNameLabelLB;

    @FXML
    private Button quitButton;

    @FXML
    private Text rankLabelLB;

    @FXML
    private VBox leaderboardContainer;

    @FXML
    private ImageView potionGlow;
    @FXML
    private ImageView bookGlow;
    @FXML
    private ImageView crystalGlow;
    @FXML
    private ImageView lobbyImageView;
    @FXML
    private ImageView enterGameImageLabel;
    @FXML
    private ImageView quitImageLabel;
    @FXML
    private ImageView aboutImageLabel;
    @FXML
    private ImageView candleFlameImage;


    public void initialize() {
        if (enterGameButton == null) {
            System.err.println("[ERROR] enterGameButton is NULL! Check FXML.");
        }
        if (quitButton == null) {
            System.err.println("[ERROR] quitButton is NULL! Check FXML.");
        }

        loadImage(lobbyImageView, "/images/testUI/lobby-shack1.png");
        loadImage(candleFlameImage, "/images/testUI/fire-nobg.gif");


        loadImage(potionGlow, "/images/testUI/hovers/potion_hovered.png");
        potionGlow.setVisible(false);

        loadImage(bookGlow, "/images/testUI/hovers/book_hovered.png");
        bookGlow.setVisible(false);

        loadImage(crystalGlow, "/images/testUI/hovers/crystal_hovered.png");
        crystalGlow.setVisible(false);

        loadImage(enterGameImageLabel, "/images/testUI/hovers/book_hovered_enter_game.png");
        enterGameImageLabel.setVisible(false);

        loadImage(aboutImageLabel, "/images/testUI/hovers/crystal_hovered_about.png");
        aboutImageLabel.setVisible(false);

        loadImage(quitImageLabel, "/images/testUI/hovers/potion_hovered_quit.png");
        quitImageLabel.setVisible(false);

        setupHoverEffects();
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

    private void setupHoverEffects() {
        // Enter Game Button hover
        enterGameButton.setOnMouseEntered(e -> {
            bookGlow.setVisible(true);
            enterGameImageLabel.setVisible(true);
        });
        enterGameButton.setOnMouseExited(e -> {
            bookGlow.setVisible(false);
            enterGameImageLabel.setVisible(false);
        });

        // About Button hover
        aboutButton.setOnMouseEntered(e -> {
            crystalGlow.setVisible(true);
            aboutImageLabel.setVisible(true);
        });
        aboutButton.setOnMouseExited(e -> {
            crystalGlow.setVisible(false);
            aboutImageLabel.setVisible(false);
        });

        // Quit Button hover
        quitButton.setOnMouseEntered(e -> {
            potionGlow.setVisible(true);
            quitImageLabel.setVisible(true);
        });
        quitButton.setOnMouseExited(e -> {
            potionGlow.setVisible(false);
            quitImageLabel.setVisible(false);
        });
    }

    public void setActionEnterGameButton(EventHandler<ActionEvent> event) {
        enterGameButton.setOnAction(event);
    }

    public void setActionQuitButton(EventHandler<ActionEvent> event) {
        if (quitButton == null) {
            System.err.println("[VIEW ERROR] quitButton is NULL!");
        } else {
            quitButton.setOnAction(event);
        }
    }

    private void verifyComponent(String name, Object component) {
        if (component == null) {
            System.err.println("CRITICAL: " + name + " is NULL - FXML injection failed!");
        } else {
            System.out.println(name + " successfully injected");
        }
    }

    public VBox getLeaderboardContainer() {
        return leaderboardContainer;
    }

    public void setActionAboutButton(EventHandler<ActionEvent> event) {
        aboutButton.setOnAction(event);
    }

    public Text getCurrentUserLB() {
        return currentUserLB;
    }

    public Text getCurrentUserPointsLB() {
        return currentUserPointsLB;
    }

    public Text getCurrentUserRankLB() {
        return currentUserRankLB;
    }

    public FlowPane getLeaderboardsFlowPane() {
        return leaderboardsFlowPane;
    }

    public ScrollPane getLeaderboardScrollPane() {
        return leaderboardScrollPane;
    }

    public Text getGamePointsLabelLB() {
        return gamePointsLabelLB;
    }

    public Text getPlayerNameLabelLB() {
        return playerNameLabelLB;
    }

    public Text getRankLabelLB() {
        return rankLabelLB;
    }

}
