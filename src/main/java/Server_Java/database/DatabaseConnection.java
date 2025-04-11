package Server_Java.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/whatstheword_db"; // Update with your database URL
    private static final String USER = "root"; // Update with your database user
    private static final String PASSWORD = ""; // Update with your database password

    public static Connection setCon() {
        Connection connection = null;
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connection established successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Failed to establish database connection.");
            e.printStackTrace();
        }
        return connection;
    }

}