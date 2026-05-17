package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.ApiResponse;
import exception.ApiException;
import server.StaticFileHandler;
import service.PasswordResetService;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class PasswordResetController implements HttpHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            String method = exchange.getRequestMethod();
            String path   = exchange.getRequestURI().getPath();

            System.out.println("REQUEST: " + method + " " + path);

            if (method.equalsIgnoreCase("OPTIONS")) {
                sendEmpty(exchange, 204);
                return;
            }

            // GET redireciona pro StaticFileHandler (ex: /password/reset-password.html)
            if (method.equalsIgnoreCase("GET")) {
                new StaticFileHandler("public").handle(exchange);
                return;
            }

            if (!method.equalsIgnoreCase("POST")) {
                sendJson(exchange,
                        new ApiResponse(false, "Método não permitido", null),
                        405);
                return;
            }

            String endpoint = path.replace("/password", "");

            switch (endpoint) {
                case "/forgot-password":
                    handleForgotPassword(exchange);
                    break;
                case "/reset-password":
                    handleResetPassword(exchange);
                    break;
                default:
                    sendJson(exchange,
                            new ApiResponse(false, "Rota não encontrada", path),
                            404);
            }

        } catch (ApiException e) {
            sendJson(exchange,
                    new ApiResponse(false, e.getMessage(), null),
                    e.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange,
                    new ApiResponse(false, "Erro interno do servidor", null),
                    500);
        }
    }

    private void handleForgotPassword(HttpExchange exchange) throws Exception {
        String body   = readBody(exchange);
        JsonNode json = objectMapper.readTree(body);

        if (!json.has("email")) {
            throw new ApiException("Email obrigatório", 400);
        }

        String email = json.get("email").asText();
        passwordResetService.solicitarResetSenha(email);

        sendJson(exchange,
                new ApiResponse(true, "Link de recuperação enviado", null),
                200);
    }

    private void handleResetPassword(HttpExchange exchange) throws Exception {
        String body   = readBody(exchange);
        JsonNode json = objectMapper.readTree(body);

        if (!json.has("token") || !json.has("newPassword")) {
            throw new ApiException("Token e nova senha são obrigatórios", 400);
        }

        String token       = json.get("token").asText();
        String newPassword = json.get("newPassword").asText();

        passwordResetService.redefinirSenha(token, newPassword);

        sendJson(exchange,
                new ApiResponse(true, "Senha redefinida com sucesso", null),
                200);
    }

    private String readBody(HttpExchange exchange) throws Exception {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void sendJson(HttpExchange exchange, ApiResponse response, int status) {
        try {
            String json  = objectMapper.writeValueAsString(response);
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendEmpty(HttpExchange exchange, int status) {
        try {
            exchange.sendResponseHeaders(status, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}