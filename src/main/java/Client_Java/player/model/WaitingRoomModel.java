package Client_Java.player.model;

import PlayerGame.GameTimeoutException;
import PlayerGame.NotLoggedInException;
import PlayerGame.Player;

public class WaitingRoomModel {
    private Player player;
    private int gid;
    private String sessionToken;

    public WaitingRoomModel(Player player, int gid, String sessionToken) {
        this.player = player;
        this.gid = gid;
        this.sessionToken = sessionToken;
    }

    public void leaveGame() throws NotLoggedInException {
        PlayerClient_Model.gameService.leaveGame(player.playerID, sessionToken, gid);
    }

    public int getRemainingWaitingTime() throws GameTimeoutException, NotLoggedInException {
        return (int) PlayerClient_Model.gameService.getRemainingWaitingTime(player.playerID, sessionToken);
    }

    public int getNumberOfPlayersWaiting() throws NotLoggedInException {
        return (int) PlayerClient_Model.gameService.getNumberOfPlayersJoined(player.playerID, sessionToken);
    }

    public Player getPlayer() {
        return player;
    }

    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
}

