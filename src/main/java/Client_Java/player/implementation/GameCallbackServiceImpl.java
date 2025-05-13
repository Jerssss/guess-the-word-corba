package Client_Java.player.implementation;

import Client_Java.player.controller.GameRoomController;
import PlayerCallBackIDL.GameCallBackServicePOA;

public class GameCallbackServiceImpl extends GameCallBackServicePOA {
    private final GameRoomController controller;

    public GameCallbackServiceImpl(GameRoomController controller) {
        this.controller = controller;
    }

    @Override
    public void notifyGameStart(String gameToken, String sessionToken) {
        System.out.printf("[CALLBACK][GameStart] gameToken=%s session=%s%n",
                gameToken, sessionToken);
        // First round is client-driven; ignore
    }

    @Override
    public void notifyRoundStart(String gameToken, int roundNumber, String sessionToken) {
        System.out.printf("[CALLBACK][RoundStart] → gameToken=%s round=%d session=%s%n",
                gameToken, roundNumber, sessionToken);
        controller.notifyRoundStartFromCallback(roundNumber);
    }

    @Override
    public void notifyRoundEnd(String gameToken, String sessionToken, String winnerName, String secretWord) {
        if (winnerName != null && !winnerName.trim().isEmpty()) {
            System.out.printf("[CALLBACK][RoundEnd] → gameToken=%s winner=\"%s\" word=\"%s\" session=%s%n",
                    gameToken, winnerName, secretWord, sessionToken);
        } else {
            System.out.printf("[CALLBACK][RoundEnd] → gameToken=%s NO WINNER word=\"%s\" session=%s%n",
                    gameToken, secretWord, sessionToken);
        }
        controller.showRoundEnd(winnerName, secretWord);
    }

    @Override
    public void notifyGameEnd(String gameToken, String sessionToken, String winnerName) {
        if (winnerName != null && !winnerName.trim().isEmpty()) {
            System.out.printf("[CALLBACK][GameEnd] → gameToken=%s champion=\"%s\" session=%s%n",
                    gameToken, winnerName, sessionToken);
        } else {
            System.out.printf("[CALLBACK][GameEnd] → gameToken=%s NO WINNER session=%s%n",
                    gameToken, sessionToken);
        }
        controller.showGameEnd(winnerName);
    }
}