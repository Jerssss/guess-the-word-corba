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

public class AuthenticationServiceImpl extends AuthenticationServicePOA {

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
            }

            // Now set logged_in to true
            updateLoginStatus(dbPlayerID);
            playerID.value = dbPlayerID;

            // Return new sessionToken (simple for now, you can enhance)
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
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NotLoggedInException("Database error: " + e.getMessage());
        }
    }

    @Override
    public String adminLogin(String username, String password, IntHolder adminID)
            throws AuthenticationException, AlreadyLoggedInException {
        // Similar logic for admins
        return "";
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

    private String generateSessionToken(String username) {
        return username + "-" + System.currentTimeMillis();
    }
}
