package Client_Java.player.controller;

import Client_Java.PlayerClient_Java;
import Client_Java.player.model.GameLobbyModel;
import Client_Java.player.model.WaitingRoomModel;
import Client_Java.player.view.GameLobbyPageView;
import Client_Java.player.view.WaitingRoomSectionView;
import PlayerGame.Player;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class WaitingRoomController {
    private final WaitingRoomModel model;
    private WaitingRoomSectionView view;
    private static int remainingTime;
    private int playerCount;
    public WaitingRoomController(WaitingRoomModel model, WaitingRoomSectionView view, Player player) {
        this.model = model;
        this.view = view;

        this.view.setActionCancelButton(event -> handleCancelButtonAction(player));
    }

    private void handleCancelButtonAction(Player player){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_Java/player/res/fxml/WWGameLobbyPage.fxml"));
            Parent root = loader.load();

            // Get the view and controller
            GameLobbyPageView lobbyView = loader.getController();
            GameLobbyModel lobbyModel = new GameLobbyModel(player);
            // You need to define gid or pass it appropriately
            int gid = 0; // Replace with actual gid if available
            new GameLobbyPageController(lobbyModel, lobbyView, player, gid, PlayerClient_Java.getSessionToken());

            // Create scene with stylesheet
            Scene lobbyScene = new Scene(root);
            // Load stylesheet safely
            try {
                URL stylesheet = getClass().getResource("/Client_Java/player/res/css/styles.css");
                if (stylesheet != null) {
                    lobbyScene.getStylesheets().add(stylesheet.toExternalForm());
                } else {
                    System.out.println("Stylesheet not found, using default styling");
                }
            } catch (Exception e) {
                System.err.println("Error loading stylesheet: " + e.getMessage());
            }

            // Update stage
            Stage stage = PlayerClient_Java.getStage();
            Platform.runLater(() -> {
                stage.setScene(lobbyScene);
                stage.setTitle("What's the Word - Game Lobby");
                stage.show();
            });

        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load Game Lobby FXML.");
            e.printStackTrace();
        }
    }

}
