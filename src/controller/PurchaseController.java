package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import dto.CreatePurchaseDTO;
import model.PurchaseModel;
import service.PurchaseService;
import util.JsonUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PurchaseController
        implements HttpHandler {

    private final PurchaseService purchaseService;

    public PurchaseController(
            PurchaseService purchaseService
    ) {
        this.purchaseService = purchaseService;
    }

    @Override
    public void handle(
            HttpExchange exchange
    ) throws IOException {

        String method =
                exchange.getRequestMethod();

        switch (method) {

            case "GET" -> getAllPurchases(exchange);

            case "POST" -> createPurchase(exchange);

            case "PUT" -> updatePurchase(exchange);

            case "DELETE" -> deletePurchase(exchange);

            default -> {

                exchange.sendResponseHeaders(
                        405,
                        -1
                );

                exchange.close();
            }
        }
    }

    private void getAllPurchases(
            HttpExchange exchange
    ) throws IOException {

        List<PurchaseModel> purchases =
                purchaseService.findAllPurchases();

        String response =
                JsonUtil.getGson()
                        .toJson(purchases);

        exchange.getResponseHeaders().add(
                "Content-Type",
                "application/json"
        );

        exchange.sendResponseHeaders(
                200,
                response.getBytes(
                        StandardCharsets.UTF_8
                ).length
        );

        exchange.getResponseBody().write(
                response.getBytes(
                        StandardCharsets.UTF_8
                )
        );

        exchange.close();
    }

    private void createPurchase(
            HttpExchange exchange
    ) throws IOException {

        try {

            String body =
                    new String(
                            exchange.getRequestBody()
                                    .readAllBytes(),
                            StandardCharsets.UTF_8
                    );

            CreatePurchaseDTO dto =
                    JsonUtil.getGson()
                            .fromJson(
                                    body,
                                    CreatePurchaseDTO.class
                            );

            purchaseService.savePurchase(
                    dto
            );

            String response =
                    "Purchase created successfully";

            exchange.sendResponseHeaders(
                    201,
                    response.getBytes(
                            StandardCharsets.UTF_8
                    ).length
            );

            exchange.getResponseBody().write(
                    response.getBytes(
                            StandardCharsets.UTF_8
                    )
            );

        } catch (Exception e) {

            String response =
                    e.getMessage();

            exchange.sendResponseHeaders(
                    400,
                    response.getBytes(
                            StandardCharsets.UTF_8
                    ).length
            );

            exchange.getResponseBody().write(
                    response.getBytes(
                            StandardCharsets.UTF_8
                    )
            );
        }

        exchange.close();
    }

    private void updatePurchase(
            HttpExchange exchange
    ) throws IOException {

        String response =
                "Update purchase endpoint not implemented yet";

        exchange.sendResponseHeaders(
                501,
                response.getBytes(
                        StandardCharsets.UTF_8
                ).length
        );

        exchange.getResponseBody().write(
                response.getBytes(
                        StandardCharsets.UTF_8
                )
        );

        exchange.close();
    }

    private void deletePurchase(
            HttpExchange exchange
    ) throws IOException {

        String response =
                "Delete purchase endpoint not implemented yet";

        exchange.sendResponseHeaders(
                501,
                response.getBytes(
                        StandardCharsets.UTF_8
                ).length
        );

        exchange.getResponseBody().write(
                response.getBytes(
                        StandardCharsets.UTF_8
                )
        );

        exchange.close();
    }
}