package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AnalyticsProcessLauncher {

    private static final Logger logger = Logger.getLogger(AnalyticsProcessLauncher.class.getName());
    private static Process pythonProcess;

    public static void start() {
        Path analyticsDir = Paths.get("python_analytics").toAbsolutePath();
        Path serverScript = analyticsDir.resolve("server.py");

        if (!Files.exists(serverScript)) {
            logger.warning("python_analytics/server.py nao encontrado em " + analyticsDir
                    + " — o servico de analytics (graficos) nao sera iniciado automaticamente. "
                    + "Rode manualmente: cd python_analytics && python server.py");
            return;
        }

        String pythonExecutable = findPythonExecutable();
        if (pythonExecutable == null) {
            logger.warning("Nenhum executavel Python (python3/python) encontrado no PATH. "
                    + "O servico de analytics (graficos) nao sera iniciado automaticamente.");
            return;
        }

        try {
            ProcessBuilder builder = new ProcessBuilder(pythonExecutable, "server.py");
            builder.directory(analyticsDir.toFile());
            builder.redirectErrorStream(true);

            pythonProcess = builder.start();
            logger.info("Servico de analytics (Python) iniciado com '" + pythonExecutable + "' em " + analyticsDir);

            // Encaminha a saida do processo Python para o log do Java (o server.py
            // ja prefixa cada linha com "[python-analytics] ")
            Thread logThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(pythonProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        logger.info(line);
                    }
                } catch (IOException ignored) {
                    // stream fechado quando o processo termina — esperado
                }
            }, "analytics-log-forwarder");
            logThread.setDaemon(true);
            logThread.start();

            // Garante que o processo Python morre junto com o Java
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (pythonProcess != null && pythonProcess.isAlive()) {
                    logger.info("Encerrando servico de analytics (Python)...");
                    pythonProcess.destroy();
                    try {
                        pythonProcess.waitFor();
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, "analytics-shutdown-hook"));

        } catch (IOException e) {
            logger.log(Level.WARNING, "Falha ao iniciar o servico de analytics (Python)", e);
        }
    }

    /**
     * Procura um executavel Python valido no PATH, testando "python3" primeiro
     * (padrao em Linux/macOS) e depois "python" (padrao em Windows).
     */
    private static String findPythonExecutable() {
        for (String candidate : new String[]{"python3", "python"}) {
            try {
                Process check = new ProcessBuilder(candidate, "--version")
                        .redirectErrorStream(true)
                        .start();
                if (check.waitFor() == 0) {
                    return candidate;
                }
            } catch (IOException | InterruptedException ignored) {
                // tenta o proximo candidato
            }
        }
        return null;
    }
}