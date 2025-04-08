package Server_Java.database;

import java.sql.*;

public class DatabaseConnection {
    private static Connection con;

    public static Connection setCon(){
        try{
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/whatstheword_db?user=root&password");
        }catch (SQLException e){
            System.out.println("Database Connection Failed");
        }
        return con;
    }
}
