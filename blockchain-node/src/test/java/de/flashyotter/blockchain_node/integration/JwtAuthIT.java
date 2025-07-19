package de.flashyotter.blockchain_node.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "node.jwt-secret=integration-secret-0123456789abcdef0123456789ab",
        "grpc.server.port=19091",
        "node.data-path=build/test-data/jwt",
        "node.libp2p-port=0"
    })
class JwtAuthIT {

    @Autowired
    TestRestTemplate rest;

    @Autowired
    de.flashyotter.blockchain_node.config.NodeProperties props;

    private String token() {
        return Jwts.builder()
                .setSubject("test")
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(Keys.hmacShaKeyFor(
                        props.getJwtSecret().getBytes(StandardCharsets.UTF_8)),
                        SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    void unauthorizedWithoutToken() {
        ResponseEntity<String> res = rest.getForEntity("/api/chain/latest", String.class);
        assertThat(res.getStatusCode().value()).isIn(401, 403);
    }

    @Test
    void authorizedWithToken() {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token());
        ResponseEntity<String> res = rest.exchange("/api/chain/latest", HttpMethod.GET,
                new HttpEntity<>(h), String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
