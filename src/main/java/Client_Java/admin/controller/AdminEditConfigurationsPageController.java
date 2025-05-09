package Client_Java.admin.controller;

import Client_Java.admin.AdminClient_Java;
import Client_Java.admin.model.AdminClientModel;
import Client_Java.admin.model.AdminEditConfigurationsPageModel;
import Client_Java.admin.model.AdminMainMenuPageModel;
import Client_Java.admin.view.AdminEditConfigurationsPageView;
import AdminIDL.NotLoggedInException;
import Client_Java.admin.view.AdminMainMenuPageView;
import Shared_Files.AdminAccount;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class AdminEditConfigurationsPageController {
    private final AdminEditConfigurationsPageView view;
    private final AdminEditConfigurationsPageModel model;

    public AdminEditConfigurationsPageController(AdminEditConfigurationsPageView view, AdminEditConfigurationsPageModel model) {
        this.view = view;
        this.model = model;
        attachEventHandlers();
    }

    private void attachEventHandlers() {
        view.setActionIncrementRLButton(e -> adjustRoundLength(1));
        view.setActionDecrementRLwButton(e -> adjustRoundLength(-1));
        view.setActionIncrementWTButton(e -> adjustWaitingTime(1));
        view.setActionDecrementWTButton(e -> adjustWaitingTime(-1));
        view.setActionSaveButton(e -> handleSave());
    }

    private void adjustWaitingTime(int delta) {
        TextField waitingTimeField = view.getWaitingTimeLabel();
        Label waitingTimeNoticeLabel = view.getWaitingTimeNoticeLabel();
        try {
            int current = Integer.parseInt(waitingTimeField.getText());
            int newValue = current + delta;
            if (newValue >= 5 && newValue <= 60) {
                waitingTimeField.setText(String.valueOf(newValue));
                waitingTimeNoticeLabel.setVisible(false);
            } else {
                waitingTimeNoticeLabel.setText("Waiting time must be 5-60 seconds");
                waitingTimeNoticeLabel.setStyle("-fx-text-fill: red;");
                waitingTimeNoticeLabel.setVisible(true);
            }
        } catch (NumberFormatException e) {
            waitingTimeNoticeLabel.setText("Please enter a valid number");
            waitingTimeNoticeLabel.setStyle("-fx-text-fill: red;");
            waitingTimeNoticeLabel.setVisible(true);
        }
    }

    private void adjustRoundLength(int delta) {
        TextField roundLengthField = view.getRoundLengthLabel();
        Label roundLengthNoticeLabel = view.getRoundLengthNoticeLabel();
        try {
            int current = Integer.parseInt(roundLengthField.getText());
            int newValue = current + delta;
            if (newValue >= 10 && newValue <= 300) {
                roundLengthField.setText(String.valueOf(newValue));
                roundLengthNoticeLabel.setVisible(false);
            } else {
                roundLengthNoticeLabel.setText("Round duration must be 10-300 seconds");
                roundLengthNoticeLabel.setStyle("-fx-text-fill: red;");
                roundLengthNoticeLabel.setVisible(true);
            }
        } catch (NumberFormatException e) {
            roundLengthNoticeLabel.setText("Please enter a valid number");
            roundLengthNoticeLabel.setStyle("-fx-text-fill: red;");
            roundLengthNoticeLabel.setVisible(true);
        }
    }

    private void handleSave() {
        TextField roundLengthField = view.getRoundLengthLabel();
        TextField waitingTimeField = view.getWaitingTimeLabel();
        Label roundLengthNoticeLabel = view.getRoundLengthNoticeLabel();
        Label waitingTimeNoticeLabel = view.getWaitingTimeNoticeLabel();

        if (roundLengthField == null || waitingTimeField == null ||
                roundLengthNoticeLabel == null || waitingTimeNoticeLabel == null) {
            System.err.println("UI component is null: " +
                    "roundLengthField=" + roundLengthField +
                    ", waitingTimeField=" + waitingTimeField +
                    ", roundLengthNoticeLabel=" + roundLengthNoticeLabel +
                    ", waitingTimeNoticeLabel=" + waitingTimeNoticeLabel);
            if (roundLengthNoticeLabel != null) {
                roundLengthNoticeLabel.setText("Error: UI components not initialized.");
                roundLengthNoticeLabel.setStyle("-fx-text-fill: red;");
                roundLengthNoticeLabel.setVisible(true);
            }
            if (waitingTimeNoticeLabel != null) {
                waitingTimeNoticeLabel.setText("Error: UI components not initialized.");
                waitingTimeNoticeLabel.setStyle("-fx-text-fill: red;");
                waitingTimeNoticeLabel.setVisible(true);
            }
            return;
        }

        try {
            int roundTime = Integer.parseInt(roundLengthField.getText());
            int waitTime = Integer.parseInt(waitingTimeField.getText());

            if (waitTime < 5 || waitTime > 60) {
                waitingTimeNoticeLabel.setText("Waiting time must be 5-60 seconds");
                waitingTimeNoticeLabel.setStyle("-fx-text-fill: red;");
                waitingTimeNoticeLabel.setVisible(true);
                return;
            }

            if (roundTime < 10 || roundTime > 300) {
                roundLengthNoticeLabel.setText("Round duration must be 10-300 seconds");
                roundLengthNoticeLabel.setStyle("-fx-text-fill: red;");
                roundLengthNoticeLabel.setVisible(true);
                return;
            }

            model.modifyWaitingTime(waitTime);
            model.modifyRoundDuration(roundTime);

            waitingTimeNoticeLabel.setText("Waiting time updated successfully.");
            waitingTimeNoticeLabel.setStyle("-fx-text-fill: green;");
            waitingTimeNoticeLabel.setVisible(true);

            roundLengthNoticeLabel.setText("Round duration updated successfully.");
            roundLengthNoticeLabel.setStyle("-fx-text-fill: green;");
            roundLengthNoticeLabel.setVisible(true);

            // Delay for 5 seconds before redirecting
            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(event -> redirectToAdminMainMenu(AdminClient_Java.getLoggidInAdmin()));
            delay.play();

        } catch (NumberFormatException e) {
            waitingTimeNoticeLabel.setText("Invalid input: must be numeric.");
            waitingTimeNoticeLabel.setStyle("-fx-text-fill: red;");
            waitingTimeNoticeLabel.setVisible(true);
            roundLengthNoticeLabel.setText("Invalid input: must be numeric.");
            roundLengthNoticeLabel.setStyle("-fx-text-fill: red;");
            roundLengthNoticeLabel.setVisible(true);
        } catch (NotLoggedInException e) {
            waitingTimeNoticeLabel.setText("Error: Not logged in.");
            waitingTimeNoticeLabel.setStyle("-fx-text-fill: red;");
            waitingTimeNoticeLabel.setVisible(true);
            roundLengthNoticeLabel.setText("Error: Not logged in.");
            roundLengthNoticeLabel.setStyle("-fx-text-fill: red;");
            roundLengthNoticeLabel.setVisible(true);
        } catch (Exception e) {
            waitingTimeNoticeLabel.setText("Error: " + e.getMessage());
            waitingTimeNoticeLabel.setStyle("-fx-text-fill: red;");
            waitingTimeNoticeLabel.setVisible(true);
            roundLengthNoticeLabel.setText("Error: " + e.getMessage());
            roundLengthNoticeLabel.setStyle("-fx-text-fill: red;");
            roundLengthNoticeLabel.setVisible(true);
        }
    }


    private void redirectToAdminMainMenu(AdminAccount admin) {
        try {
            File fxmlFile = new File("src/main/resources/fxml/admin/AdminMainMenuPage.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
            Parent root = loader.load();

            AdminMainMenuPageView pageView = loader.getController();
            if (pageView == null) {
                System.err.println("[ERROR] AdminMainMenuPageView is NULL after FXML load!");
                return;
            } else {
                System.out.println("[DEBUG] AdminMainMenuPageView loaded successfully.");
            }

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

            Scene scene = new Scene(root);
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
}