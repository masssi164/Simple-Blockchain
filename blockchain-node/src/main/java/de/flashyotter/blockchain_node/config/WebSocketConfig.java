// blockchain-node/src/main/java/de/flashyotter/blockchain_node/config/WebSocketConfig.java
package de.flashyotter.blockchain_node.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import lombok.RequiredArgsConstructor;

/**
 * Servlet-based STOMP/WebSocket setup.
 *
 *  • in-memory broker for `/topic/*`
 *
 * NOTE  
 * ----  
 * Do **not** annotate with @EnableWebFlux – bringing both the
 * WebFlux and WebMVC auto-configs into the same context creates two
 * `requestMappingHandlerMapping` beans which fails the bootstrap.
 */
@Configuration
@EnableWebSocketMessageBroker   // für STOMP/SockJS
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry reg) {
        reg.addEndpoint("/stomp").setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry reg) {
        reg.enableSimpleBroker("/topic");
        reg.setApplicationDestinationPrefixes("/app");
    }

    @Bean
    public WebSocketHandlerAdapter wsAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
