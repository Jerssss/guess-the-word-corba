package Server_Java.implementation;

import Server_Java.database.DatabaseConnection;
import AuthenticationIDL.AlreadyLoggedInException;
import AuthenticationIDL.AuthenticationException;
import AuthenticationIDL.AuthenticationServicePOA;
import AuthenticationIDL.NotLoggedInException;
import PlayerCallBackIDL.LoginCallbackService;
import org.omg.CORBA.IntHolder;

import java.sql.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthenticationServiceImpl extends AuthenticationServicePOA {


    // Track active player callbacks and session tokens
    private final Map<Integer, LoginCallbackService> activePlayerCallbacks =
            Collections.synchronizedMap(new HashMap<>());
    private final Map<Integer, String> sessionTokens =
            Collections.synchronizedMap(new HashMap<>());

    /** new: allow external code to check if a token is known/valid */
    public synchronized boolean isTokenValid(String token) {
        return sessionTokens.containsValue(token);
    }

    @Override
    public synchronized String login(String username, String password, IntHolder playerID, LoginCallbackService cb)
            throws AuthenticationException, AlreadyLoggedInException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // 1. Verify credentials
            String query = "SELECT player_id, password, is_logged_in FROM players WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                throw new AuthenticationException("Invalid username or password.");
            }

            int id = rs.getInt("player_id");
            String storedPwd = rs.getString("password");
            boolean isLoggedIn = rs.getBoolean("is_logged_in");

            if (!storedPwd.equals(password)) {
                throw new AuthenticationException("Invalid username or password.");
            }

            // 2. If already logged in, force logout via callback
            if (isLoggedIn && activePlayerCallbacks.containsKey(id)) {
                try {
                    String oldToken = sessionTokens.get(id);
                    activePlayerCallbacks.get(id).notifyForcedLogout(id, oldToken);
                } catch (Exception e) {
                    System.err.println("[AuthService] Failed to notify previous session for user " + id);
                    e.printStackTrace();
                }
                // Clean up old session
                activePlayerCallbacks.remove(id);
                sessionTokens.remove(id);
            }

            // 3. Update DB: mark logged in
            PreparedStatement update = conn.prepareStatement(
                    "UPDATE players SET is_logged_in = 1 WHERE player_id = ?");
            update.setInt(1, id);
            update.executeUpdate();

            // 4. Generate new session token and store callback
            String newToken = UUID.randomUUID().toString();
            activePlayerCallbacks.put(id, cb);
            sessionTokens.put(id, newToken);

            // 5. Return values
            playerID.value = id;
            return newToken;

        } catch (SQLException ex) {
            System.err.println("[AuthService] Database error during login: " + ex.getMessage());
            throw new AuthenticationException("Internal server error.");
        }
    }

    @Override
    public synchronized void logout(int playerID, String sessionToken)
            throws NotLoggedInException {
        // Validate session
        String token = sessionTokens.get(playerID);
        if (token == null || !token.equals(sessionToken)) {
            throw new NotLoggedInException();
        }
        // Update DB and clear session
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement update = conn.prepareStatement(
                    "UPDATE players SET is_logged_in = 0 WHERE player_id = ?");
            update.setInt(1, playerID);
            update.executeUpdate();

            activePlayerCallbacks.remove(playerID);
            sessionTokens.remove(playerID);
        } catch (SQLException ex) {
            System.err.println("[AuthService] Database error during logout: " + ex.getMessage());
            // ignore or log
        }
    }

    @Override
    public String adminLogin(String username, String password, IntHolder adminID)
            throws AuthenticationException, AlreadyLoggedInException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // 1. Verify credentials
            String query = "SELECT admin_id, password FROM admin WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                throw new AuthenticationException("Account does not exist.");
            }

            int id = rs.getInt("admin_id");
            String dbPassword = rs.getString("password");

            if (!dbPassword.equals(password)) {
                throw new AuthenticationException("Invalid password.");
            }

            // 2. Update DB: mark logged in
            PreparedStatement update = conn.prepareStatement("UPDATE admin SET is_logged_in = 1 WHERE admin_id = ?");
            update.setInt(1, id);
            update.executeUpdate();

            // 3. Generate new session token and store callback
            String newToken = UUID.randomUUID().toString();
            sessionTokens.put(id, newToken);

            // 4. Return values
            adminID.value = id;
            System.out.println("[SERVER] [ADMIN LOGIN SUCCESS] " + username + " (adminID=" + id + ") logged in.");
            return newToken;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    @Override
    public void adminLogout(int adminID, String sessionToken) throws NotLoggedInException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE admin SET is_logged_in = 0 WHERE admin_id = ?")) {

            stmt.setInt(1, adminID);
            stmt.executeUpdate();

            sessionTokens.remove(adminID);

            System.out.println("[SERVER] [ADMIN LOGOUT SUCCESS] for adminID=" + adminID);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new NotLoggedInException("Database error: " + e.getMessage());
        }
    }
}
