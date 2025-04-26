package Server_Java.database;

import java.sql.*;
import java.util.List;

public class GameQueries {
//    private static Connection con = DatabaseConnection.setCon(); // creates the connection to the database
//    private static String query; // stores the sql query
//    private static Statement stmt; // executes a query
//    private static ResultSet resultSet; // stores the executed stmt query
//    private static PreparedStatement preparedStatement; // prepares and executes parameterized sql statement
//
//    /**
//     * This method adds the list of players playing a game into the game_players table
//     */
//    public static void addGamePlayers(List<Player> playersData, int gid) {
//        query = "INSERT INTO game_players (game_id, player_id) "+
//                "VALUES (?, ?); ";
//        try {
//            preparedStatement = con.prepareStatement(query);
//
//            for (Player player : playersData){
//                preparedStatement.setInt(1, gid);
//                preparedStatement.setInt(2, player.playerID);
//                preparedStatement.execute();
//            }
//
//        } catch (SQLException e1) {
//            e1.printStackTrace();
//        } catch (Exception e2) {
//            e2.printStackTrace();
//        }
//    }
//
//    /**
//     * This method retrieves the game id of the last saved game in the games table
//     */
//    public static int getLastGameID() {
//        query = "SELECT MAX(game_id) FROM games";
//        try {
//            stmt = con.createStatement();
//            resultSet = stmt.executeQuery(query);
//
//            if (resultSet.next()) {
//                int lastGameID = resultSet.getInt(1);
//                return lastGameID;
//            }
//        } catch (SQLException e1) {
//            e1.printStackTrace();
//        } catch (Exception e2) {
//            e2.printStackTrace();
//        }
//        return 0;
//    }
//
//    /**
//     * This method saves the game id only of the current session
//     * since it is still in the lobby state
//     */
//    public static void saveGameID(int gid) {
//        query = "INSERT INTO games(game_id) " +
//                "VALUES(?)";
//        try {
//            preparedStatement = con.prepareStatement(query);
//            preparedStatement.setInt(1, gid);
//            preparedStatement.executeUpdate();
//        } catch (SQLException e1) {
//            e1.printStackTrace();
//        } catch (Exception e2) {
//            e2.printStackTrace();
//        }
//    }
//
//    /**
//     * This method save the details of a round in a game
//     */
//    public static void saveRound(int gid, int roundNum, int wLength, int winner, Date start, Date end) {
//        query = "INSERT INTO rounds(game_id, round_number, word_length, round_winner, start_time, end_time)" +
//                "VALUES(?, ?, ?, ?, ?, ?)";
//        try {
//            preparedStatement = con.prepareStatement(query);
//            preparedStatement.setInt(1, gid);
//            preparedStatement.setInt(2, roundNum);
//            preparedStatement.setInt(3, wLength);
//            preparedStatement.setInt(4, winner);
//            preparedStatement.setDate(5, start);
//            preparedStatement.setDate(6, end);
//            preparedStatement.executeUpdate();
//        } catch (SQLException e1) {
//            e1.printStackTrace();
//        } catch (Exception e2) {
//            e2.printStackTrace();
//        }
//    }
//
//    /**
//     * This method saves the game details in the games table
//     */
//    public static void saveGame(int gid, int tRound, int winner, String gStat, Date start, Date end) {
//        query = "UPDATE games " +
//                "SET total_rounds = ?, game_winner = ?, game_status = ?, start_time = ?, end_time = ?" +
//                "WHERE game_id = ?";
//        try {
//            preparedStatement = con.prepareStatement(query);
//            preparedStatement.setInt(1, tRound);
//            preparedStatement.setInt(2, winner);
//            preparedStatement.setString(3, gStat);
//            preparedStatement.setDate(4, start);
//            preparedStatement.setDate(5, end);
//            preparedStatement.setInt(6, gid);
//            preparedStatement.executeUpdate();
//        } catch (SQLException e1) {
//            e1.printStackTrace();
//        } catch (Exception e2) {
//            e2.printStackTrace();
//        }
//    }
}
