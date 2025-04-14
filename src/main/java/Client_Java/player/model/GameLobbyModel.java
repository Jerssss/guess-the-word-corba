package Client_Java.player.model;

import Client_Java.PlayerClient_Java;
import PlayerGame.GameService;

public class GameLobbyModel {
    private final GameService gameService;

    public GameLobbyModel(GameService gameService) {
        this.gameService = gameService;
    }

    public String[] fetchTopPlayers() {
        try {
            // Pass the playerID and sessionToken to the getLeaderboards method
            int playerID = (int) PlayerClient_Java.getLoggedInPlayerID();
            String sessionToken = PlayerClient_Java.getSessionToken();
            return gameService.getLeaderboards(playerID, sessionToken);
        } catch (Exception e) {
            e.printStackTrace();
            return new String[0];
        }
    }
}
