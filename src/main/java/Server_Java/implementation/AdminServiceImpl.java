package Server_Java.implementation;

import Server_Java.database.DatabaseConnection;
import AdminIDL.*;
import Shared_Files.PlayerAccount;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AdminServiceImpl extends AdminServicePOA {
    private static String query; // holds the sql query
    private final AuthenticationServiceImpl authService;

    public AdminServiceImpl(AuthenticationServiceImpl authService) {
        this.authService = authService;
    }

    public static int generatePlayerID() {
        Random random = new Random();
        int id;
        boolean isUnique;

        do {
            id = random.nextInt(90000) + 10000; // Generates a number between 10000 and 99999
            isUnique = true;

            // Check if ID exists in database
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT COUNT(*) FROM players WHERE player_id = ?")) {
                stmt.setInt(1, id);
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    isUnique = false;
                }
            } catch (SQLException e) {
                System.err.println("[PlayerService ERROR] Error checking player ID=" + id + ": " + e.getMessage());
            }
        } while (!isUnique);

        return id;
    }

    // Checks if a admin ID is valid by looking it up in the database.
    private boolean isValidAdmin(int adminID) {
        // Query the database to see if the player exists.
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM admin WHERE admin_id = ?")) {
            stmt.setInt(1, adminID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("[AdminService ERROR] Error validating adminID=" + adminID + ": " + e.getMessage());
        }
        return false;
    }

    @Override
    public void createPlayer(String fullName, String username, String password, String sessionToken, int adminID) throws AccountExistsException, NotLoggedInException {
        if (sessionToken == null) {
            System.err.println("[AdminService ERROR] getLeaderboards: Null session token for adminID=" + adminID);
            throw new AdminIDL.NotLoggedInException();
        }
        if (!isValidAdmin(adminID)) {
            System.err.println("[AdminService ERROR] createPlayer: Invalid adminID=" + adminID);
            throw new AdminIDL.NotLoggedInException();
        }

        // Check if username already exists
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM players WHERE username = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                System.err.println("[AdminService ERROR] createPlayer: Username already exists: " + username);
                throw new AccountExistsException("Username " + username + " is already taken");
            }
        } catch (SQLException e) {
            System.err.println("[AdminService ERROR] Error checking username=" + username + ": " + e.getMessage());
            throw new RuntimeException(e);
        }

        String query = "INSERT INTO players (player_id, name, username, password) " +
                "VALUES (?, ?, ?, ?); ";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(query)){
            int player_id = generatePlayerID();
            preparedStatement.setInt(1, player_id);
            preparedStatement.setString(2, fullName);
            preparedStatement.setString(3, username);
            preparedStatement.setString(4, password);

            preparedStatement.executeUpdate();
            // Get the current timestamp and print the action
            LocalDateTime timestamp = LocalDateTime.now();
            String formattedTimestamp = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // Print the action details
            System.out.println("[" + formattedTimestamp +"] [Admin: " + adminID + "] - Action: Player Created  - Details: Player ID: " + player_id +", Username: " + username);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
    @Override
    public String[] viewPlayers(String sessionToken, int adminID) throws NotLoggedInException {
        if (sessionToken == null) {
            System.err.println("[AdminService ERROR] getLeaderboards: Null session token for adminID=" + adminID);
            throw new AdminIDL.NotLoggedInException();
        }

        if (!isValidAdmin(adminID)) {
            System.err.println("[AdminService ERROR] createPlayer: Invalid adminID=" + adminID);
            throw new AdminIDL.NotLoggedInException();
        }

        query = "SELECT player_id, username, password, name, game_wins FROM players;";
        List<String> players = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(query);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                String playerID = resultSet.getString(1);
                String username = resultSet.getString(2);
                String password = resultSet.getString(3);
                String name = resultSet.getString(4);
                int gameWins = resultSet.getInt(5);
                String entry = String.join(",", playerID, username, password, name, String.valueOf(gameWins));
                players.add(entry);
            }
            // Get the current timestamp and print the action
            LocalDateTime timestamp = LocalDateTime.now();
            String formattedTimestamp = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // Print the action details
            System.out.println("[" + formattedTimestamp +"] [Admin: " + adminID + "] - Action: Accessed Player List");
        } catch (SQLException e) {
            System.err.println("[AdminService ERROR] Database connection error: " + e.getMessage());
            return new String[0];
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        return players.toArray(new String[0]);
    }

    @Override
    public void modifyPlayer(int playerID, String newPassword, String sessionToken, int adminID) throws AccountNotFoundException, ExistingPasswordException, NotLoggedInException {
        if (sessionToken == null) {
            System.err.println("[AdminService ERROR] getLeaderboards: Null session token for adminID=" + adminID);
            throw new AdminIDL.NotLoggedInException();
        }
        if (!isValidAdmin(adminID)) {
            System.err.println("[AdminService ERROR] createPlayer: Invalid adminID=" + adminID);
            throw new AdminIDL.NotLoggedInException();
        }

        // Check if player exists
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM players WHERE player_id = ?")) {
            stmt.setInt(1, playerID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                System.err.println("[AdminService ERROR] modifyPlayer: Player not found for playerID=" + playerID);
                throw new AccountNotFoundException("Player with ID " + playerID + " not found");
            }
        } catch (SQLException e) {
            System.err.println("[AdminService ERROR] Error checking playerID=" + playerID + ": " + e.getMessage());
            throw new RuntimeException(e);
        }

        query = "UPDATE players SET password = ? WHERE player_id = ?";
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, newPassword);
            stmt.setInt(2, playerID);
            stmt.executeUpdate();
            // Get the current timestamp and print the action
            LocalDateTime timestamp = LocalDateTime.now();
            String formattedTimestamp = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // Print the action details
            System.out.println("[" + formattedTimestamp +"] [Admin: " + adminID + "] - Action: Modified Player - Details: Player: " + playerID + " New Password: " + newPassword);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void deletePlayer(int playerID, String sessionToken, int adminID) throws AccountNotFoundException, AccountCurrentlyActiveException, NotLoggedInException {
        if (sessionToken == null) {
            System.err.println("[AdminService ERROR] getLeaderboards: Null session token for adminID=" + adminID);
            throw new AdminIDL.NotLoggedInException();
        }
        if (!isValidAdmin(adminID)) {
            System.err.println("[AdminService ERROR] createPlayer: Invalid adminID=" + adminID);
            throw new AdminIDL.NotLoggedInException();
        }

        // Check if player exists
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM players WHERE player_id = ?")) {
            stmt.setInt(1, playerID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                System.err.println("[AdminService ERROR] deletePlayer: Player not found for playerID=" + playerID);
                throw new AccountNotFoundException("Player with ID " + playerID + " not found");
            }
        } catch (SQLException e) {
            System.err.println("[AdminService ERROR] Error checking playerID=" + playerID + ": " + e.getMessage());
            throw new RuntimeException(e);
        }

        query = "DELETE FROM players WHERE player_id = ?";
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, playerID);
            stmt.executeUpdate();
            // Get the current timestamp and print the action
            LocalDateTime timestamp = LocalDateTime.now();
            String formattedTimestamp = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // Print the action details
            System.out.println("[" + formattedTimestamp +"] [Admin: " + adminID + "] - Action: Deleted Player - Details: Player: " + playerID);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public String[] searchPlayers(String query, String sessionToken, int adminID) throws NotLoggedInException, PlayerNotFoundException {
        if (sessionToken == null) {
            System.err.println("[AdminService ERROR] searchPlayers: Null session token for adminID=" + adminID);
            throw new AdminIDL.NotLoggedInException();
        }
        if (!isValidAdmin(adminID)) {
            System.err.println("[AdminService ERROR] searchPlayers: Invalid adminID=" + adminID);
            throw new AdminIDL.NotLoggedInException();
        }

        String sqlQuery = "SELECT player_id, username, password, name, game_wins FROM players " +
                "WHERE CAST(player_id AS CHAR) LIKE ? " +
                "OR username LIKE ? " +
                "OR name LIKE ? " +
                "OR CAST(game_wins AS CHAR) LIKE ?";
        List<String> players = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sqlQuery)) {
            String likeQuery = "%" + query + "%";
            stmt.setString(1, likeQuery);
            stmt.setString(2, likeQuery);
            stmt.setString(3, likeQuery);
            stmt.setString(4, likeQuery);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                String playerID = resultSet.getString(1);
                String username = resultSet.getString(2);
                String password = resultSet.getString(3);
                String name = resultSet.getString(4);
                int gameWins = resultSet.getInt(5);
                String entry = String.join(",", playerID, username, password, name, String.valueOf(gameWins));
                players.add(entry);
            }

            if (players.isEmpty()) {
                System.err.println("[AdminService INFO] searchPlayers: No players found for query=" + query);
                throw new PlayerNotFoundException();
            }

        } catch (SQLException e) {
            System.err.println("[AdminService ERROR] Database connection error: " + e.getMessage());
            e.printStackTrace();
            throw new PlayerNotFoundException();
        }

        return players.toArray(new String[0]);
    }

    @Override
    public int getCurrentWaitingTime(String sessionToken, int adminID) throws NotLoggedInException{
        if (sessionToken == null) {
            System.err.println("[AdminService ERROR] getLeaderboards: Null session token for adminID=" + adminID);
            throw new AdminIDL.NotLoggedInException();
        }
        if (!isValidAdmin(adminID)) {
            System.err.println("[AdminService ERROR] createPlayer: Invalid adminID=" + adminID);
            throw new AdminIDL.NotLoggedInException();
        }

        int waitingTime = 0;
        query = "SELECT lobby_waiting_time FROM settings";
        try (Connection con = DatabaseConnection.getConnection()){
            Statement stmt = con.createStatement();
            ResultSet resultSet = stmt.executeQuery(query);

            if (resultSet.next()) {
                waitingTime = resultSet.getInt(1);
            }
        } catch (SQLException e1) {
            e1.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        // Get the current timestamp and print the action
        LocalDateTime timestamp = LocalDateTime.now();
        String formattedTimestamp = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Print the action details
        System.out.println("[" + formattedTimestamp +"] [Admin: " + adminID + "] - Action: Retreived Game Lobby Waiting Time");
        return waitingTime;
    }

    @Override
    public int getCurrentRoundDuration(String sessionToken, int adminID) throws NotLoggedInException {
        if (sessionToken == null) {
            System.err.println("[AdminService ERROR] getLeaderboards: Null session token for adminID=" + adminID);
            throw new AdminIDL.NotLoggedInException();
        }
        if (!isValidAdmin(adminID)) {
            System.err.println("[AdminService ERROR] createPlayer: Invalid adminID=" + adminID);
            throw new AdminIDL.NotLoggedInException();
        }

        int roundDuration = 0;
        query = "SELECT round_duration FROM settings";
        try (Connection con = DatabaseConnection.getConnection()){
            Statement stmt = con.createStatement();
            ResultSet resultSet = stmt.executeQuery(query);

            if (resultSet.next()) {
                roundDuration = resultSet.getInt(1);

            }
        } catch (SQLException e1) {
            e1.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        // Get the current timestamp and print the action
        LocalDateTime timestamp = LocalDateTime.now();
        String formattedTimestamp = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Print the action details
        System.out.println("[" + formattedTimestamp +"] [Admin: " + adminID + "] - Action: Retreived Game Round Duration");
        return roundDuration;
    }

    //TODO edit the game config to only accept numbers
    @Override
    public void modifyWaitingTime(int waitTime, String sessionToken, int adminID) throws NotLoggedInException {
        if (sessionToken == null) {
            System.err.println("[AdminService ERROR] getLeaderboards: Null session token for adminID=" + adminID);
            throw new AdminIDL.NotLoggedInException();
        }
        if (!isValidAdmin(adminID)) {
            System.err.println("[AdminService ERROR] createPlayer: Invalid adminID=" + adminID);
            throw new AdminIDL.NotLoggedInException();
        }

        query = "UPDATE settings SET lobby_waiting_time = ?";
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, waitTime);
            stmt.executeUpdate();

            // Get the current timestamp and print the action
            LocalDateTime timestamp = LocalDateTime.now();
            String formattedTimestamp = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // Print the action details
            System.out.println("[" + formattedTimestamp +"] [Admin: " + adminID + "] - Action: Modified Game Waiting Time - Details: Waiting Time: " + waitTime);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void modifyRoundDuration(int roundTime, String sessionToken, int adminID) throws NotLoggedInException {
        if (sessionToken == null) {
            System.err.println("[AdminService ERROR] getLeaderboards: Null session token for adminID=" + adminID);
            throw new AdminIDL.NotLoggedInException();
        }
        if (!isValidAdmin(adminID)) {
            System.err.println("[AdminService ERROR] createPlayer: Invalid adminID=" + adminID);
            throw new AdminIDL.NotLoggedInException();
        }

        query = "UPDATE settings SET round_duration = ?";
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, roundTime);
            stmt.executeUpdate();
            // Get the current timestamp and print the action
            LocalDateTime timestamp = LocalDateTime.now();
            String formattedTimestamp = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // Print the action details
            System.out.println("[" + formattedTimestamp +"] [Admin: " + adminID + "] - Action: Modified Game Round Duration - Details: Waiting Time: " + roundTime);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
