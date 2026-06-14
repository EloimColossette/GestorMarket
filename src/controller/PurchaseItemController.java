package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import dto.CreatePurchaseItemDTO;
import model.PurchaseItemModel;
import service.PurchaseItemService;
import util.JsonUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PurchaseItemController
        implements HttpHandler {

    private final PurchaseItemService purchaseItemService;

    public PurchaseItemController(
            PurchaseItemService purchaseItemService
    ) {
        this.purchaseItemService =
                purchaseItemService;
    }

    @Override
    public void handle(
            HttpExchange exchange
    ) throws IOException {

        String method =
                exchange.getRequestMethod();

        switch (method) {

            case "GET" -> getAllPurchaseItems(exchange);

            case "POST" -> createPurchaseItem(exchange);

            case "PUT" -> updatePurchaseItem(exchange);

            case "DELETE" -> deletePurchaseItem(exchange);

            default -> {

                exchange.sendResponseHeaders(
                        405,
                        -1
                );

                exchange.close();
            }
        }
    }

    private void getAllPurchaseItems(
            HttpExchange exchange
    ) throws IOException {

        List<PurchaseItemModel> purchaseItems =
                purchaseItemService.findAllPurchaseItems();

        String response =
                JsonUtil.getGson()
                        .toJson(
                                purchaseItems
                        );

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

    private void createPurchaseItem(
            HttpExchange exchange
    ) throws IOException {

        try {

            String body =
                    new String(
                            exchange.getRequestBody()
                                    .readAllBytes(),
                            StandardCharsets.UTF_8
                    );

            CreatePurchaseItemDTO dto =
                    JsonUtil.getGson()
                            .fromJson(
                                    body,
                                    CreatePurchaseItemDTO.class
                            );

            purchaseItemService.savePurchaseItem(
                    dto
            );

            String response =
                    "Purchase item created successfully";

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

    private void updatePurchaseItem(
            HttpExchange exchange
    ) throws IOException {

        String response =
                "Update purchase item endpoint not implemented yet";

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

    private void deletePurchaseItem(
            HttpExchange exchange
    ) throws IOException {

        String response =
                "Delete purchase item endpoint not implemented yet";

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