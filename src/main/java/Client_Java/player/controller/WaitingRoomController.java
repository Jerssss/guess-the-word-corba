package Client_Java.player.controller;

import Client_Java.PlayerClient_Java;
import Client_Java.player.model.GameLobbyModel;
import Client_Java.player.model.WaitingRoomModel;
import Client_Java.player.view.GameLobbyPageView;
import Client_Java.player.view.WaitingRoomSectionView;
import PlayerGame.GameTimeoutException;
import PlayerGame.NotLoggedInException;
import PlayerGame.Player;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class WaitingRoomController {
    private final WaitingRoomModel model;
    private final WaitingRoomSectionView view;
    private static int remainingTime;
    private int playerCount;

    public WaitingRoomController(WaitingRoomModel model, WaitingRoomSectionView view, Player player) {
        this.model = model;
        this.view = view;

        this.view.setActionCancelButton(event -> handleCancelButtonAction(player));

        // Start the countdown and handle the first round immediately
        initiateCountdown();
        handleFirstRound();
    }

    private void handleCancelButtonAction(Player player) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_Java/player/res/fxml/WWGameLobbyPage.fxml"));
            Parent root = loader.load();

            // Get the view and controller
            GameLobbyPageView lobbyView = loader.getController();
            GameLobbyModel lobbyModel = new GameLobbyModel(player);
            int gid = 0; // Replace with actual gid if available
            new GameLobbyPageController(lobbyModel, lobbyView, player, gid, PlayerClient_Java.getSessionToken());

            // Create scene with stylesheet
            Scene lobbyScene = new Scene(root);
            loadStylesheet(lobbyScene);

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

    private void initiateCountdown() {
        Thread countdownThread = new Thread(() -> {
            try {
                while (true) {
                    remainingTime = model.getRemainingWaitingTime();
                    playerCount = model.getNumberOfPlayersWaiting();

                    Platform.runLater(() -> {
                        view.setRemainingTime(remainingTime);
                        view.setWaitingPlayersCount(playerCount);
                    });
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (GameTimeoutException | NotLoggedInException e) {
                throw new RuntimeException(e);
            }
        });
        countdownThread.setDaemon(true);
        countdownThread.start();
    }

    private void handleFirstRound() {
        Timer delayTimer = new Timer();
        delayTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (playerCount > 1) {
                    Platform.runLater(() -> {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_Java/player/res/fxml/WWGameRoomPage.fxml"));
                            Parent root = loader.load();

                            // Ensure the controller is properly initialized
//                            GameRoomPageView gameRoomView = loader.getController();
//                            GameRoomPageModel gameRoomModel = new GameRoomPageModel(model.getCurrentWord()); // Ensure this method exists
//                            new GameRoomPageController(gameRoomView, gameRoomModel, model.getPlayerID(), PlayerClient_Java.getSessionToken()); // Pass playerID and sessionToken

                            Scene gameRoomScene = new Scene(root);
                            Stage stage = PlayerClient_Java.getStage();
                            stage.setScene(gameRoomScene);
                            stage.setTitle("What's the Word - Game Room");
                            stage.show();
                        } catch (IOException e) {
                            System.err.println("[ERROR] Failed to load Game Room FXML.");
                            e.printStackTrace();
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        // Handle case where not enough players are present
                        System.out.println("Not enough players to start the game.");
                    });
                }
            }
        }, 1000);
    }

    private void loadStylesheet(Scene scene) {
        try {
            URL stylesheet = getClass().getResource("/Client_Java/player/res/css/styles.css");
            if (stylesheet != null) {
                scene.getStylesheets().add(stylesheet.toExternalForm());
            } else {
                System.out.println("Stylesheet not found, using default styling");
            }
        } catch (Exception e) {
            System.err.println("Error loading stylesheet: " + e.getMessage());
        }
    }
}