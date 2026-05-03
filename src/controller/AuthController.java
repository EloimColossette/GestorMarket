package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.ApiResponse;
import exception.ApiException;
import service.AuthService;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class AuthController implements HttpHandler {

    private static final ObjectMapper mapper = new ObjectMapper();

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void handle(HttpExchange exchange) {

        try {
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendResponse(exchange,
                        new ApiResponse(false, "Método não permitido", null),
                        405);
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonNode json = mapper.readTree(body);

            if (!json.has("email") || !json.has("password")) {
                throw new ApiException("Email e senha são obrigatórios", 400);
            }

            String email = json.get("email").asText();
            String password = json.get("password").asText();

            String token = authService.login(email, password);

            sendResponse(exchange,
                    new ApiResponse(true, "Login realizado com sucesso", token),
                    200
            );

        } catch (ApiException e) {
            sendResponse(exchange,
                    new ApiResponse(false, e.getMessage(), null),
                    e.getStatusCode()
            );

        } catch (Exception e) {
            e.printStackTrace();

            sendResponse(exchange,
                    new ApiResponse(false, "Erro interno do servidor", null),
                    500
            );

        } finally {
            exchange.close();
        }
    }

    private void sendResponse(HttpExchange exchange, ApiResponse response, int status) {
        try {
            String json = mapper.writeValueAsString(response);

            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

            exchange.sendResponseHeaders(status, bytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}