package Client_Java.player.model;

import PlayerGame.Player;
import Server_Java.database.PlayerQueries;

public class GameLobbyModel {
    private final Player player;

    public GameLobbyModel(Player player) {
        this.player = player;
    }

    public String[] fetchTopPlayers() {
        return PlayerQueries.fetchTopPlayers();
    }

    public Player getPlayer() {
        return player;
    }
}