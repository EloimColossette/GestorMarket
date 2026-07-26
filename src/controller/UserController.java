package controller;

import dto.ApiResponse;
import dto.UserRequest;
import dto.UserResponse;
import exception.ApiException;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import model.UserModel;
import service.UserService;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

public class UserController implements HttpHandler {

    private static final Logger logger = Logger.getLogger(UserController.class.getName());
    private static final ObjectMapper mapper = new ObjectMapper();
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /users -> agora só devolve o PRÓPRIO perfil, não a lista de todo mundo
    private void handleGet(HttpExchange exchange) throws Exception {
        logger.info("GET /users (own profile)");

        Integer authUserId = (Integer) exchange.getAttribute("authUserId");
        UserModel user = userService.getOwnProfile(authUserId);

        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setCpf(user.getCpf());
        dto.setPhoneNumber(user.getPhoneNumber());

        ApiResponse response = new ApiResponse(true, "Profile fetched successfully", dto);
        sendResponse(exchange, mapper.writeValueAsString(response), 200);
    }

    // POST /users -> continua público (cadastro)
    private void handlePost(HttpExchange exchange) throws Exception {
        logger.info("POST /users");

        String body      = readBody(exchange);
        UserRequest user = mapper.readValue(body, UserRequest.class);
        userService.createUser(user);

        ApiResponse response = new ApiResponse(true, "User created successfully", null);
        sendResponse(exchange, mapper.writeValueAsString(response), 201);
    }

    // PUT /users/{id} -> só se id == usuário logado
    private void handlePut(HttpExchange exchange) throws Exception {
        logger.info("PUT /users/{id}");

        Integer authUserId = (Integer) exchange.getAttribute("authUserId");
        int id             = getIdFromPath(exchange.getRequestURI().getPath());
        UserRequest user   = mapper.readValue(readBody(exchange), UserRequest.class);

        userService.updateUser(id, authUserId, user);

        ApiResponse response = new ApiResponse(true, "User updated successfully", null);
        sendResponse(exchange, mapper.writeValueAsString(response), 200);
    }

    // PATCH /users/{id} -> só se id == usuário logado
    private void handlePatch(HttpExchange exchange) throws Exception {
        logger.info("PATCH /users/{id}");

        Integer authUserId = (Integer) exchange.getAttribute("authUserId");
        int id             = getIdFromPath(exchange.getRequestURI().getPath());
        UserRequest user   = mapper.readValue(readBody(exchange), UserRequest.class);

        userService.partialUpdateUser(id, authUserId, user);

        ApiResponse response = new ApiResponse(true, "User partially updated successfully", null);
        sendResponse(exchange, mapper.writeValueAsString(response), 200);
    }

    // DELETE /users/{id} -> só se id == usuário logado
    private void handleDelete(HttpExchange exchange) throws Exception {
        logger.info("DELETE /users/{id}");

        Integer authUserId = (Integer) exchange.getAttribute("authUserId");
        int id = getIdFromPath(exchange.getRequestURI().getPath());
        if (id <= 0) throw new ApiException("Invalid ID", 400);

        userService.deleteUser(id, authUserId);

        ApiResponse response = new ApiResponse(true, "User deleted successfully", null);
        sendResponse(exchange, mapper.writeValueAsString(response), 200);
    }

    private String readBody(HttpExchange exchange) throws Exception {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes());
        }
    }

    private int getIdFromPath(String path) {
        String[] parts = path.split("/");
        if (parts.length < 3) return -1;
        return Integer.parseInt(parts[2]);
    }

    private void sendResponse(HttpExchange exchange, String body, int status) throws Exception {
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = body.getBytes("UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            String method = exchange.getRequestMethod();
            switch (method) {
                case "OPTIONS" -> { exchange.sendResponseHeaders(204, -1); exchange.close(); }
                case "GET"     -> handleGet(exchange);
                case "POST"    -> handlePost(exchange);
                case "PUT"     -> handlePut(exchange);
                case "PATCH"   -> handlePatch(exchange);
                case "DELETE"  -> handleDelete(exchange);
                default        -> sendResponse(exchange, "Method not supported", 405);
            }
        } catch (ApiException e) {
            logger.warning("Business error: " + e.getMessage());
            ApiResponse response = new ApiResponse(false, e.getMessage(), null);
            try { sendResponse(exchange, mapper.writeValueAsString(response), e.getStatusCode()); }
            catch (Exception ex) { logger.log(Level.SEVERE, "Error serializing JSON", ex); }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Internal error", e);
            ApiResponse response = new ApiResponse(false, "Internal server error", null);
            try { sendResponse(exchange, mapper.writeValueAsString(response), 500); }
            catch (Exception ex) { logger.log(Level.SEVERE, "Error serializing JSON", ex); }
        }
    }
}