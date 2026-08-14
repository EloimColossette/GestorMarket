package security;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class RateLimitFilter extends Filter {

    private final int maxAttempts;
    private final long windowMillis;

    // IP -> lista de horários (ms) das tentativas recentes
    private final Map<String, List<Long>> attemptsByIp = new ConcurrentHashMap<>();

    public RateLimitFilter(int maxAttempts, long windowMinutes) {
        this.maxAttempts = maxAttempts;
        this.windowMillis = windowMinutes * 60_000L;
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {

        String method = exchange.getRequestMethod();

        // Só limita ações reais (POST). GET (ex: /login.html) e OPTIONS passam direto.
        if (method.equalsIgnoreCase("OPTIONS") || method.equalsIgnoreCase("GET")) {
            chain.doFilter(exchange);
            return;
        }

        String ip = exchange.getRemoteAddress().getAddress().getHostAddress();
        long now = System.currentTimeMillis();

        List<Long> attempts = attemptsByIp.computeIfAbsent(ip, k -> new CopyOnWriteArrayList<>());

        // remove tentativas fora da janela de tempo
        attempts.removeIf(timestamp -> now - timestamp > windowMillis);

        if (attempts.size() >= maxAttempts) {
            sendTooManyRequests(exchange);
            return;
        }

        attempts.add(now);

        // Sem isso, todo IP que já passou por aqui uma vez fica pra sempre no
        // mapa (mesmo com a lista de tentativas vazia) -> vazamento de memória
        // lento em servidor de longa duração.
        if (attemptsByIp.size() > 10_000) {
            attemptsByIp.entrySet().removeIf(e -> e.getValue().isEmpty());
        }

        chain.doFilter(exchange);
    }

    private void sendTooManyRequests(HttpExchange exchange) throws IOException {
        String message = "{\"success\":false,\"message\":\"Muitas tentativas. Tente novamente em alguns minutos.\",\"data\":null}";
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(429, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
        exchange.close();
    }

    @Override
    public String description() {
        return "Rate Limit Filter";
    }
}