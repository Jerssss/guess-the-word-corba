package Client_Java.player.controller;

import Client_Java.player.PlayerClient_Model;
import Client_Java.player.PlayerClient_Java;
import Client_Java.player.model.GameRoomModel;
import Client_Java.player.model.WaitingRoomModel;
import Client_Java.player.view.GameRoomView;
import Client_Java.player.view.WaitingRoomView;

import Server_Java.idls.PlayerCallBackIDL.WaitingRoomGameCallbackService;
import Server_Java.idls.PlayerCallBackIDL.GameCallBackService;


import Server_Java.implementation.GameCallbackServiceImpl;
import Server_Java.implementation.WaitingRoomCallbackServiceImpl;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Controller for the waiting room: joins lobby, registers both waiting-room
 * and game-start callbacks, and drives the UI in response to server pushes.
 */
public class WaitingRoomController {
    private final WaitingRoomModel model;
    private final WaitingRoomView view;
    private final int playerId;
    private final String sessionToken;
    private String gameToken;

    private ScheduledExecutorService scheduler;

    private int countdown;
    private final int initialCountdown;
    private final int minimumPlayers;

    @FXML private Label countdownLabel;
    @FXML private Label playerCountLabel;

    public WaitingRoomController(
            WaitingRoomModel model,
            WaitingRoomView view,
            int playerId,
            String sessionToken
    ) {
        this.model        = model;
        this.view         = view;
        this.playerId     = playerId;
        this.sessionToken = sessionToken;

        // fetch server-configured settings up front
        this.initialCountdown = model.getSetting("countdown_to_game_start", sessionToken);
        this.minimumPlayers   = model.getSetting("minimum_players",       sessionToken);
        this.countdown        = initialCountdown;

        initialize();
    }

    private void initialize() {
        // 1) join the lobby
        gameToken = model.joinLobby(playerId, sessionToken);
        if (gameToken == null) {
            System.err.println("[WaitingRoom] Failed to join lobby.");
            return;
        }

        // 2) initialize UI
        int joined = model.getNumberOfPlayersJoined(playerId, sessionToken);
        view.setWaitingPlayersCount(joined);
        view.setRemainingTime(countdown);
        view.setActionCancelButton(this::handleCancel);

        // 3) register waiting-room callback
        WaitingRoomCallbackServiceImpl waitServant =
                new WaitingRoomCallbackServiceImpl(this);
        WaitingRoomGameCallbackService waitStub =
                PlayerClient_Model.registerWaitingRoomCallback(waitServant);
        System.out.println("[WaitingRoomController] registering waiting callback for session=" + sessionToken);
        model.registerWaitingRoomCallback(
                playerId, gameToken, sessionToken, waitStub
        );

        // Register game callbacks properly here
   //     WaitingRoomCallbackServiceImpl callbackServant = new WaitingRoomCallbackServiceImpl(model, sessionToken);
     //   GameCallBackService callbackStub = PlayerClient_Model.registerGameCallback(callbackServant);
   //     model.registerCallback(playerId, gameToken, sessionToken, callbackStub);

    }

    // --- Methods invoked by the WaitingRoomCallbackServiceImpl on the FX thread ---

    public void onPlayerCountUpdate(int totalPlayers) {
        view.setWaitingPlayersCount(totalPlayers);
    }

    public void onCountdownStart(int seconds) {
        this.countdown = seconds;
        view.setRemainingTime(seconds);

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            countdown--;
            Platform.runLater(() -> view.setRemainingTime(countdown));
            if (countdown <= 0) {
                scheduler.shutdownNow();
                onReadyToStart();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public void onCountdownReset() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
        countdown = initialCountdown;
        view.setRemainingTime(countdown);
    }

    public void onReadyToStart() {
        System.out.println("[WaitingRoom] Game starting now.");
        // Ensure UI work happens on the FX thread
        Platform.runLater(() -> {
            try {
                // 1) Load the GameRoom FXML
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/player/GameRoomPage.fxml")
                );
                Parent root = loader.load();

                // 2) Grab the view (controller from FXML) and build the model
                GameRoomView view   = loader.getController();
                GameRoomModel model = new GameRoomModel(PlayerClient_Model.gameService);

                // 3) Instantiate your GameRoomController with the same playerId, sessionToken, and gameToken
                new GameRoomController(
                        model,
                        view,
                        this.playerId,
                        this.sessionToken,
                        this.gameToken
                );

                // 4) Swap scenes
                Stage stage = PlayerClient_Java.getStage();
                stage.setScene(new Scene(root));
                stage.setTitle("What's The Word - Game");
                stage.show();

            } catch (IOException e) {
                System.err.println("[WaitingRoomController] Failed to load GameRoomPage.fxml: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }


    // --- User action handlers ---

    private void handleCancel(ActionEvent event) {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
        model.leaveLobby(playerId, gameToken, sessionToken);
        PlayerClient_Java.navigateToLobby();
    }
}
