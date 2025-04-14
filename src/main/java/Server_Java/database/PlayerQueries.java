package Server_Java.database;

import PlayerGame.AlreadyLoggedInException;
import PlayerGame.AuthenticationException;
import PlayerGame.Player;
import Shared_Files.PlayerAccount;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerQueries {
    private static Connection con = DatabaseConnection.setCon();

    private static String query;
    private static PreparedStatement preparedStatement;
    private static ResultSet resultSet;


    /**
     * This method will authenticate the player by username and password.
     * It will throw exceptions for invalid authentication and if the account is already logged in.
     *
     * throws AuthenticationException
     * throws AlreadyLoggedInException
     */
    public static Player login(String username, String password) throws AuthenticationException, AlreadyLoggedInException {
        query = "SELECT * FROM players WHERE username = ? AND password = ? ";

        try {
            preparedStatement = con.prepareStatement(query);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int loggedInStatus = resultSet.getInt("is_logged_in");
                if (loggedInStatus == 1) {
                    throw new AlreadyLoggedInException("Account Already Logged in!");
                } else {
                    int playerId = resultSet.getInt("player_id");
                    String fullName = resultSet.getString("full_name");
                    int gameWins = resultSet.getInt("game_wins");

                    updateLoginStatus(playerId);

                    return new Player(playerId, fullName, username, gameWins);
                }
            } else {
                throw new AuthenticationException("Account Does Not Exist in the DATABASE");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method will update the player's login status to 1 in the database
     * which means the account is logged in.
     */
    private static void updateLoginStatus(int playerId){
        query = "UPDATE players SET is_logged_in = 1 WHERE player_id = ?";

        try{
            preparedStatement = con.prepareStatement(query);
            preparedStatement.setInt(1, playerId);
            preparedStatement.executeUpdate();

        }catch (SQLException e){
            throw new RuntimeException(e);
        }catch (Exception e1){
            e1.printStackTrace();
        }
    }

    /**
     *  This method will set the player's login status to 0
     *  which means the player account is logged out.
     */
    public static void logout (int playerId){
        query = "UPDATE players SET is_logged_in = 0 WHERE player_id = ?";

        try{
            preparedStatement = con.prepareStatement(query);
            preparedStatement.setInt(1, playerId);
            preparedStatement.executeUpdate();

        }catch (SQLException e){
            throw new RuntimeException(e);
        }catch (Exception e1){
            e1.printStackTrace();
        }
    }

    /**
     * This method will retrieve the details or data by the player_id in the database.
     */
    public static Player getPlayer(int playerId) {
        query = "SELECT * FROM players WHERE player_id = ?";
        try {
            preparedStatement = con.prepareStatement(query);
            preparedStatement.setInt(1, playerId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return new Player(
                        playerId,
                        resultSet.getString("full_name"),
                        resultSet.getString("username"),
                        resultSet.getInt("game_wins")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method will update the game wins of the players for the list of players in the database.
     */
    public static void updatePlayerWins (List<Player> playerList){
        query = "UPDATE players SET game_wins = ? WHERE player_id = ?";

        try{
            for (Player player : playerList){
                preparedStatement = con.prepareStatement(query);
                preparedStatement.setInt(1, player.gameWins);
                preparedStatement.setInt(2, player.playerID);
                preparedStatement.executeUpdate();

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * This method will fetch the top 10 players ordered by their game wins in the database.
     * So this will be used for the leaderboard.
     */
    public static String[] fetchTopPlayers(){
        List<String> topPlayers = new ArrayList<>();

        query = "SELECT * FROM players ORDER BY game_wins DESC LIMIT 10";

        try{
           preparedStatement = con.prepareStatement(query);
           resultSet = preparedStatement.executeQuery();

           while (resultSet.next()){
               int playerId = resultSet.getInt("player_id");
               String fullName = resultSet.getString("full_name");
               String username = resultSet.getString("username");
               int gameWins = resultSet.getInt("game_wins");

               Player player = new Player(playerId,fullName, username, gameWins);
               topPlayers.add(player.username + "-" + player.gameWins);
           }

        }catch (Exception e){
            e.printStackTrace();
        }
        return topPlayers.toArray(new String[0]);
    }

    /**
     * This method will check if a username already exist in the database.
     */
    public static boolean isUsernameExist (String username) {
        query = "SELECT username FROM players WHERE username = ?";

        try{
            preparedStatement = con.prepareStatement(query);
            preparedStatement.setString(1, username);
            resultSet = preparedStatement.executeQuery();

            if(resultSet.next()){
                return true;
            }

        }catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return false;
    }

    /**
     * This method will insert a new player in the database or a new player account.
     */
    public static void createNewPlayer (String fullName, String username, String password){
        query ="INSERT INTO players (full_name, username, password, game_wins, is_logged_in)" +
               "VALUES (?, ?, ?, ?, ?); ";

        try{
            preparedStatement = con.prepareStatement(query);
            preparedStatement.setString(1, fullName);
            preparedStatement.setString(2, username);
            preparedStatement.setString(3, password);
            preparedStatement.setInt(4, 0);
            preparedStatement.setInt(5, 0);

            preparedStatement.executeUpdate();

        }catch (SQLException e){
            throw new RuntimeException(e);
        }catch (Exception e1){
            e1.printStackTrace();
        }
    }

    /**
     * This method will retrieve a list of all the player accounts in the database for the admin to use;
     */
    public static List<PlayerAccount> getPlayerList (){
        List<PlayerAccount> playerAccountList = new ArrayList<>();

        query = "SELECT player_id, full_name, username, password, game_wins FROM players";

        try{
            Statement statement = con.createStatement();
            resultSet = statement.executeQuery(query);

            while (resultSet.next()){
                int playerId = resultSet.getInt("player_id");
               // String fullName = resultSet.getString("fullname");
                String username = resultSet.getString("username");
                String password= resultSet.getString("password");
                int gameWins = resultSet.getInt("game_wins");

                PlayerAccount playerAccount = new PlayerAccount(playerId, username, password, gameWins);
                playerAccountList.add(playerAccount);
            }

        }catch (SQLException e){
            throw new RuntimeException(e);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return playerAccountList;
    }

    /**
     * This method will delete a player in the database using the player_id;
     */
    public static void deletePlayer (int playerId){
        query = "DELETE FROM players WHERE player_id = ?";

        try{
            preparedStatement = con.prepareStatement(query);
            preparedStatement.setInt(1, playerId);

            preparedStatement.executeUpdate();

        }catch (SQLException e){
            throw new RuntimeException(e);
        }catch (Exception e1){
            e1.printStackTrace();
        }
    }

    /**
     * This method will update the existing player information by the edited player details in the database.
     */
    public static void  saveModifiedPlayerDetails(PlayerAccount playerAccount){
        query = "UPDATE players SET username = ?, password = ?, game_wins = ? " +
                "WHERE player_id = ?";

        try{
            preparedStatement = con.prepareStatement(query);
            preparedStatement.setString(1, playerAccount.getUsername());
            preparedStatement.setString(2, playerAccount.getPassword());
            preparedStatement.setInt(3, playerAccount.getGame_wins());
            preparedStatement.setInt(4, playerAccount.getPlayer_id());

            preparedStatement.executeUpdate();

        }catch (SQLException e){
            throw new RuntimeException(e);
        }catch (Exception e1){
            e1.printStackTrace();
        }
    }

}
