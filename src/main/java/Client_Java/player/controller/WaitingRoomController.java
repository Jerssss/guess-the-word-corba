// File: Client_Java/player/controller/WaitingRoomController.java
package Client_Java.player.controller;

import Client_Java.player.PlayerClient_Java;
import Client_Java.player.SessionManager;
import Client_Java.player.model.WaitingRoomModel;
import Client_Java.player.view.WaitingRoomView;
import Client_Java.player.view.ViewNavigator;
import Client_Java.player.implementation.WaitingRoomCallbackServiceImpl;
import Server_Java.idls.PlayerCallBackIDL.GameCallBackService;
import Server_Java.idls.PlayerCallBackIDL.GameCallBackServiceHelper;
import Server_Java.idls.PlayerCallBackIDL.GameCallBackServicePOA;
import Server_Java.idls.PlayerCallBackIDL.WaitingRoomGameCallbackService;
import Server_Java.idls.PlayerCallBackIDL.WaitingRoomGameCallbackServiceHelper;
import javafx.application.Platform;
import javafx.event.ActionEvent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WaitingRoomController {
    private final WaitingRoomModel model;
    private final WaitingRoomView  view;

    private final int initialCountdown;
    private final int minimumPlayers;
    private int       countdown;

    private ScheduledExecutorService scheduler;

    public WaitingRoomController(
            WaitingRoomModel model,
            WaitingRoomView view
    ) {
        this.model    = model;
        this.view     = view;

        this.initialCountdown = model.getSetting("countdown_to_game_start");
        this.minimumPlayers   = model.getSetting("minimum_players");
        this.countdown        = initialCountdown;

        view.setActionCancelButton(this::onCancel);
        startFlow();
    }

    private void startFlow() {
        int playerId = SessionManager.getLoggedInPlayer().getPlayerId();
        String gameToken = model.joinLobby(playerId);
        if (gameToken == null) {
            System.err.println("[WaitingRoom] joinLobby failed");
            return;
        }

        registerWaitingRoomCallback(playerId);
        registerGameStartCallback(playerId);

        view.setWaitingPlayersCount(model.getNumberOfPlayersJoined());
        view.setRemainingTime(countdown);
    }

    private void registerWaitingRoomCallback(int playerId) {
        WaitingRoomCallbackServiceImpl servant =
                new WaitingRoomCallbackServiceImpl(this);
        org.omg.CORBA.Object cbRef =
                PlayerClient_Java.getClientModel()
                        .registerWaitingRoomCallback(servant);
        WaitingRoomGameCallbackService stub =
                WaitingRoomGameCallbackServiceHelper.narrow(cbRef);
        model.registerWaitingRoomCallback(playerId, stub);
    }

    private void registerGameStartCallback(int playerId) {
        GameCallBackServicePOA servant = new GameCallBackServicePOA() {
            @Override
            public void notifyGameStart(String gt, String st) {
                onReadyToStart();
            }
            @Override public void notifyRoundStart(String gt, int r, String st) {}
            @Override public void notifyRoundEnd  (String gt, String st, String w) {}
            @Override public void notifyGameEnd   (String gt, String st, String w) {}
        };
        org.omg.CORBA.Object cbRef =
                PlayerClient_Java.getClientModel()
                        .registerGameCallback(servant);
        GameCallBackService stub =
                GameCallBackServiceHelper.narrow(cbRef);
        model.registerGameStartCallback(playerId, stub);
    }

    public void onPlayerCountUpdate(int totalPlayers) {
        Platform.runLater(() -> view.setWaitingPlayersCount(totalPlayers));
    }

    public void onCountdownStart(int seconds) {
        this.countdown = seconds;
        Platform.runLater(() -> view.setRemainingTime(seconds));

        if (scheduler != null) scheduler.shutdownNow();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            countdown--;
            Platform.runLater(() -> view.setRemainingTime(countdown));
            if (countdown <= 0) {
                scheduler.shutdown();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public void onCountdownReset() {
        if (scheduler != null) scheduler.shutdownNow();
        countdown = initialCountdown;
        Platform.runLater(() -> view.setRemainingTime(countdown));
    }

    /** NOW uses the stored gameToken rather than playerId */
    private void onReadyToStart() {
        Platform.runLater(() -> {
            try {
                ViewNavigator.goToGameRoom();  // <-- no-arg uses SessionManager.getGameToken()
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void onCancel(ActionEvent evt) {
        if (scheduler != null) scheduler.shutdownNow();
        int playerId = SessionManager.getLoggedInPlayer().getPlayerId();
        model.leaveLobby(playerId);
        Platform.runLater(() -> {
            try {
                ViewNavigator.goToLobby();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
