package security;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class AuthInterceptor extends Filter {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {

        String path = exchange.getRequestURI().getPath();

        if (isPublicRoute(path)) {
            chain.doFilter(exchange);
            return;
        }

        try {
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendError(exchange, "Token ausente ou inválido");
                return;
            }

            String token = authHeader.replace("Bearer ", "").trim();

            String user = JwtUtil.validateToken(token);

            if (user == null) {
                sendError(exchange, "Token inválido");
                return;
            }

            exchange.setAttribute("user", user);

            chain.doFilter(exchange);

        } catch (Exception e) {
            sendError(exchange, "Acesso negado: token inválido ou expirado");
        }
    }

    private boolean isPublicRoute(String path) {
        return path.equals("/login");
    }

    private void sendError(HttpExchange exchange, String message) throws IOException {

        ApiResponse response = new ApiResponse(false, message, null);

        String json = mapper.writeValueAsString(response);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(401, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    @Override
    public String description() {
        return "JWT Authentication Middleware";
    }
}