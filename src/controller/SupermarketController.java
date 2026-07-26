package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.CreateSupermarketDTO;
import exception.ApiException;
import model.SupermarketModel;
import service.SupermarketService;
import util.JsonUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SupermarketController implements HttpHandler {

    private final SupermarketService supermarketService;

    public SupermarketController(SupermarketService supermarketService) {
        this.supermarketService = supermarketService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String method = exchange.getRequestMethod();

        switch (method) {

            case "OPTIONS" -> { exchange.sendResponseHeaders(204, -1); exchange.close(); }

            case "GET" -> getAllSupermarkets(exchange);

            case "POST" -> createSupermarket(exchange);

            case "PUT" -> updateSupermarket(exchange);

            case "DELETE" -> deleteSupermarket(exchange);

            default -> {
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
            }
        }
    }

    // =========================================
    // GET ALL — só os supermercados do usuário logado
    // =========================================
    private void getAllSupermarkets(HttpExchange exchange) throws IOException {

        Integer userId = (Integer) exchange.getAttribute("authUserId");

        List<SupermarketModel> supermarkets =
                supermarketService.findAllSupermarkets(userId);

        String response = JsonUtil.getGson().toJson(supermarkets);

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
        exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
        exchange.close();
    }

    // =========================================
    // POST — cria supermercado vinculado ao usuário logado
    // =========================================
    private void createSupermarket(HttpExchange exchange) throws IOException {

        try {
            Integer userId = (Integer) exchange.getAttribute("authUserId");

            String body = new String(
                    exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            CreateSupermarketDTO dto =
                    JsonUtil.getGson().fromJson(body, CreateSupermarketDTO.class);

            supermarketService.saveSupermarket(dto, userId);

            String response = "Supermarket created successfully";
            exchange.sendResponseHeaders(201, response.getBytes(StandardCharsets.UTF_8).length);
            exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));

        } catch (ApiException e) {
            sendError(exchange, e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            sendError(exchange, e.getMessage(), 400);
        }

        exchange.close();
    }

    // =========================================
    // PUT — só atualiza se o supermercado for do usuário logado
    // =========================================
    private void updateSupermarket(HttpExchange exchange) throws IOException {

        try {
            Integer userId = (Integer) exchange.getAttribute("authUserId");

            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");

            if (parts.length < 3) {
                sendError(exchange, "Missing supermarket id", 400);
                return;
            }

            Integer id = Integer.parseInt(parts[2]);

            String body = new String(
                    exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            CreateSupermarketDTO dto =
                    JsonUtil.getGson().fromJson(body, CreateSupermarketDTO.class);

            supermarketService.updateSupermarket(id, dto, userId);

            String response = "Supermarket updated successfully";
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));

        } catch (ApiException e) {
            sendError(exchange, e.getMessage(), e.getStatusCode());
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid supermarket id", 400);
        } catch (Exception e) {
            sendError(exchange, e.getMessage(), 400);
        }

        exchange.close();
    }

    // =========================================
    // DELETE — só apaga se o supermercado for do usuário logado
    // =========================================
    private void deleteSupermarket(HttpExchange exchange) throws IOException {

        try {
            Integer userId = (Integer) exchange.getAttribute("authUserId");

            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");

            if (parts.length < 3) {
                sendError(exchange, "Missing supermarket id", 400);
                return;
            }

            Integer id = Integer.parseInt(parts[2]);

            supermarketService.deleteSupermarket(id, userId);

            String response = "Supermarket deleted successfully";
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));

        } catch (ApiException e) {
            sendError(exchange, e.getMessage(), e.getStatusCode());
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid supermarket id", 400);
        } catch (Exception e) {
            sendError(exchange, e.getMessage(), 400);
        }

        exchange.close();
    }

    // =========================================
    // Helper de erro padronizado
    // =========================================
    private void sendError(HttpExchange exchange, String message, int status) throws IOException {
        String response = message != null ? message : "Unexpected error";
        exchange.sendResponseHeaders(status, response.getBytes(StandardCharsets.UTF_8).length);
        exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
    }
}