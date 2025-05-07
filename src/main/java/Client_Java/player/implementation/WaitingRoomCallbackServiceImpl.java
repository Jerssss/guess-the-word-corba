// File: Client_Java/player/implementation/WaitingRoomCallbackServiceImpl.java
package Client_Java.player.implementation;

import Client_Java.player.controller.WaitingRoomController;
import PlayerCallBackIDL.NotLoggedInException;
import PlayerCallBackIDL.WaitingRoomGameCallbackServicePOA;
import javafx.application.Platform;

/**
 * CORBA servant for waiting‑room updates.
 * Delegates on the FX thread to WaitingRoomController.
 */
public class WaitingRoomCallbackServiceImpl extends WaitingRoomGameCallbackServicePOA {
    private final WaitingRoomController controller;

    public WaitingRoomCallbackServiceImpl(WaitingRoomController controller) {
        this.controller = controller;
        System.out.println("[DEBUG][WaitingRoomCallbackService] instantiated with controller=" + controller);
    }

    @Override
    public void notifyPlayerJoined(String gameToken, int totalPlayers, String sessionToken)
            throws NotLoggedInException {
        System.out.println("[DEBUG][WaitingRoomCallbackService] notifyPlayerJoined()"
                + " gameToken=" + gameToken
                + ", totalPlayers=" + totalPlayers
                + ", sessionToken=" + sessionToken
        );
        Platform.runLater(() -> {
            System.out.println("[DEBUG][WaitingRoomCallbackService][UI] onPlayerCountUpdate(" + totalPlayers + ")");
            controller.onPlayerCountUpdate(totalPlayers);
        });
    }

    @Override
    public void notifyCountdownStart(String gameToken, int countdownSeconds, String sessionToken)
            throws NotLoggedInException {
        System.out.println("[DEBUG][WaitingRoomCallbackService] notifyCountdownStart()"
                + " gameToken=" + gameToken
                + ", countdownSeconds=" + countdownSeconds
                + ", sessionToken=" + sessionToken
        );
        Platform.runLater(() -> {
            System.out.println("[DEBUG][WaitingRoomCallbackService][UI] onCountdownStart(" + countdownSeconds + ")");
            controller.onCountdownStart(countdownSeconds);
        });
    }

    @Override
    public void notifyCountdownReset(String gameToken, String sessionToken)
            throws NotLoggedInException {
        System.out.println("[DEBUG][WaitingRoomCallbackService] notifyCountdownReset()"
                + " gameToken=" + gameToken
                + ", sessionToken=" + sessionToken
        );
        Platform.runLater(() -> {
            System.out.println("[DEBUG][WaitingRoomCallbackService][UI] onCountdownReset()");
            controller.onCountdownReset();
        });
    }
}
