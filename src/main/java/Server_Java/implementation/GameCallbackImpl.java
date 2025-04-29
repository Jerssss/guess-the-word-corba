package Server_Java.implementation;

import Server_Java.idls.PlayerCallBackIDL.GameCallBackServicePOA;

public class GameCallbackImpl extends GameCallBackServicePOA {
    @Override
    public void notifyGameStart(String gameToken, String sessionToken) {

    }

    @Override
    public void notifyRoundStart(String gameToken, int roundNumber, String sessionToken) {

    }

    @Override
    public void notifyRoundEnd(String gameToken, String sessionToken, String result) {

    }

    @Override
    public void notifyGameEnd(String gameToken, String sessionToken, String result) {

    }
}
