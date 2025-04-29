package Client_Java.player.model;

import Shared_Files.PlayerAccount;
import java.util.List;
import java.util.Arrays;

public class GameLobbyModel {
    private final PlayerAccount player;

    public GameLobbyModel(PlayerAccount player) {
        this.player = player;
    }

    public PlayerAccount getPlayer() {
        return player;
    }

    // Placeholder: simulate leaderboard data
    public List<String> fetchLeaderboard() {
        // Later replace this with real CORBA call to server
        return Arrays.asList(
                "1. Nathan - 10 wins",
                "2. Jacob - 8 wins",
                "3. Liam - 7 wins",
                "4. Mia - 5 wins",
                "5. Ava - 4 wins"
        );
    }
}
