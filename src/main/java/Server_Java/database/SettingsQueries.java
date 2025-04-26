package Server_Java.database;

import java.sql.*;

public class SettingsQueries {
//    private static Connection con = DatabaseConnection.setCon(); // creates the connection to the database
//    private static String query; // stores the sql query
//    private static Statement stmt; // executes a query
//    private static ResultSet resultSet; // stores the executed stmt query
//    private static PreparedStatement preparedStatement; // prepares and executes parameterized sql statement
//
//    /**
//     * This method updates the current waiting time value
//     * with the method parameter
//     */
//    public static void updateWaitingTime(int waitTime) {
//        if (con == null) {
//            System.out.println("null ung con");
//        }
//        query = "UPDATE settings " +
//                "SET waiting_time = ?";
//        try {
//            preparedStatement = con.prepareStatement(query);
//            preparedStatement.setInt(1, waitTime);
//            preparedStatement.executeUpdate();
//        } catch (SQLException e1) {
//            e1.printStackTrace();
//        } catch (Exception e2) {
//            e2.printStackTrace();
//        }
//    }
//
//    /**
//     * This method updates the current round duration value
//     * using the method parameter
//     */
//    public static void updateRoundDuration(int roundTime) {
//        query = "UPDATE settings " +
//                "SET round_duration = ?";
//        try {
//            preparedStatement = con.prepareStatement(query);
//            preparedStatement.setInt(1, roundTime);
//            preparedStatement.executeUpdate();
//        } catch (SQLException e1) {
//            e1.printStackTrace();
//        } catch (Exception e2) {
//            e2.printStackTrace();
//        }
//    }
//
//    /**
//     * This method retrieves the current waiting time value
//     */
//    public static int getCurrentWaitingTime() {
//        query = "SELECT waiting_time FROM settings";
//        try {
//            stmt = con.createStatement();
//            resultSet = stmt.executeQuery(query);
//
//            if (resultSet.next()) {
//                int waitingTime = resultSet.getInt(1);
//                return waitingTime;
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
//     * This method retrieves the currend value of round duration
//     */
//    public static int getCurrentRoundDuration() {
//        query = "SELECT round_duration FROM settings";
//        try {
//            stmt = con.createStatement();
//            resultSet = stmt.executeQuery(query);
//
//            if (resultSet.next()) {
//                int roundDuration = resultSet.getInt(1);
//                return roundDuration;
//            }
//        } catch (SQLException e1) {
//            e1.printStackTrace();
//        } catch (Exception e2) {
//            e2.printStackTrace();
//        }
//        return 0;
//    }
}
