package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.CreateSupermarketDTO;
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

    // GET ALL
    private void getAllSupermarkets(HttpExchange exchange) throws IOException {

        List<SupermarketModel> supermarkets =
                supermarketService.findAllSupermarkets();

        String response =
                JsonUtil.getGson().toJson(supermarkets);

        exchange.getResponseHeaders().add(
                "Content-Type",
                "application/json"
        );

        exchange.sendResponseHeaders(
                200,
                response.getBytes(StandardCharsets.UTF_8).length
        );

        exchange.getResponseBody().write(
                response.getBytes(StandardCharsets.UTF_8)
        );

        exchange.close();
    }

    // POST
    private void createSupermarket(HttpExchange exchange) throws IOException {

        try {

            String body = new String(
                    exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            CreateSupermarketDTO dto =
                    JsonUtil.getGson().fromJson(body, CreateSupermarketDTO.class);

            supermarketService.saveSupermarket(dto);

            String response = "Supermarket created successfully";

            exchange.sendResponseHeaders(
                    201,
                    response.getBytes(StandardCharsets.UTF_8).length
            );

            exchange.getResponseBody().write(
                    response.getBytes(StandardCharsets.UTF_8)
            );

        } catch (Exception e) {

            String response = e.getMessage();

            exchange.sendResponseHeaders(
                    400,
                    response.getBytes(StandardCharsets.UTF_8).length
            );

            exchange.getResponseBody().write(
                    response.getBytes(StandardCharsets.UTF_8)
            );
        }

        exchange.close();
    }

    // PUT (ainda simples, igual seu padrão atual)
    private void updateSupermarket(HttpExchange exchange) throws IOException {

        String response = "Update supermarket endpoint not implemented yet";

        exchange.sendResponseHeaders(
                501,
                response.getBytes(StandardCharsets.UTF_8).length
        );

        exchange.getResponseBody().write(
                response.getBytes(StandardCharsets.UTF_8)
        );

        exchange.close();
    }

    // DELETE
    private void deleteSupermarket(HttpExchange exchange) throws IOException {

        String response = "Delete supermarket endpoint not implemented yet";

        exchange.sendResponseHeaders(
                501,
                response.getBytes(StandardCharsets.UTF_8).length
        );

        exchange.getResponseBody().write(
                response.getBytes(StandardCharsets.UTF_8)
        );

        exchange.close();
    }
}