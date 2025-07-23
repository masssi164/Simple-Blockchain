package de.flashyotter.blockchain_node.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;

public final class JwtUtils {
    private JwtUtils() {}

    public static boolean verify(String token, NodeProperties props) {
        if (token == null || token.isBlank()) return false;
        if (props.getJwtSecret() == null ||
            props.getJwtSecret().getBytes(StandardCharsets.UTF_8).length < 32) {
            return false;
        }
        try {
            Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(props.getJwtSecret().getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
