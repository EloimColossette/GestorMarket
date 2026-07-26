package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.CreatePurchaseItemDTO;
import exception.ApiException;
import model.PurchaseItemModel;
import service.PurchaseItemService;
import util.JsonUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PurchaseItemController implements HttpHandler {

    private final PurchaseItemService purchaseItemService;

    public PurchaseItemController(PurchaseItemService purchaseItemService) {
        this.purchaseItemService = purchaseItemService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        switch (method) {
            case "OPTIONS" -> { exchange.sendResponseHeaders(204, -1); exchange.close(); }
            case "GET"     -> getAll(exchange);
            case "POST"    -> create(exchange);
            case "PUT"     -> update(exchange);
            case "DELETE"  -> delete(exchange);
            default -> { exchange.sendResponseHeaders(405, -1); exchange.close(); }
        }
    }

    private void getAll(HttpExchange exchange) throws IOException {
        Integer userId = (Integer) exchange.getAttribute("authUserId");

        List<PurchaseItemModel> items = purchaseItemService.findAllPurchaseItems(userId);
        String response = JsonUtil.getGson().toJson(items);

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
        exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
        exchange.close();
    }

    private void create(HttpExchange exchange) throws IOException {
        try {
            Integer userId = (Integer) exchange.getAttribute("authUserId");

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            CreatePurchaseItemDTO dto = JsonUtil.getGson().fromJson(body, CreatePurchaseItemDTO.class);

            purchaseItemService.savePurchaseItem(dto, userId);

            String response = "Purchase item created successfully";
            exchange.sendResponseHeaders(201, response.getBytes(StandardCharsets.UTF_8).length);
            exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));

        } catch (ApiException e) {
            sendError(exchange, e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            sendError(exchange, e.getMessage(), 400);
        }
        exchange.close();
    }

    private void update(HttpExchange exchange) throws IOException {
        try {
            Integer userId = (Integer) exchange.getAttribute("authUserId");

            String[] parts = exchange.getRequestURI().getPath().split("/");
            if (parts.length < 3) { sendError(exchange, "Missing purchase item id", 400); return; }
            Integer id = Integer.parseInt(parts[2]);

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            CreatePurchaseItemDTO dto = JsonUtil.getGson().fromJson(body, CreatePurchaseItemDTO.class);

            purchaseItemService.updatePurchaseItem(id, dto, userId);

            String response = "Purchase item updated successfully";
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));

        } catch (ApiException e) {
            sendError(exchange, e.getMessage(), e.getStatusCode());
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid purchase item id", 400);
        } catch (Exception e) {
            sendError(exchange, e.getMessage(), 400);
        }
        exchange.close();
    }

    private void delete(HttpExchange exchange) throws IOException {
        try {
            Integer userId = (Integer) exchange.getAttribute("authUserId");

            String[] parts = exchange.getRequestURI().getPath().split("/");
            if (parts.length < 3) { sendError(exchange, "Missing purchase item id", 400); return; }
            Integer id = Integer.parseInt(parts[2]);

            purchaseItemService.deletePurchaseItem(id, userId);

            String response = "Purchase item deleted successfully";
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));

        } catch (ApiException e) {
            sendError(exchange, e.getMessage(), e.getStatusCode());
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid purchase item id", 400);
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