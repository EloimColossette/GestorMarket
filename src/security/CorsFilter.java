package security;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class CorsFilter extends Filter {

    private static final String ALLOWED_ORIGIN = "*";

    private static final String ALLOWED_METHODS =
            "GET, POST, PUT, DELETE, OPTIONS";

    private static final String ALLOWED_HEADERS =
            "Content-Type, Authorization";

    @Override
    public void doFilter(
            HttpExchange exchange,
            Chain chain
    ) throws IOException {

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin",
                ALLOWED_ORIGIN
        );

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods",
                ALLOWED_METHODS
        );

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Headers",
                ALLOWED_HEADERS
        );

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Credentials",
                "true"
        );

        if (
                exchange.getRequestMethod()
                        .equalsIgnoreCase("OPTIONS")
        ) {

            exchange.sendResponseHeaders(
                    204,
                    -1
            );

            return;
        }

        chain.doFilter(exchange);
    }

    @Override
    public String description() {

        return "CORS Filter";
    }
}