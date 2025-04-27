package Server_Java.implementation;

import IDL_Files.AuthenticationIDL.AuthenticationException;
import IDL_Files.AuthenticationIDL.AlreadyLoggedInException;
import IDL_Files.AuthenticationIDL.AuthenticationServicePOA;
import IDL_Files.AuthenticationIDL.NotLoggedInException;
import Server_Java.database.DatabaseConnection;
import org.omg.CORBA.IntHolder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AuthenticationServiceImpl extends AuthenticationServicePOA {

    private static final Set<String> activeClients = Collections.synchronizedSet(new HashSet<>());

    @Override
    public String login(String username, String password, IntHolder playerID)
            throws AuthenticationException, AlreadyLoggedInException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT player_id, password, is_logged_in FROM players WHERE username = ?")) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                throw new AuthenticationException("Account does not exist.");
            }

            String dbPassword = rs.getString("password");
            boolean isLoggedIn = rs.getBoolean("is_logged_in");
            int dbPlayerID = rs.getInt("player_id");

            if (!dbPassword.equals(password)) {
                throw new AuthenticationException("Invalid password.");
            }

            if (isLoggedIn) {
                // Force logout old session
                forceLogout(dbPlayerID);
                System.out.println("[SERVER] Force-Logged out previous session for user: " + username);
            }

            // Update login status
            updateLoginStatus(dbPlayerID);
            playerID.value = dbPlayerID;

            // Add to active clients list
            activeClients.add(username);

            System.out.println("[SERVER] LOGIN SUCCESS: " + username + " has logged in.");

            // Return session token
            return generateSessionToken(username);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    @Override
    public void logout(int playerID, String sessionToken) throws NotLoggedInException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE players SET is_logged_in = 0 WHERE player_id = ?")) {
            stmt.setInt(1, playerID);
            stmt.executeUpdate();

            // Find username from ID (optional if you want to remove from active list)
            // activeClients.remove(username);

            System.out.println("[SERVER] LOGOUT SUCCESS for playerID: " + playerID);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NotLoggedInException("Database error: " + e.getMessage());
        }
    }

    @Override
    public String adminLogin(String username, String password, IntHolder adminID)
            throws AuthenticationException, AlreadyLoggedInException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT admin_id, password, is_logged_in FROM admin WHERE username = ?")) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                throw new AuthenticationException("Account does not exist.");
            }

            String dbPassword = rs.getString("password");
            boolean isLoggedIn = rs.getBoolean("is_logged_in");
            int dbAdminID = rs.getInt("admin_id");

            if (!dbPassword.equals(password)) {
                throw new AuthenticationException("Invalid password.");
            }

            if (isLoggedIn) {
                // Force logout old session
                forceAdminLogout(dbAdminID);
                System.out.println("[SERVER] Force-Logged out previous session for user: " + username);
            }

            // Update login status
            updateAdminLoginStatus(dbAdminID);
            adminID.value = dbAdminID;

            // Add to active clients list
            activeClients.add(username);

            System.out.println("[SERVER] LOGIN SUCCESS: " + username + " has logged in.");

            // Return session token
            return generateSessionToken(username);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    @Override
    public void adminLogout(int adminID, String sessionToken) throws NotLoggedInException {
        // Admin logout logic
    }

    private void forceLogout(int playerId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE players SET is_logged_in = 0 WHERE player_id = ?")) {
            stmt.setInt(1, playerId);
            stmt.executeUpdate();
        }
    }

    private void updateLoginStatus(int playerId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE players SET is_logged_in = 1 WHERE player_id = ?")) {
            stmt.setInt(1, playerId);
            stmt.executeUpdate();
        }
    }

    private void forceAdminLogout(int adminId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE admin SET is_logged_in = 0 WHERE admin_id = ?")) {
            stmt.setInt(1, adminId);
            stmt.executeUpdate();
        }
    }

    private void updateAdminLoginStatus(int adminId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE admin SET is_logged_in = 1 WHERE admin_id = ?")) {
            stmt.setInt(1, adminId);
            stmt.executeUpdate();
        }
    }

    private String generateSessionToken(String username) {
        return username + "-" + System.currentTimeMillis();
    }
}