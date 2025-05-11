package Client_Java.player.view;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;

import java.io.InputStream;

public class AboutPageView {
    @FXML private ImageView backgroundImageView;
    @FXML private Button returnButton;
    @FXML private Label titleLabel;
    @FXML private ScrollPane aboutScrollPane;
    @FXML private ScrollPane htpScrollPane;
    @FXML private ScrollPane rulesScrollPane;
    @FXML private AnchorPane aboutAnchorPane;
    @FXML private AnchorPane htpAnchorPane;
    @FXML private AnchorPane rulesAnchorPane;
    @FXML private AnchorPane aboutParentAnchorPane;
    @FXML private AnchorPane htpParentAnchorPane;
    @FXML private AnchorPane rulesParentAnchorPane;
    @FXML private TabPane tabPane;

    private static final String ABOUT_IMAGE = "/images/testUI/red_about_crystal.png";
    private static final String ABOUT_SELECTED_IMAGE = "/images/testUI/red_about_crystal_selected.png";
    private static final String HTP_IMAGE = "/images/testUI/pink_about_crystal.png";
    private static final String HTP_SELECTED_IMAGE = "/images/testUI/pink_about_crystal_selected.png";
    private static final String RULES_IMAGE = "/images/testUI/purple_about_crystal.png";
    private static final String RULES_SELECTED_IMAGE = "/images/testUI/purple_about_crystal_selected.png";

    private Runnable returnAction;

    public void initialize() {
        verifyResources();
        setupTabs();
        setupReturnButton();
        setupScrollPanes();
    }

    private void verifyResources() {
        checkImage(ABOUT_IMAGE);
        checkImage(ABOUT_SELECTED_IMAGE);
        checkImage(HTP_IMAGE);
        checkImage(HTP_SELECTED_IMAGE);
        checkImage(RULES_IMAGE);
        checkImage(RULES_SELECTED_IMAGE);
    }

    private void checkImage(String path) {
        try {
            if (getClass().getResource(path) == null) {
                System.err.println("Image resource not found: " + path);
            } else {
                System.out.println("Image resource found: " + path);
            }
        } catch (Exception e) {
            System.err.println("Error checking image resource: " + path);
            e.printStackTrace();
        }
    }

    private void setupTabs() {
        if (tabPane == null) {
            System.err.println("TabPane is null - add fx:id=\"tabPane\" to your FXML");
            return;
        }

        configureTab(0, ABOUT_IMAGE, ABOUT_SELECTED_IMAGE);
        configureTab(1, HTP_IMAGE, HTP_SELECTED_IMAGE);
        configureTab(2, RULES_IMAGE, RULES_SELECTED_IMAGE);

        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (oldTab != null) updateTabGraphic(oldTab, false);
            if (newTab != null) updateTabGraphic(newTab, true);
        });

        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            updateTabGraphic(selectedTab, true);
        }
    }

    private void configureTab(int index, String defaultImage, String selectedImage) {
        if (tabPane.getTabs().size() > index) {
            Tab tab = tabPane.getTabs().get(index);
            tab.getProperties().put("defaultImage", defaultImage);
            tab.getProperties().put("selectedImage", selectedImage);
            setTabGraphic(tab, defaultImage);
        }
    }

    private void setTabGraphic(Tab tab, String imagePath) {
        try {
            InputStream imageStream = getClass().getResourceAsStream(imagePath);
            if (imageStream == null) {
                System.err.println("Failed to load tab image (null stream): " + imagePath);
                // Optionally set a placeholder or skip setting the graphic
                tab.setGraphic(null);
                return;
            }
            Image image = new Image(imageStream);
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(120);
            imageView.setFitHeight(120);
            imageView.setPreserveRatio(true);
            tab.setGraphic(imageView);
            System.out.println("Successfully loaded tab image: " + imagePath);
        } catch (Exception e) {
            System.err.println("Error loading tab image: " + imagePath);
            e.printStackTrace();
            tab.setGraphic(null); // Prevent null pointer issues
        }
    }

    private void updateTabGraphic(Tab tab, boolean selected) {
        String imagePath = selected ?
                (String) tab.getProperties().get("selectedImage") :
                (String) tab.getProperties().get("defaultImage");
        if (imagePath != null) {
            setTabGraphic(tab, imagePath);
        }
    }

    private void setupScrollPanes() {
        makeScrollPaneTransparent(aboutScrollPane);
        makeScrollPaneTransparent(htpScrollPane);
        makeScrollPaneTransparent(rulesScrollPane);
    }

    private void makeScrollPaneTransparent(ScrollPane scrollPane) {
        if (scrollPane != null) {
            scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
            scrollPane.setBackground(Background.EMPTY);
            Node content = scrollPane.getContent();
            if (content instanceof Region) {
                ((Region) content).setBackground(Background.EMPTY);
            }
        }
    }

    private void setupReturnButton() {
        if (returnButton != null) {
            returnButton.setOnAction(event -> {
                if (returnAction != null) {
                    returnAction.run();
                } else {
                    System.out.println("Return action not set");
                }
            });
        } else {
            System.err.println("returnButton is null - check FXML");
        }
    }

    public void setReturnAction(Runnable action) {
        this.returnAction = action;
    }
}