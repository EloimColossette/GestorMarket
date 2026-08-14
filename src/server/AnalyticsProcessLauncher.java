package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AnalyticsProcessLauncher {

    private static final Logger logger =
            Logger.getLogger(AnalyticsProcessLauncher.class.getName());

    private static Process pythonProcess;

    public static void start() {

        // ============================================================
        // DIRETÓRIO DO PROJETO
        // ============================================================

        Path projectDir = Paths.get("")
                .toAbsolutePath()
                .normalize();

        Path analyticsDir = projectDir.resolve("python_analytics");

        Path serverScript = analyticsDir.resolve("server.py");

        Path pythonExecutable = projectDir
                .resolve(".venv")
                .resolve("bin")
                .resolve("python")
                .normalize();

        // ============================================================
        // VALIDAÇÕES
        // ============================================================

        logger.info("==================================================");
        logger.info("INICIANDO SERVICO PYTHON");
        logger.info("Projeto: " + projectDir);
        logger.info("Python: " + pythonExecutable);
        logger.info("Analytics: " + analyticsDir);
        logger.info("Script: " + serverScript);
        logger.info("==================================================");

        if (!Files.exists(serverScript)) {
            logger.severe(
                    "server.py nao encontrado: "
                            + serverScript
            );
            return;
        }

        if (!Files.exists(pythonExecutable)) {
            logger.severe(
                    "Python do .venv nao encontrado: "
                            + pythonExecutable
            );
            return;
        }

        if (!Files.isExecutable(pythonExecutable)) {
            logger.severe(
                    "Python nao possui permissao de execucao: "
                            + pythonExecutable
            );
            return;
        }

        try {

            // ========================================================
            // DETECTA SE ESTAMOS DENTRO DE FLATPAK
            // ========================================================

            boolean isFlatpak =
                    Files.exists(Paths.get("/.flatpak-info"));

            logger.info(
                    "Executando dentro do Flatpak: "
                            + isFlatpak
            );

            // ========================================================
            // MONTA O COMANDO
            // ========================================================

            ProcessBuilder builder;

            if (isFlatpak) {

                /*
                 * IntelliJ instalado via Flatpak.
                 *
                 * O Python do Fedora precisa ser executado fora
                 * do sandbox através do flatpak-spawn --host.
                 */

                logger.info(
                        "IntelliJ Flatpak detectado."
                );

                logger.info(
                        "Usando flatpak-spawn --host."
                );

                builder = new ProcessBuilder(
                        "/usr/bin/flatpak-spawn",
                        "--host",
                        pythonExecutable.toString(),
                        serverScript.toString()
                );

            } else {

                /*
                 * Execucao normal fora do Flatpak.
                 */

                logger.info(
                        "Executando fora do Flatpak."
                );

                builder = new ProcessBuilder(
                        pythonExecutable.toString(),
                        serverScript.toString()
                );
            }

            // ========================================================
            // DIRETÓRIO DE EXECUÇÃO
            // ========================================================

            builder.directory(analyticsDir.toFile());

            // Junta stdout + stderr
            builder.redirectErrorStream(true);

            // ========================================================
            // AMBIENTE
            // ========================================================

            Map<String, String> environment =
                    builder.environment();

            /*
             * Impede que configurações externas de Python
             * interfiram no ambiente virtual.
             */
            environment.put(
                    "PYTHONNOUSERSITE",
                    "1"
            );

            /*
             * Garante que o Python encontre os pacotes
             * instalados no .venv.
             */
            environment.put(
                    "VIRTUAL_ENV",
                    projectDir
                            .resolve(".venv")
                            .toString()
            );

            /*
             * Não herdamos PYTHONHOME.
             */
            environment.remove("PYTHONHOME");

            /*
             * Não deixamos PYTHONPATH externo interferir.
             */
            environment.remove("PYTHONPATH");

            // ========================================================
            // INICIA PYTHON
            // ========================================================

            pythonProcess = builder.start();

            logger.info(
                    "Servico de analytics (Python) iniciado."
            );

            logger.info(
                    "PID do Python: "
                            + pythonProcess.pid()
            );

            // ========================================================
            // LÊ LOG DO PYTHON
            // ========================================================

            Thread logThread = new Thread(() -> {

                try (
                        BufferedReader reader =
                                new BufferedReader(
                                        new InputStreamReader(
                                                pythonProcess
                                                        .getInputStream()
                                        )
                                )
                ) {

                    String line;

                    while (
                            (line = reader.readLine()) != null
                    ) {

                        logger.info(
                                "[python-analytics] "
                                        + line
                        );
                    }

                } catch (IOException e) {

                    if (
                            pythonProcess != null
                                    && pythonProcess.isAlive()
                    ) {

                        logger.log(
                                Level.WARNING,
                                "Erro ao ler saida do Python",
                                e
                        );
                    }
                }

            }, "analytics-log-forwarder");

            logThread.setDaemon(true);
            logThread.start();

            // ========================================================
            // MONITORA PROCESSO
            // ========================================================

            Thread monitorThread = new Thread(() -> {

                try {

                    int exitCode =
                            pythonProcess.waitFor();

                    if (exitCode == 0) {

                        logger.info(
                                "Servico Python terminou normalmente."
                        );

                    } else {

                        logger.warning(
                                "Servico Python terminou. "
                                        + "Codigo: "
                                        + exitCode
                        );
                    }

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();

                    logger.log(
                            Level.WARNING,
                            "Monitor do Python interrompido",
                            e
                    );
                }

            }, "analytics-process-monitor");

            monitorThread.setDaemon(true);
            monitorThread.start();

            // ========================================================
            // SHUTDOWN HOOK
            // ========================================================

            Runtime.getRuntime().addShutdownHook(
                    new Thread(() -> {

                        if (
                                pythonProcess != null
                                        && pythonProcess.isAlive()
                        ) {

                            logger.info(
                                    "Encerrando servico de analytics..."
                            );

                            pythonProcess.destroy();

                            try {

                                if (
                                        !pythonProcess.waitFor(
                                                5,
                                                java.util.concurrent.TimeUnit.SECONDS
                                        )
                                ) {

                                    logger.warning(
                                            "Python nao encerrou. "
                                                    + "Forcando encerramento."
                                    );

                                    pythonProcess.destroyForcibly();
                                }

                            } catch (InterruptedException e) {

                                Thread.currentThread()
                                        .interrupt();

                                pythonProcess
                                        .destroyForcibly();
                            }
                        }

                    }, "analytics-shutdown-hook")
            );

        } catch (IOException e) {

            logger.log(
                    Level.SEVERE,
                    "Falha ao iniciar o servico de analytics (Python)",
                    e
            );
        }
    }
}

