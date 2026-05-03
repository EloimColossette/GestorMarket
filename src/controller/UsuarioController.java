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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

public class UsuarioController implements HttpHandler {

    private static final Logger logger = Logger.getLogger(UsuarioController.class.getName());
    private static final ObjectMapper mapper = new ObjectMapper();

    private final UserService userService;

    // 🔥 Injeção via construtor
    public UsuarioController(UserService userService) {
        this.userService = userService;
    }

    // GET
    private void handleGet(HttpExchange exchange) throws Exception {
        logger.info("Recebendo Requisicao GET /Usuario");

        List<UserModel> users = userService.listarUsuarios();

        List<UserResponse> responseDtos = users.stream().map(user -> {
            UserResponse dto = new UserResponse();
            dto.setId(user.getId());
            dto.setEmail(user.getEmail());
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            dto.setCpf(user.getCpf());
            dto.setPhoneNumber(user.getPhoneNumber());
            return dto;
        }).toList();

        ApiResponse response = new ApiResponse(
                true,
                "Usuario listado com Sucesso",
                responseDtos
        );

        sendResponse(exchange, mapper.writeValueAsString(response), 200);
    }

    // POST
    private void handlePost(HttpExchange exchange) throws Exception {
        logger.info("Criando requisição POST /Usuario");

        String body = lerBody(exchange);

        UserRequest user = mapper.readValue(body, UserRequest.class);

        userService.criarUsuario(user);

        ApiResponse response = new ApiResponse(
                true,
                "Usuario criado com Sucesso",
                null
        );

        sendResponse(exchange, mapper.writeValueAsString(response), 201);
    }

    // PUT
    private void handlePut(HttpExchange exchange) throws Exception {
        logger.info("Recebendo a requisição PUT /Usuario");

        int id = getIdFrompath(exchange.getRequestURI().getPath());

        UserRequest user = mapper.readValue(lerBody(exchange), UserRequest.class);

        userService.atualizarUsuario(id, user);

        ApiResponse response = new ApiResponse(
                true,
                "Usuario Atualizado com sucesso",
                null
        );

        sendResponse(exchange, mapper.writeValueAsString(response), 200);
    }

    // PATCH
    private void handlePatch(HttpExchange exchange) throws Exception {
        logger.info("Recebendo requisição PATCH /Usuario");

        int id = getIdFrompath(exchange.getRequestURI().getPath());

        UserRequest user = mapper.readValue(lerBody(exchange), UserRequest.class);

        userService.atualizarParcialmenteUsuario(id, user);

        ApiResponse response = new ApiResponse(
                true,
                "Usuario atualizado parcialmente com sucesso",
                null
        );

        sendResponse(exchange, mapper.writeValueAsString(response), 200);
    }

    // DELETE
    private void handleDelete(HttpExchange exchange) throws Exception {
        logger.info("Recebendo requisição DELETE /usuarios/{id}");

        int id = getIdFrompath(exchange.getRequestURI().getPath());

        if (id <= 0) {
            throw new ApiException("ID inválido", 400);
        }

        userService.excluirUsuario(id);

        ApiResponse response = new ApiResponse(
                true,
                "Usuario deletado com sucesso",
                null
        );

        sendResponse(exchange, mapper.writeValueAsString(response), 200);
    }

    // UTIL
    private String lerBody(HttpExchange exchange) throws Exception {
        InputStream is = exchange.getRequestBody();
        String body = new String(is.readAllBytes());
        is.close();
        return body;
    }

    private int getIdFrompath(String path) {
        String[] partes = path.split("/");

        if (partes.length < 3) {
            return -1;
        }

        return Integer.parseInt(partes[2]);
    }

    private void sendResponse(HttpExchange exchange, String resposta, int status) throws Exception {
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");

        byte[] responseBytes = resposta.getBytes("UTF-8");
        exchange.sendResponseHeaders(status, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    @Override
    public void handle(HttpExchange exchange) {
        System.out.println("PATH: " + exchange.getRequestURI().getPath());

        try {
            String metodo = exchange.getRequestMethod();

            switch (metodo) {
                case "GET":
                    handleGet(exchange);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "PUT":
                    handlePut(exchange);
                    break;
                case "PATCH":
                    handlePatch(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange);
                    break;
                default:
                    sendResponse(exchange, "Metodo não suportado", 404);
            }

        } catch (ApiException e) {

            logger.warning("Erro de Negocio: " + e.getMessage());

            ApiResponse response = new ApiResponse(false, e.getMessage(), null);

            try {
                sendResponse(exchange, mapper.writeValueAsString(response), e.getStatusCode());
            } catch (Exception jsonError) {
                logger.log(Level.SEVERE, "Erro ao converter JSON", jsonError);
            }

        } catch (Exception e) {

            logger.log(Level.SEVERE, "Erro interno", e);

            ApiResponse response = new ApiResponse(false, "Erro interno do servidor", null);

            try {
                sendResponse(exchange, mapper.writeValueAsString(response), 500);
            } catch (Exception jsonError) {
                logger.log(Level.SEVERE, "Erro ao converter JSON", jsonError);
            }
        }
    }
}