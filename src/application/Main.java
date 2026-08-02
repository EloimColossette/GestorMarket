package application;

import config.LogConfig;
import server.Server;
import server.AnalyticsProcessLauncher;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            // configure() must be called before Server.start()
            // so that EnvLoader (and JwtUtil's static block) has already loaded the .env
            LogConfig.configure();

            logger.info("Starting server...");

            Server.start();

            // Sobe o microsservico Python (graficos/analytics) junto com o Java
            AnalyticsProcessLauncher.start();

        } catch (Exception e) {
            logger.severe("Failed to start server!");
            logger.log(Level.SEVERE, "Error details: ", e);
        }
    }
}