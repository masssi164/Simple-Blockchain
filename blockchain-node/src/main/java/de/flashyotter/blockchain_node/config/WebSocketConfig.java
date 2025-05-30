// blockchain-node/src/main/java/de/flashyotter/blockchain_node/config/WebSocketConfig.java
package de.flashyotter.blockchain_node.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import de.flashyotter.blockchain_node.p2p.PeerServer;
import lombok.RequiredArgsConstructor;

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
@EnableWebSocketMessageBroker   // für STOMP/SockJS
@EnableWebSocket                // für raw WebSocketHandler
@RequiredArgsConstructor
public class WebSocketConfig 
     implements WebSocketMessageBrokerConfigurer, WebSocketConfigurer {

    private final PeerServer peerServer;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry reg) {
        reg.addEndpoint("/stomp").setAllowedOrigins("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry reg) {
        reg.enableSimpleBroker("/topic");
        reg.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry reg) {
        reg.addHandler(peerServer, "/ws")
           .setAllowedOrigins("*");
    }
}
