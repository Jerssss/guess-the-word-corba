package Server_Java.implementation;

import Server_Java.database.DatabaseConnection;
import AdminIDL.*;

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
        int id = random.nextInt(90000) + 10000; // Generates a number between 10000 and 99999
        return id;
        // TODO - check existing player id to prevent duplication
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

        // Check if the player is logged in.
        if (sessionToken == null) {
            System.err.println("[AdminService ERROR] getLeaderboards: Null session token for adminID=" + adminID);
            throw new AdminIDL.NotLoggedInException();
        }

        // Validate playerID
        if (!isValidAdmin(adminID)) {
            System.err.println("[AdminService ERROR] createPlayer: Invalid adminID=" + adminID);
            throw new AdminIDL.NotLoggedInException();
        }

        // TODO apply the account exists exception
        query ="INSERT INTO players (player_id, name, username, password) " +
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
        }catch (SQLException e){
            throw new RuntimeException(e);
        }catch (Exception e1){
            e1.printStackTrace();
        }
    }

    @Override
    public String[] viewPlayers(String sessionToken, int adminID) throws NotLoggedInException {
        // Check if the player is logged in.
        if (sessionToken == null) {
            System.err.println("[AdminService ERROR] getLeaderboards: Null session token for adminID=" + adminID);
            throw new AdminIDL.NotLoggedInException();
        }

        // Validate playerID
        if (!isValidAdmin(adminID)) {
            System.err.println("[AdminService ERROR] createPlayer: Invalid adminID=" + adminID);
            throw new AdminIDL.NotLoggedInException();
        }

        query = "SELECT player_id, name, username, password, game_wins FROM players;";
        List<String> players = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = con.prepareStatement(query);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                String playerID = resultSet.getString(1);
                String name = resultSet.getString(2);
                String username = resultSet.getString(3);
                String password = resultSet.getString(4);
                int gameWins = resultSet.getInt(5);
                String entry = String.join(",", playerID, name, username, password, String.valueOf(gameWins));
                players.add(entry);
            }
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

    }

    @Override
    public void deletePlayer(int playerID, String sessionToken, int adminID) throws AccountNotFoundException, AccountCurrentlyActiveException, NotLoggedInException {

    }

    @Override
    public String searchPlayer(String username, String sessionToken, int adminID) throws NotLoggedInException, PlayerNotFoundException {
        return "";
    }

    @Override
    public int getCurrentWaitingTime(String sessionToken, int adminID) throws NotLoggedInException{

        // Check if the player is logged in.
        if (sessionToken == null) {
            System.err.println("[AdminService ERROR] getLeaderboards: Null session token for adminID=" + adminID);
            throw new AdminIDL.NotLoggedInException();
        }

        // Validate playerID
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
        System.out.println("[" + formattedTimestamp +"] [Admin: " + adminID + "] - Action: Retreived Game Round Duration");
        return waitingTime;
    }

    @Override
    public int getCurrentRoundDuration(String sessionToken, int adminID) throws NotLoggedInException {

        // Check if the player is logged in.
        if (sessionToken == null) {
            System.err.println("[AdminService ERROR] getLeaderboards: Null session token for adminID=" + adminID);
            throw new AdminIDL.NotLoggedInException();
        }

        // Validate playerID
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
