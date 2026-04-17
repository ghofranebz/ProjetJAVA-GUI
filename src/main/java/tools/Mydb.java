package tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Mydb {

    private static Mydb instance;
    private Connection connection;

    private final String URL = "jdbc:mysql://localhost:3306/purrly";
    private final String user = "root";
    private final String password = "";

    private Mydb() {
        try {
            connection = DriverManager.getConnection(URL, user, password);
            System.out.println("connected");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Mydb getInstance() {
        if (instance == null) {
            instance = new Mydb();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}