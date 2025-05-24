package de.flashyotter.blockchain_node.p2p;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.flashyotter.blockchain_node.dto.P2PMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * Sends one JSON-encoded {@link P2PMessageDto} to a remote peer.
 * Uses the shared ReactorNettyWebSocketClient bean for connection pooling.
 */
@Component
@RequiredArgsConstructor
public class PeerClient {

    private final ObjectMapper                mapper;
    private final ReactorNettyWebSocketClient wsClient;

    @SneakyThrows
    public void send(Peer peer, P2PMessageDto msg) {
        String json = mapper.writeValueAsString(msg);

        wsClient.execute(
                URI.create(peer.wsUrl()),
                session -> session
                        .send(Mono.just(session.textMessage(json)))
                        .then())
                .block(); // fire-and-wait for completion
    }
}
