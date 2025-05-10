package Client_Java.player.model;

import GameIDL.GameService;
import GameIDL.NotLoggedInException;
import Shared_Files.PlayerAccount;

import java.util.ArrayList;
import java.util.Collections;
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

            // Process entries and collect data for the table
            for (int i = 0; i < entries.length; i++) {
                String entry = entries[i];
                System.out.println("[DEBUG] Processing entry: " + entry);
                String[] parts = entry.split(":");
                if (parts.length == 2) {
                    String username = parts[0];
                    int points;
                    try {
                        points = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException e) {
                        System.err.println("[ERROR] Failed to parse points for entry: " + entry);
                        continue;
                    }
                    leaderboard.add(new LobbyLeaderboardCardModel(i + 1, username, points));
                } else {
                    System.err.println("[ERROR] Invalid leaderboard entry format: " + entry);
                }
            }

            // Print leaderboard as a table
            if (!leaderboard.isEmpty()) {
                printLeaderboardTable(leaderboard);
            } else {
                System.out.println("[DEBUG] No valid leaderboard entries to display");
            }

        } catch (NotLoggedInException e) {
            System.err.println("[ERROR] NotLoggedInException while fetching leaderboard: " + e.getMessage());
            System.err.println("[DEBUG] Player ID: " + (player != null ? player.getPlayerId() : "null") + ", Session Token: " + sessionToken);
        } catch (Exception e) {
            System.err.println("[ERROR] Unexpected error fetching leaderboard: " + e.getMessage());
        }

        System.out.println("[DEBUG] Returning " + leaderboard.size() + " leaderboard entries");
        return leaderboard;
    }

    private void printLeaderboardTable(List<LobbyLeaderboardCardModel> leaderboard) {
        // Define column widths
        int rankWidth = 6; // "Rank" + padding
        int usernameWidth = 15; // Adjust based on expected username length
        int pointsWidth = 8; // "Points" + padding

        // Print table header
        String header = String.format("[DEBUG] Leaderboard:%n" +
                        "+-%s-+-%s-+-%s-+%n" +
                        "| %-" + rankWidth + "s | %-" + usernameWidth + "s | %-" + pointsWidth + "s |%n" +
                        "+-%s-+-%s-+-%s-+%n",
                String.join("", Collections.nCopies(rankWidth, "-")),
                String.join("", Collections.nCopies(usernameWidth, "-")),
                String.join("", Collections.nCopies(pointsWidth, "-")),
                "Rank", "Username", "Points",
                String.join("", Collections.nCopies(rankWidth, "-")),
                String.join("", Collections.nCopies(usernameWidth, "-")),
                String.join("", Collections.nCopies(pointsWidth, "-")));

        System.out.print(header);

        // Print table rows
        for (LobbyLeaderboardCardModel entry : leaderboard) {
            System.out.printf("| %-" + rankWidth + "d | %-" + usernameWidth + "s | %-" + pointsWidth + "d |%n",
                    entry.getRank(), entry.getUsername(), entry.getPoints());
        }

        // Print table footer
        System.out.printf("+-%" + rankWidth + "s-+-%-" + usernameWidth + "s-+-%-" + pointsWidth + "s-+%n",
                String.join("", Collections.nCopies(rankWidth, "-")),
                String.join("", Collections.nCopies(usernameWidth, "-")),
                String.join("", Collections.nCopies(pointsWidth, "-")));
    }
}