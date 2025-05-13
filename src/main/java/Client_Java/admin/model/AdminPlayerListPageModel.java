package Client_Java.admin.model;

import AdminIDL.*;
import Shared_Files.PlayerAccount;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

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

    public boolean removePlayer(int playerID) throws AccountCurrentlyActiveException, AccountNotFoundException, NotLoggedInException {
        try {
            adminService.deletePlayer(playerID, sessionToken, adminID);
            return true;
        } catch (AdminIDL.NotLoggedInException e) {
            System.err.println("Error: Not logged in. " + e.getMessage());
            return false;
        } catch (AccountNotFoundException e) {
            System.err.println("Error: Account not found. " + e.getMessage());
            return false;
        } catch (AccountCurrentlyActiveException e) {
            System.err.println("Error: Account currently active. " + e.getMessage());
            return false;
        }
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
                        String username = parts[1];
                        String password = parts[2];
                        String name = parts[3];
                        int gameWins = Integer.parseInt(parts[4]);
                        PlayerAccount player = new PlayerAccount(playerID, username, password, name, gameWins);
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
        this.playerList.setAll(playerList);
        return FXCollections.observableArrayList(playerList);
    }

    public void searchPlayers(String query, ObservableList<PlayerAccount> playerData, FilteredList<PlayerAccount> filteredData) {
        Platform.runLater(() -> {
            if (query == null || query.trim().isEmpty()) {
                playerData.setAll(fetchPlayers());
                filteredData.setPredicate(p -> true);
                System.out.println("[INFO] Search cleared, showing all players: " + playerData.size());
                return;
            }

            List<PlayerAccount> playerList = new ArrayList<>();
            try {
                String[] players = adminService.searchPlayers(query, sessionToken, adminID);
                for (String data : players) {
                    String[] parts = data.split(",");
                    if (parts.length == 5) {
                        try {
                            int playerID = Integer.parseInt(parts[0]);
                            String username = parts[1];
                            String password = parts[2];
                            String name = parts[3];
                            int gameWins = Integer.parseInt(parts[4]);
                            PlayerAccount player = new PlayerAccount(playerID, username, password, name, gameWins);
                            playerList.add(player);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid number format in player data: " + data);
                        }
                    } else {
                        System.err.println("Malformed player data: " + data);
                    }
                }
                playerData.setAll(playerList);
                filteredData.setPredicate(p -> true);
            } catch (NotLoggedInException e) {
                System.err.println("Error: Not logged in. " + e.getMessage());
                playerData.clear();
                filteredData.setPredicate(p -> false);
            } catch (PlayerNotFoundException e) {
                System.err.println("Error: No players found for query: " + query);
                playerData.clear();
                filteredData.setPredicate(p -> false);
            }
        });
    }
}