package security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;

public class JwtUtil {

    // 🔥 ideal vir de variável de ambiente
    private static final String SECRET = System.getenv().getOrDefault(
            "JWT_SECRET",
            "DEFAULT_DEV_SECRET_CHANGE_ME"
    );

    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET);

    public static String generateToken(String email) {

        return JWT.create()
                .withSubject(email)
                .withIssuedAt(new Date())
                .withIssuer("SistemaCompras")
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .sign(ALGORITHM);
    }

    public static String validateToken(String token) {

        try {
            DecodedJWT jwt = JWT.require(ALGORITHM)
                    .withIssuer("SistemaCompras")
                    .build()
                    .verify(token);

            return jwt.getSubject();

        } catch (JWTVerificationException e) {
            // opcional: log aqui
            return null;
        }
    }
}