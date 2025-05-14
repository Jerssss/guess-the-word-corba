package Client_Java.player.controller;

import Client_Java.player.PlayerClient_Java;
import Client_Java.player.SessionManager;
import Client_Java.player.model.WaitingRoomModel;
import Client_Java.player.view.WaitingRoomView;
import Client_Java.player.view.ViewNavigator;
import Client_Java.player.implementation.WaitingRoomCallbackServiceImpl;
import PlayerCallBackIDL.GameCallBackService;
import PlayerCallBackIDL.GameCallBackServiceHelper;
import PlayerCallBackIDL.GameCallBackServicePOA;
import PlayerCallBackIDL.WaitingRoomGameCallbackService;
import PlayerCallBackIDL.WaitingRoomGameCallbackServiceHelper;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.util.Duration;

public class WaitingRoomController {
    private final WaitingRoomModel model;
    private final WaitingRoomView view;
    private final int initialCountdown;
    private final int minimumPlayers;
    private int countdown;
    private String gameToken;
    private Timeline countdownTimeline;

    public WaitingRoomController(WaitingRoomModel model, WaitingRoomView view) {
        this.model = model;
        this.view = view;
        this.initialCountdown = model.getSetting("countdown_to_game_start");
        this.minimumPlayers = model.getSetting("minimum_players");
        this.countdown = initialCountdown;
        view.setActionCancelButton(this::onCancel);
        startFlow();
    }

    private void startFlow() {
        int playerId = SessionManager.getLoggedInPlayer().getPlayerId();
        this.gameToken = model.joinLobby(playerId);
        if (this.gameToken == null) {
            System.err.println("[WaitingRoom] joinLobby failed");
            Platform.runLater(() -> {
                try {
                    ViewNavigator.goToLobby();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            return;
        }
        registerWaitingRoomCallback(playerId);
        registerGameStartCallback(playerId);
        view.setWaitingPlayersCount(model.getNumberOfPlayersJoined());
    }

    private void registerWaitingRoomCallback(int playerId) {
        WaitingRoomCallbackServiceImpl servant = new WaitingRoomCallbackServiceImpl(this);
        org.omg.CORBA.Object cbRef = PlayerClient_Java.getClientModel().registerWaitingRoomCallback(servant);
        WaitingRoomGameCallbackService stub = WaitingRoomGameCallbackServiceHelper.narrow(cbRef);
        model.registerWaitingRoomCallback(playerId, stub);
    }

    private void registerGameStartCallback(int playerId) {
        GameCallBackServicePOA servant = new GameCallBackServicePOA() {
            @Override
            public void notifyGameStart(String gt, String st) {
                onReadyToStart();
            }
            @Override
            public void notifyRoundStart(String gt, int r, String st) {}
            @Override
            public void notifyRoundEnd(String gt, String st, String winnerName, String secretWord) {}
            @Override
            public void notifyGameEnd(String gt, String st, String w) {}
        };
        org.omg.CORBA.Object cbRef = PlayerClient_Java.getClientModel().registerGameCallback(servant);
        GameCallBackService stub = GameCallBackServiceHelper.narrow(cbRef);
        model.registerGameStartCallback(playerId, stub);
    }

    public void onPlayerCountUpdate(int totalPlayers) {
        Platform.runLater(() -> {
            view.setWaitingPlayersCount(totalPlayers);
            System.out.println("[WaitingRoom] Updated player count to " + totalPlayers);
        });
    }

    public void onCountdownStart(int seconds) {
        Platform.runLater(() -> {
            if (countdownTimeline != null) {
                countdownTimeline.stop();
            }
            countdown = seconds;
            view.setRemainingTime(countdown);
            countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), evt -> {
                countdown--;
                view.setRemainingTime(countdown);
                if (countdown <= 0) {
                    countdownTimeline.stop();
                    view.setRemainingTime(0);
                }
            }));
            countdownTimeline.setCycleCount(seconds + 1);
            countdownTimeline.play();
            System.out.println("[WaitingRoom] Started countdown: " + seconds + " seconds");
        });
    }

    public void onCountdownReset() {
        Platform.runLater(() -> {
            if (countdownTimeline != null) {
                countdownTimeline.stop();
            }
            countdown = initialCountdown;
            view.setRemainingTime(0);
            int playerId = SessionManager.getLoggedInPlayer().getPlayerId();
            model.leaveLobby(playerId);
            try {
                ViewNavigator.goToLobby();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("[WaitingRoom] Countdown reset, returning to lobby");
        });
    }

    private void onReadyToStart() {
        Platform.runLater(() -> {
            try {
                ViewNavigator.goToGameRoom(gameToken);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void onCancel(ActionEvent evt) {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
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