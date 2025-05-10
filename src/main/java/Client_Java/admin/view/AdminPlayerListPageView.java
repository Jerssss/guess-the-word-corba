package Client_Java.admin.view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import Shared_Files.PlayerAccount;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.InputStream;

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
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        passwordColumn.setCellValueFactory(new PropertyValueFactory<>("password"));
        totalGamesColumn.setCellValueFactory(new PropertyValueFactory<>("gameWins"));
        // You can map totalPointsColumn if PlayerAccount has a points field later.

        // Edit and delete will require custom cell factories for button actions.
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
