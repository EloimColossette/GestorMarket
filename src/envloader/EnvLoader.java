package envloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class EnvLoader {

    private static final Map<String, String> env = new HashMap<>();

    static {
        // Tenta encontrar o .env subindo os diretórios a partir do user.dir
        File envFile = encontrarEnv();

        if (envFile == null) {
            throw new RuntimeException(
                    "Arquivo .env não encontrado! Diretório atual: " +
                            System.getProperty("user.dir")
            );
        }

        System.out.println("[EnvLoader] Carregando .env de: " + envFile.getAbsolutePath());

        try (BufferedReader br = new BufferedReader(new FileReader(envFile))) {

            String linha;

            while ((linha = br.readLine()) != null) {

                linha = linha.trim();

                if (linha.isEmpty() || linha.startsWith("#")) continue;

                String[] partes = linha.split("=", 2);

                if (partes.length == 2) {
                    String chave = partes[0].trim();
                    String valor = partes[1].trim();
                    env.put(chave, valor);
                    System.out.println("[EnvLoader] Carregado: " + chave + " = " + mascararValor(chave, valor));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar .env: " + e.getMessage(), e);
        }
    }

    public static String get(String chave) {
        String valor = env.get(chave);

        if (valor == null) {
            System.err.println("[EnvLoader] AVISO: chave não encontrada no .env -> " + chave);
        }

        return valor;
    }

    // Sobe até 3 diretórios procurando o .env
    private static File encontrarEnv() {
        File dir = new File(System.getProperty("user.dir"));

        for (int i = 0; i < 4; i++) {
            File candidato = new File(dir, ".env");
            if (candidato.exists()) {
                return candidato;
            }
            dir = dir.getParentFile();
            if (dir == null) break;
        }

        return null;
    }

    // Mascara valores sensíveis no log
    private static String mascararValor(String chave, String valor) {
        String chaveLower = chave.toLowerCase();
        if (chaveLower.contains("password") ||
                chaveLower.contains("secret") ||
                chaveLower.contains("api_key")) {
            return "****";
        }
        return valor;
    }
}