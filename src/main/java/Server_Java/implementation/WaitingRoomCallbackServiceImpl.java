package Server_Java.implementation;

import Client_Java.player.controller.WaitingRoomController;
import Server_Java.idls.PlayerCallBackIDL.NotLoggedInException;
import Server_Java.idls.PlayerCallBackIDL.WaitingRoomGameCallbackServicePOA;
import javafx.application.Platform;

public class WaitingRoomCallbackServiceImpl extends WaitingRoomGameCallbackServicePOA {

    private final WaitingRoomController controller;

    public WaitingRoomCallbackServiceImpl(WaitingRoomController controller) {
        this.controller = controller;
    }

    @Override
    public void notifyPlayerJoined(String gameToken, int totalPlayers, String sessionToken) throws NotLoggedInException {
        Platform.runLater(() ->
                controller.onPlayerCountUpdate((int)totalPlayers)
        );
    }

    @Override
    public void notifyCountdownStart(String gameToken, int countdownSeconds, String sessionToken) throws NotLoggedInException {
        Platform.runLater(() ->
                controller.onCountdownStart((int)countdownSeconds)
        );
    }

    @Override
    public void notifyCountdownReset(String gameToken, String sessionToken) throws NotLoggedInException {
        Platform.runLater(controller::onCountdownReset);
    }
}
