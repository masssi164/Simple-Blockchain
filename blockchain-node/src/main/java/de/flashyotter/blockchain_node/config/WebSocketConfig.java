// blockchain-node/src/main/java/de/flashyotter/blockchain_node/config/WebSocketConfig.java
package de.flashyotter.blockchain_node.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

/**
 * Servlet-based STOMP/WebSocket setup.
 *
 *  • `/ws` endpoint for P2P peers  
 *  • in-memory broker for `/topic/*`
 *
 * NOTE  
 * ----  
 * Do **not** annotate with @EnableWebFlux – bringing both the
 * WebFlux and WebMVC auto-configs into the same context creates two
 * `requestMappingHandlerMapping` beans which fails the bootstrap.
 */
@Configuration
@EnableWebSocketMessageBroker          //  ← keep
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }
}
