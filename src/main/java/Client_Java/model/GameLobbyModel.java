package Client_Java.model;

import GameApp.GameService;

public class GameLobbyModel {
    private final GameService gameService;

    public GameLobbyModel(GameService gameService) {
        this.gameService = gameService;
    }

    public String[] fetchTopPlayers() {
        try {
            return gameService.getLeaderboards();
        } catch (Exception e) {
            e.printStackTrace();
            return new String[0];
        }
    }
}
