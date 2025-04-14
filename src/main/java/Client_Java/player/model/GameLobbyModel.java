package Client_Java.player.model;

import PlayerGame.GameService;

public class GameLobbyModel {
    private final GameService gameService;

    public GameLobbyModel(GameService gameService) {
        this.gameService = gameService;
    }

    public String[] fetchTopPlayers(int playerID, String sessionToken) {
        try {
            return gameService.getLeaderboards(playerID, sessionToken);
        } catch (Exception e) {
            e.printStackTrace();
            return new String[0];
        }
    }
}
