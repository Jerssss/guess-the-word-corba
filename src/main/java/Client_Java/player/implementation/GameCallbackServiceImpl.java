// File: Client_Java/player/implementation/GameCallbackServiceImpl.java
package Client_Java.player.implementation;

import Client_Java.player.controller.GameRoomController;
import Server_Java.idls.PlayerCallBackIDL.GameCallBackServicePOA;

public class GameCallbackServiceImpl extends GameCallBackServicePOA {
    private final GameRoomController controller;

    public GameCallbackServiceImpl(GameRoomController controller) {
        this.controller = controller;
    }

    /** No longer drives round 1—controller did that already. */
    @Override
    public void notifyGameStart(String gameToken, String ignoredSessionToken) {
        System.out.println("[DEBUG][GameCallbackService] notifyGameStart (ignored)");
    }

    @Override
    public void notifyRoundStart(String gameToken, int roundNumber, String ignoredSessionToken) {
        System.out.println("[DEBUG][GameCallbackService] notifyRoundStart → Round " + roundNumber);
        controller.notifyRoundStartFromCallback(roundNumber);
    }

    @Override
    public void notifyRoundEnd(String gameToken, String ignoredSessionToken, String winnerName) {
        System.out.println("[DEBUG][GameCallbackService] notifyRoundEnd(winner=" + winnerName + ")");
        controller.showRoundEndPopup(winnerName);
    }

    @Override
    public void notifyGameEnd(String gameToken, String ignoredSessionToken, String winnerName) {
        System.out.println("[DEBUG][GameCallbackService] notifyGameEnd(winner=" + winnerName + ")");
        controller.showGameEndPopup(winnerName);
    }
}
