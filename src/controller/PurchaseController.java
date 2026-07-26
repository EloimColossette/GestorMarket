package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.CreatePurchaseDTO;
import dto.PurchaseDetailDTO;
import dto.SupermarketSummaryDTO;
import exception.ApiException;
import model.PurchaseModel;
import service.PurchaseService;
import util.JsonUtil;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            case "GET" -> {
                String path = exchange.getRequestURI().getPath();
                if (path.equals("/purchases/summary")) {
                    getPurchaseSummary(exchange);
                } else if (path.equals("/purchases/detail")) {
                    getPurchaseDetail(exchange);
                } else {
                    getAllPurchases(exchange);
                }
            }
            case "POST"    -> createPurchase(exchange);
            case "PUT"     -> updatePurchase(exchange);
            case "DELETE"  -> deletePurchase(exchange);
            default -> { exchange.sendResponseHeaders(405, -1); exchange.close(); }
        }
    }

    private void getAllPurchases(HttpExchange exchange) throws IOException {

        Integer userId = (Integer) exchange.getAttribute("authUserId");

        Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());

        String supermarketName = params.get("supermarket");
        LocalDate startDate = parseDateParam(params.get("startDate"));
        LocalDate endDate   = parseDateParam(params.get("endDate"));

        Object result;

        if (supermarketName != null || startDate != null || endDate != null) {
            result = purchaseService.getPurchaseReport(userId, supermarketName, startDate, endDate);
        } else {
            result = purchaseService.findAllPurchases(userId);
        }

        String response = JsonUtil.getGson().toJson(result);

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
        exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
        exchange.close();
    }

    private void getPurchaseSummary(HttpExchange exchange) throws IOException {

        Integer userId = (Integer) exchange.getAttribute("authUserId");
        Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());

        Integer supermarketId = parseIntParam(params.get("supermarketId"));
        LocalDate date = parseDateParam(params.get("date"));

        List<SupermarketSummaryDTO> result = purchaseService.getPurchaseSummary(userId, supermarketId, date);

        String response = JsonUtil.getGson().toJson(result);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
        exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
        exchange.close();
    }

    private void getPurchaseDetail(HttpExchange exchange) throws IOException {
        try {
            Integer userId = (Integer) exchange.getAttribute("authUserId");
            Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());

            Integer supermarketId = parseIntParam(params.get("supermarketId"));
            LocalDate date = parseDateParam(params.get("date"));

            List<PurchaseDetailDTO> result = purchaseService.getPurchaseDetail(userId, supermarketId, date);

            String response = JsonUtil.getGson().toJson(result);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));

        } catch (ApiException e) {
            sendError(exchange, e.getMessage(), e.getStatusCode());
        }
        exchange.close();
    }

    private Integer parseIntParam(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ApiException("ID de supermercado inválido", 400);
        }
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isBlank()) return params;

        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            params.put(key, value);
        }
        return params;
    }

    private LocalDate parseDateParam(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDate.parse(value); // espera formato yyyy-MM-dd
        } catch (Exception e) {
            throw new ApiException("Formato de data inválido (use AAAA-MM-DD)", 400);
        }
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