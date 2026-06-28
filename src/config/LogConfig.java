package config;

import java.util.logging.*;

public class LogConfig {

    // Renamed from config() to configure()
    public static void configure() {

        Logger rootLogger = Logger.getLogger("");

        // Remove default handlers
        for (Handler h : rootLogger.getHandlers()) {
            rootLogger.removeHandler(h);
        }

        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.INFO);

        handler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {

                String color;

                if (record.getLevel() == Level.SEVERE) {
                    color = "\u001B[31m"; // red
                } else if (record.getLevel() == Level.WARNING) {
                    color = "\u001B[33m"; // yellow
                } else {
                    color = "\u001B[32m"; // green
                }

                return color +
                        "[" + record.getLevel() + "] " +
                        record.getMessage() +
                        "\u001B[0m\n";
            }
        });

        rootLogger.setLevel(Level.INFO);
        rootLogger.addHandler(handler);
    }
}
