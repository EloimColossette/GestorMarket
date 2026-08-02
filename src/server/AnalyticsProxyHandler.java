package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AnalyticsProxyHandler implements HttpHandler {

    private static final Logger logger = Logger.getLogger(AnalyticsProxyHandler.class.getName());
    private static final String ANALYTICS_TARGET = "http://127.0.0.1:8000";

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String pathAndQuery = exchange.getRequestURI().getRawPath();
        String query = exchange.getRequestURI().getRawQuery();
        if (query != null) {
            pathAndQuery += "?" + query;
        }

        URL targetUrl;
        try {
            targetUrl = URI.create(ANALYTICS_TARGET + pathAndQuery).toURL();
        } catch (Exception e) {
            sendJsonError(exchange, 400, "URL invalida");
            return;
        }

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) targetUrl.openConnection();
            connection.setRequestMethod(exchange.getRequestMethod());
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(30000);

            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader != null) {
                connection.setRequestProperty("Authorization", authHeader);
            }

            connection.connect();

            int status = connection.getResponseCode();
            String contentType = connection.getContentType();
            if (contentType != null) {
                exchange.getResponseHeaders().set("Content-Type", contentType);
            }

            InputStream responseStream = (status >= 200 && status < 400)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            byte[] body = responseStream != null ? responseStream.readAllBytes() : new byte[0];

            exchange.sendResponseHeaders(status, body.length == 0 ? -1 : body.length);
            if (body.length > 0) {
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(body);
                }
            }

        } catch (IOException e) {
            logger.log(Level.WARNING, "Falha ao repassar requisicao para o servico de analytics (Python). "
                    + "Ele esta rodando na porta 8000?", e);
            sendJsonError(exchange, 502,
                    "Servico de analytics indisponivel. Verifique se o python_analytics/server.py esta rodando.");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void sendJsonError(HttpExchange exchange, int status, String message) throws IOException {
        String json = "{\"erro\":\"" + message.replace("\"", "'") + "\"}";
        byte[] bytes = json.getBytes();
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}