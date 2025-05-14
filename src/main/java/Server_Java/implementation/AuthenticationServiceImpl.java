package Server_Java.implementation;

import Client_Java.player.SessionManager;
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
    private final Map<Integer, LoginCallbackService> activePlayerCallbacks =
            Collections.synchronizedMap(new HashMap<>());
    private final Map<Integer, String> sessionTokens =
            Collections.synchronizedMap(new HashMap<>());

    @Override
    public synchronized String login(String username, String password,
                                     IntHolder playerID, LoginCallbackService cb)
            throws AuthenticationException, AlreadyLoggedInException {
        System.out.println("[DEBUG][AuthService] login() start for username='" + username + "'");
        try (Connection conn = DatabaseConnection.getConnection()) {
            // 1. Verify credentials: fetch player_id, stored password, and login flag
            String lookup =
                    "SELECT player_id, password, is_logged_in " +
                            "FROM players " +
                            "WHERE username = ?";
            PreparedStatement fetchStmt = conn.prepareStatement(lookup);
            fetchStmt.setString(1, username);
            ResultSet rs = fetchStmt.executeQuery();

            if (!rs.next()) {
                System.out.println("[DEBUG][AuthService] login() failed: username not found");
                throw new AuthenticationException("Invalid username or password.");
            }

            int id = rs.getInt("player_id");
            String storedPwd = rs.getString("password");
            boolean isLoggedIn = rs.getBoolean("is_logged_in");
            System.out.println("[DEBUG][AuthService] fetched player_id=" + id
                    + ", is_logged_in=" + isLoggedIn);

            // 2. Check password
            if (!storedPwd.equals(password)) {
                System.out.println("[DEBUG][AuthService] login() failed: bad password for player_id=" + id);
                throw new AuthenticationException("Invalid username or password.");
            }

            // 3. If already logged in, force-logout that session
            if (isLoggedIn && activePlayerCallbacks.containsKey(id)) {
                String oldToken = sessionTokens.get(id);
                System.out.println("[DEBUG][AuthService] existing session for playerID="
                        + id + ", oldToken=" + oldToken + " → forcing logout");
                try {
                    activePlayerCallbacks.get(id).notifyForcedLogout(id, oldToken);
                } catch (Exception e) {
                    System.err.println("[ERROR][AuthService] callback failed for playerID=" + id);
                    e.printStackTrace();
                }
                activePlayerCallbacks.remove(id);
                sessionTokens.remove(id);
                System.out.println("[DEBUG][AuthService] old session removed for playerID=" + id);
            }

            // 4. Mark in DB as logged in
            String markLoggedIn =
                    "UPDATE players SET is_logged_in = 1 WHERE player_id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(markLoggedIn);
            updateStmt.setInt(1, id);
            updateStmt.executeUpdate();
            System.out.println("[DEBUG][AuthService] players.is_logged_in set to 1 for player_id=" + id);

            // 5. Generate new token and store callback
            String newToken = UUID.randomUUID().toString();
            activePlayerCallbacks.put(id, cb);
            sessionTokens.put(id, newToken);

            System.out.println("[DEBUG][AuthService] login() success for playerID="
                    + id + ", newToken=" + newToken);
            playerID.value = id;
            return newToken;

        } catch (SQLException ex) {
            System.err.println("[ERROR][AuthService] DB error during login: " + ex.getMessage());
            throw new AuthenticationException("Database failure");
        }
    }

    @Override
    public synchronized void logout(int playerID, String sessionToken)
            throws NotLoggedInException {
        System.out.println("[DEBUG][AuthService] logout() called for playerID="
                + playerID + ", token=" + sessionToken);
        String expected = sessionTokens.get(playerID);
        if (expected == null || !expected.equals(sessionToken)) {
            System.err.println("[WARN][AuthService] invalid logout token for playerID=" + playerID);
            throw new NotLoggedInException();
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE players SET is_logged_in = 0 WHERE player_id = ?")) {
            stmt.setInt(1, playerID);
            stmt.executeUpdate();
            System.out.println("[DEBUG][AuthService] players.is_logged_in set to 0 for player_id=" + playerID);

            activePlayerCallbacks.remove(playerID);
            sessionTokens.remove(playerID);
            System.out.println("[DEBUG][AuthService] logout() complete for playerID=" + playerID);
        } catch (SQLException ex) {
            System.err.println("[ERROR][AuthService] DB error on logout: " + ex.getMessage());
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
