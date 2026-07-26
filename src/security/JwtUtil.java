package security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import envloader.EnvLoader;

import java.util.Date;

public class JwtUtil {

    private static final String SECRET = loadSecret();

    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET);

    private static String loadSecret() {
        String secret = EnvLoader.get("JWT_SECRET");
        if (secret == null || secret.isBlank()) {
            throw new RuntimeException(
                    "JWT_SECRET não definido no .env! Gere um valor forte antes de subir o servidor."
            );
        }
        if (secret.length() < 32) {
            throw new RuntimeException("JWT_SECRET muito curto (mínimo 32 caracteres).");
        }
        return secret;
    }

    private static final long SESSION_DURATION_MS = 1000L * 60 * 60 * 2;

    public static String generateToken(int userId, String email) {
        return JWT.create()
                .withSubject(email)
                .withClaim("userId", userId)
                .withIssuedAt(new Date())
                .withIssuer("SistemaCompras")
                .withExpiresAt(new Date(System.currentTimeMillis() + SESSION_DURATION_MS))
                .sign(ALGORITHM);
    }

    public static DecodedJWT validateToken(String token) {
        try {
            return JWT.require(ALGORITHM)
                    .withIssuer("SistemaCompras")
                    .build()
                    .verify(token);
        } catch (JWTVerificationException e) {
            return null;
        }
    }
}