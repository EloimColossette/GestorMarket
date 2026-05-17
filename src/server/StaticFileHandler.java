package server;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.nio.file.Files;

public class StaticFileHandler implements HttpHandler {
    private final String basePath;

    public StaticFileHandler(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        // CORS headers sempre
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Preflight
        if (method.equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        // StaticFileHandler só serve arquivos estáticos (GET)
        // POST/PUT/DELETE não deveriam chegar aqui
        if (!method.equalsIgnoreCase("GET")) {
            String response = "{\"success\":false,\"message\":\"Rota não encontrada\",\"data\":null}";
            byte[] bytes = response.getBytes();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(404, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
            return;
        }

        String path = exchange.getRequestURI().getPath();

        if (path.equals("/")) {
            path = "/html/login.html";
        }

        if (path.endsWith(".html") && !path.startsWith("/html/")) {
            path = "/html" + path;
        }

        File file = new File(basePath, path);
        System.out.println("Buscando arquivo em: " + file.getAbsolutePath());

        if (!file.exists()) {
            String response = "404 - Arquivo não encontrado";
            exchange.sendResponseHeaders(404, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
            return;
        }

        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null) contentType = "application/octet-stream";
        exchange.getResponseHeaders().add("Content-Type", contentType);
        byte[] bytes = Files.readAllBytes(file.toPath());
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}