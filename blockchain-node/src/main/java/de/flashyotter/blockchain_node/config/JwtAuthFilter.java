package de.flashyotter.blockchain_node.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * Simple authentication filter validating Bearer JWT tokens using the
 * secret configured in {@link NodeProperties}.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final NodeProperties props;

    public JwtAuthFilter(NodeProperties props) {
        this.props = props;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Jwts.parserBuilder()
                        .setSigningKey(Keys.hmacShaKeyFor(
                                props.getJwtSecret().getBytes(StandardCharsets.UTF_8)))
                        .build()
                        .parseClaimsJws(token);
                var auth = new UsernamePasswordAuthenticationToken(
                        "user", null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ignored) {
                // Invalid token -> leave context unauthenticated
            }
        }
        filterChain.doFilter(request, response);
    }
}

