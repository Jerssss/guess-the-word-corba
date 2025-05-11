package Client_Java.admin.model;

import AdminIDL.AccountExistsException;
import AdminIDL.AdminService;
import AdminIDL.NotLoggedInException;
import Shared_Files.PlayerAccount;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class AdminPlayerListPageModel {
    private final AdminService adminService;
    private final String sessionToken;
    private final int adminID;

    public AdminPlayerListPageModel(AdminService adminService, String sessionToken, int adminID) {
        this.adminService = adminService;
        this.sessionToken = sessionToken;
        this.adminID = adminID;
    }

    private final ObservableList<PlayerAccount> playerList = FXCollections.observableArrayList();

    public ObservableList<PlayerAccount> getPlayerList() {
        return playerList;
    }

    public void addPlayer(PlayerAccount player) {
        playerList.add(player);
    }

    public void removePlayer(PlayerAccount player) {
        playerList.remove(player);
    }

    public void clearPlayers() {
        playerList.clear();
    }

    public ObservableList<PlayerAccount> fetchPlayers() {
        List<PlayerAccount> playerList = new ArrayList<>();

        try {
            // Attempt to get the player data
            String[] players = adminService.viewPlayers(sessionToken, adminID);

            for (String data : players) {
                String[] parts = data.split(",");

                if (parts.length == 5) {
                    try {
                        int playerID = Integer.parseInt(parts[0]);
                        String name = parts[1];
                        String username = parts[2];
                        String password = parts[3];
                        int gameWins = Integer.parseInt(parts[4]);

                        // Create a new player instance
                        PlayerAccount player = new PlayerAccount(playerID, name, username, password, gameWins);
                        playerList.add(player);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid number format in player data: " + data);
                    }
                } else {
                    System.err.println("Malformed player data: " + data);
                }
            }
        } catch (AdminIDL.NotLoggedInException e) {
            // Handle the exception (e.g., show a message, return an empty list)
            System.err.println("Error: Not logged in. " + e.getMessage());
            // Optionally, return an empty list or take appropriate action
            return FXCollections.observableArrayList(); // return an empty list in case of an error
        }

        return FXCollections.observableArrayList(playerList);
    }
}
