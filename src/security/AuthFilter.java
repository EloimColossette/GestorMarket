package security;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class AuthFilter extends Filter {

    @Override
    public String description() {
        return "Filtro de autenticação JWT";
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if (isPublicRoute(path, method)) {
            chain.doFilter(exchange);
            return;
        }

        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendResponse(exchange, "Token ausente ou inválido", 401);
            return;
        }

        String token = authHeader.replace("Bearer ", "").trim();

        try {
            String email = JwtUtil.validateToken(token);

            if (email == null) {
                sendResponse(exchange, "Token inválido", 401);
                return;
            }

            chain.doFilter(exchange);

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, "Token inválido", 401);
        }
    }

    private boolean isPublicRoute(String path, String method) {
        return path.equals("/login")
                || (path.equals("/usuarios") && method.equals("POST"));
    }

    private void sendResponse(HttpExchange exchange, String message, int status) throws IOException {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(status, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}