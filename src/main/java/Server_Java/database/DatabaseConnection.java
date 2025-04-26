package Server_Java.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/whatstheword_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    static {
        try {
            // Load the JDBC driver once when the class is first loaded
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("[Database] MySQL JDBC Driver Registered.");
        } catch (ClassNotFoundException e) {
            System.err.println("[Database ERROR] MySQL JDBC Driver not found.");
            e.printStackTrace();
        }
    }

    // Get a new database connection
    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[Database] Connection established successfully.");
            return conn;
        } catch (SQLException e) {
            System.err.println("[Database ERROR] Failed to connect: " + e.getMessage());
            throw e;
        }
    }
}
