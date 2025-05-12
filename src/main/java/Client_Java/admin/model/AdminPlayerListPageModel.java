package Client_Java.admin.model;

import AdminIDL.*;
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

    public boolean editPlayer(int playerID, String newPassword) {
        try {
            // Update this call based on the actual signature of adminService.modifyPlayer
            adminService.modifyPlayer(playerID, newPassword, sessionToken, adminID);
            return true;
        } catch (AdminIDL.NotLoggedInException e) {
            System.err.println("Error: Not logged in. " + e.getMessage());
            return false;
        } catch (AccountNotFoundException e) {
            System.err.println("Error: Account not found. " + e.getMessage());
            return false;
        } catch (ExistingPasswordException e) {
            System.err.println("Error: Password already exists. " + e.getMessage());
            return false;
        }
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
            System.err.println("Error: Not logged in. " + e.getMessage());
            return FXCollections.observableArrayList();
        }

        return FXCollections.observableArrayList(playerList);
    }
}