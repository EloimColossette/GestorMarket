package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class DocsHandler implements HttpHandler {

    private static final String SWAGGER_HTML_RESOURCE = "/Swagger.html";
    private static final String OPENAPI_YAML_RESOURCE  = "/Openapi.yaml";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        if (method.equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        if (!method.equalsIgnoreCase("GET")) {
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return;
        }

        String path = exchange.getRequestURI().getPath();

        try {
            if (path.equals("/openapi.yaml")) {
                serveResource(exchange, OPENAPI_YAML_RESOURCE, "application/yaml; charset=UTF-8");
            } else {
                // /docs (e qualquer sub-caminho de /docs) -> sempre devolve o Swagger UI
                serveResource(exchange, SWAGGER_HTML_RESOURCE, "text/html; charset=UTF-8");
            }
        } finally {
            exchange.close();
        }
    }

    private void serveResource(HttpExchange exchange, String resourcePath, String contentType) throws IOException {
        byte[] bytes = readResource(resourcePath);

        if (bytes == null) {
            String notFound = "Documentação não encontrada (" + resourcePath + " ausente no classpath)";
            byte[] body = notFound.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(404, body.length);
            exchange.getResponseBody().write(body);
            return;
        }

        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private byte[] readResource(String resourcePath) throws IOException {
        try (InputStream is = DocsHandler.class.getResourceAsStream(resourcePath)) {
            if (is == null) return null;

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int read;
            while ((read = is.read(chunk)) != -1) {
                buffer.write(chunk, 0, read);
            }
            return buffer.toByteArray();
        }
    }
}