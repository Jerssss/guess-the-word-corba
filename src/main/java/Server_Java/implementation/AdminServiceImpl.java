package Server_Java.implementation;

import Server_Java.database.DatabaseConnection;
import AdminIDL.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

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

    @Override
    public void createPlayer(String fullName, String username, String password, String sessionToken, int adminID) throws AccountExistsException, NotLoggedInException {
        // FIXME - add string fullname as parameter also in idl
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
    public void modifyPlayer(int playerID, String newPassword, String sessionToken, int adminID) throws AccountNotFoundException, ExistingPasswordException, NotLoggedInException {

    }

    @Override
    public void deletePlayer(int playerID, String sessionToken, int adminID) throws AccountNotFoundException, AccountCurrentlyActiveException, NotLoggedInException {

    }

    @Override
    public String searchPlayer(String username, String sessionToken, int adminID) throws NotLoggedInException, PlayerNotFoundException {
        return "";
    }

    // FIXME add these methods to the idl
    @Override
    public int getCurrentWaitingTime(String sessionToken, int adminID) {
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
    public int getCurrentRoundDuration(String sessionToken, int adminID) {
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
