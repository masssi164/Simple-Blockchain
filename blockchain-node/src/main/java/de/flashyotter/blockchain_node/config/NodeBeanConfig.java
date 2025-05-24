package de.flashyotter.blockchain_node.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;

/**
 * Infrastructure beans shared across the node.
 * A single ObjectMapper and WebSocket client are reused everywhere
 * to avoid the overhead of repeated allocations.
 */
@Configuration
public class NodeBeanConfig {

    @Bean
    public ReactorNettyWebSocketClient webSocketClient() {
        // default ctor → creates its own Reactor Netty HttpClient
        return new ReactorNettyWebSocketClient();
    }

    @Bean
    public WebClient webClient(ObjectMapper mapper) {
        return WebClient.builder()
                .codecs(cfg -> {
                    cfg.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(mapper));
                    cfg.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(mapper));
                })
                .build();
    }
}
