package security;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class AuthFilter extends Filter {

    @Override
    public String description() {
        return "JWT Authentication Filter";
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {

        String path   = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if (isPublicRoute(path, method)) {
            chain.doFilter(exchange);
            return;
        }

        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendResponse(exchange, "Missing or invalid token", 401);
            return;
        }

        String token = authHeader.replace("Bearer ", "").trim();

        try {
            String email = JwtUtil.validateToken(token);

            if (email == null) {
                sendResponse(exchange, "Invalid token", 401);
                return;
            }

            chain.doFilter(exchange);

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, "Invalid token", 401);
        }
    }

    private boolean isPublicRoute(String path, String method) {
        // /login is public for everyone
        // POST /users is public so anyone can register
        return path.equals("/login")
                || (path.equals("/users") && method.equals("POST"))
                || path.startsWith("/password"); // forgot-password and reset-password
    }

    private void sendResponse(HttpExchange exchange, String message, int status) throws IOException {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
