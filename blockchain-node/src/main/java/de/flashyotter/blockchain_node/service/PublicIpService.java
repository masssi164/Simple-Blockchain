package de.flashyotter.blockchain_node.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * Looks up the node's outward-facing IP address via a simple HTTP request.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PublicIpService {

    private final WebClient webClient;

    /**
     * @return detected public IP address or {@code null} if the lookup failed
     */
    public String fetchPublicIp() {
        try {
            return webClient.get()
                             .uri("https://api.ipify.org")
                             .retrieve()
                             .bodyToMono(String.class)
                             .block(Duration.ofSeconds(5));
        } catch (Exception e) {
            log.warn("Public IP lookup failed: {}", e.toString());
            return null;
        }
    }
}
