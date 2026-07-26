package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.CreatePurchaseDTO;
import exception.ApiException;
import model.PurchaseModel;
import service.PurchaseService;
import util.JsonUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PurchaseController implements HttpHandler {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String method = exchange.getRequestMethod();

        switch (method) {
            case "OPTIONS" -> { exchange.sendResponseHeaders(204, -1); exchange.close(); }
            case "GET"     -> getAllPurchases(exchange);
            case "POST"    -> createPurchase(exchange);
            case "PUT"     -> updatePurchase(exchange);
            case "DELETE"  -> deletePurchase(exchange);
            default -> { exchange.sendResponseHeaders(405, -1); exchange.close(); }
        }
    }

    private void getAllPurchases(HttpExchange exchange) throws IOException {

        Integer userId = (Integer) exchange.getAttribute("authUserId");

        List<PurchaseModel> purchases = purchaseService.findAllPurchases(userId);
        String response = JsonUtil.getGson().toJson(purchases);

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
        exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
        exchange.close();
    }

    private void createPurchase(HttpExchange exchange) throws IOException {
        try {
            Integer userId = (Integer) exchange.getAttribute("authUserId");

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            CreatePurchaseDTO dto = JsonUtil.getGson().fromJson(body, CreatePurchaseDTO.class);

            purchaseService.savePurchase(dto, userId);

            String response = "Purchase created successfully";
            exchange.sendResponseHeaders(201, response.getBytes(StandardCharsets.UTF_8).length);
            exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));

        } catch (ApiException e) {
            sendError(exchange, e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            sendError(exchange, e.getMessage(), 400);
        }
        exchange.close();
    }

    private void updatePurchase(HttpExchange exchange) throws IOException {
        try {
            Integer userId = (Integer) exchange.getAttribute("authUserId");

            String[] parts = exchange.getRequestURI().getPath().split("/");
            if (parts.length < 3) { sendError(exchange, "Missing purchase id", 400); return; }
            Integer id = Integer.parseInt(parts[2]);

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            CreatePurchaseDTO dto = JsonUtil.getGson().fromJson(body, CreatePurchaseDTO.class);

            purchaseService.updatePurchase(id, dto, userId);

            String response = "Purchase updated successfully";
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));

        } catch (ApiException e) {
            sendError(exchange, e.getMessage(), e.getStatusCode());
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid purchase id", 400);
        } catch (Exception e) {
            sendError(exchange, e.getMessage(), 400);
        }
        exchange.close();
    }

    private void deletePurchase(HttpExchange exchange) throws IOException {
        try {
            Integer userId = (Integer) exchange.getAttribute("authUserId");

            String[] parts = exchange.getRequestURI().getPath().split("/");
            if (parts.length < 3) { sendError(exchange, "Missing purchase id", 400); return; }
            Integer id = Integer.parseInt(parts[2]);

            purchaseService.deletePurchase(id, userId);

            String response = "Purchase deleted successfully";
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));

        } catch (ApiException e) {
            sendError(exchange, e.getMessage(), e.getStatusCode());
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid purchase id", 400);
        } catch (Exception e) {
            sendError(exchange, e.getMessage(), 400);
        }
        exchange.close();
    }

    private void sendError(HttpExchange exchange, String message, int status) throws IOException {
        String response = message != null ? message : "Unexpected error";
        exchange.sendResponseHeaders(status, response.getBytes(StandardCharsets.UTF_8).length);
        exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
    }
}