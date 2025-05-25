package de.flashyotter.blockchain_node.p2p;

import java.net.URI;
import java.time.Duration;
import reactor.util.retry.Retry;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.flashyotter.blockchain_node.dto.P2PMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Sends one JSON-encoded {@link P2PMessageDto} to a remote peer.
 * Uses the shared ReactorNettyWebSocketClient bean for connection pooling.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PeerClient {

    private final ObjectMapper                mapper;
    private final ReactorNettyWebSocketClient wsClient;

    @SneakyThrows
    public void send(Peer peer, P2PMessageDto msg) throws JsonProcessingException {

        String json = mapper.writeValueAsString(msg);

        wsClient.execute(URI.create(peer.wsUrl()),
                        sess -> sess.send(Mono.just(sess.textMessage(json))).then())
                .retryWhen(                         // <- statt retryBackoff
                    Retry.backoff(5, Duration.ofSeconds(2))
                        .maxBackoff(Duration.ofSeconds(32)))
                .doOnError(e -> log.warn("❌  send to {} failed: {}", peer, e.toString()))
                .subscribe();                       // fire & forget
    }
    
}
