package database;

import java.sql.Connection;
import java.sql.DriverManager;
import envloader.EnvLoader;

public class Database {

    private static final String URL      = EnvLoader.get("DB_URL");
    private static final String DB_USER  = EnvLoader.get("DB_USER");
    private static final String PASSWORD = EnvLoader.get("DB_PASSWORD");

    // Renamed from conectar() to connect()
    public static Connection connect() {
        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(URL, DB_USER, PASSWORD);
            System.out.println("Connected to database!");
            return conn;
        } catch (Exception e) {
            System.out.println("Error connecting to database: " + e.getMessage());
            return null;
        }
    }
}
