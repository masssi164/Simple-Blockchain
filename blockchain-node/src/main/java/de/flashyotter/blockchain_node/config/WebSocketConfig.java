package de.flashyotter.blockchain_node.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import de.flashyotter.blockchain_node.websocket.BlockWebSocketHandler;
import de.flashyotter.blockchain_node.websocket.PeerWebSocketHandler;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    private final BlockWebSocketHandler blockHandler;
    private final PeerWebSocketHandler  peerHandler;

    public void registerWebSocketHandlers(WebSocketHandlerRegistry r) {
        r.addHandler(blockHandler, "/ws/blocks").setAllowedOrigins("*");
        r.addHandler(peerHandler , "/ws/peers" ).setAllowedOrigins("*");
    }
}
