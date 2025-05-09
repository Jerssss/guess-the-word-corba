package Client_Java.player.model;

import GameIDL.GameService;
import GameIDL.NotLoggedInException;
import Shared_Files.PlayerAccount;

import java.util.ArrayList;
import java.util.List;

public class GameLobbyModel {
    private final PlayerAccount player;
    private final GameService gameService;

    public GameLobbyModel(PlayerAccount player, GameService gameService) {
        this.player = player;
        this.gameService = gameService;
    }

    public PlayerAccount getPlayer() {
        return player;
    }

    public List<LobbyLeaderboardCardModel> getLeaderboardData(String sessionToken) {
        List<LobbyLeaderboardCardModel> leaderboard = new ArrayList<>();
        System.out.println("[DEBUG] getLeaderboardData called with sessionToken: " + (sessionToken != null ? sessionToken : "null"));

        try {
            if (sessionToken == null) {
                System.err.println("[ERROR] Session token is null");
                return leaderboard;
            }

            String[] entries = gameService.getLeaderboards(player.getPlayerId(), sessionToken);
            System.out.println("[DEBUG] Fetched " + entries.length + " leaderboard entries from GameService");
            for (int i = 0; i < entries.length; i++) {
                System.out.println("[DEBUG] Processing entry: " + entries[i]);
                String[] parts = entries[i].split(":");
                if (parts.length == 2) {
                    String username = parts[0];
                    int gameWins = Integer.parseInt(parts[1]);
                    leaderboard.add(new LobbyLeaderboardCardModel(i + 1, username, gameWins));
                    System.out.println("[DEBUG] Added leaderboard entry: rank=" + (i + 1) + ", username=" + username + ", gameWins=" + gameWins);
                } else {
                    System.err.println("[ERROR] Invalid leaderboard entry format: " + entries[i]);
                }
            }
        } catch (NotLoggedInException e) {
            System.err.println("[ERROR] NotLoggedInException while fetching leaderboard: " + e.getMessage());
            // Log the player ID and session token for debugging
            System.err.println("[DEBUG] Player ID: " + (player != null ? player.getPlayerId() : "null") + ", Session Token: " + sessionToken);
        } catch (NumberFormatException e) {
            System.err.println("[ERROR] Failed to parse game_wins: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[ERROR] Unexpected error fetching leaderboard: " + e.getMessage());
        }

        System.out.println("[DEBUG] Returning " + leaderboard.size() + " leaderboard entries");
        return leaderboard;
    }
}