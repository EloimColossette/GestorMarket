package envloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class EnvLoader {

    private static final Map<String, String> env = new HashMap<>();

    static {
        // Search for .env file walking up from working directory
        File envFile = findEnvFile();

        if (envFile == null) {
            throw new RuntimeException(
                    ".env file not found! Working directory: " +
                            System.getProperty("user.dir")
            );
        }

        System.out.println("[EnvLoader] Loading .env from: " + envFile.getAbsolutePath());

        try (BufferedReader br = new BufferedReader(new FileReader(envFile))) {

            String line;

            while ((line = br.readLine()) != null) {

                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("=", 2);

                if (parts.length == 2) {
                    String key   = parts[0].trim();
                    String value = parts[1].trim();
                    env.put(key, value);
                    System.out.println("[EnvLoader] Loaded: " + key + " = " + maskValue(key, value));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error loading .env: " + e.getMessage(), e);
        }
    }

    public static String get(String key) {
        String value = env.get(key);

        if (value == null) {
            System.err.println("[EnvLoader] WARNING: key not found in .env -> " + key);
        }

        return value;
    }

    // Walks up to 4 directory levels looking for .env
    private static File findEnvFile() {
        File dir = new File(System.getProperty("user.dir"));

        for (int i = 0; i < 4; i++) {
            File candidate = new File(dir, ".env");
            if (candidate.exists()) {
                return candidate;
            }
            dir = dir.getParentFile();
            if (dir == null) break;
        }

        return null;
    }

    // Masks sensitive values in logs
    private static String maskValue(String key, String value) {
        String lowerKey = key.toLowerCase();
        if (lowerKey.contains("password") ||
                lowerKey.contains("secret") ||
                lowerKey.contains("api_key")) {
            return "****";
        }
        return value;
    }
}
