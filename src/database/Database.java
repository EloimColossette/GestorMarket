package database;

import java.sql.Connection;
import java.sql.DriverManager;
import envloader.EnvLoader;

public class Database {

    private static final String URL = EnvLoader.get("DB_URL");
    private static final String DB_USER = EnvLoader.get("DB_USER");
    private static final String PASSWORD = EnvLoader.get("DB_PASSWORD");

    public static Connection conectar() {
        try {
            Class.forName("org.postgresql.Driver");

            Connection conn = DriverManager.getConnection(URL, DB_USER, PASSWORD);

            System.out.println("Conectado ao banco de dados!");

            return conn;

        } catch (Exception e) {
            System.out.println("Erro ao conectar no banco: " + e.getMessage());
            return null;
        }
    }
}