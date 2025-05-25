// PeerClient.java
package de.flashyotter.blockchain_node.p2p;

import java.net.URI;
import java.time.Duration;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.flashyotter.blockchain_node.dto.P2PMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
@RequiredArgsConstructor @Slf4j
public class PeerClient {

    private final ObjectMapper                mapper;
    private final ReactorNettyWebSocketClient wsClient;

    @SneakyThrows
    public void send(Peer peer, P2PMessageDto msg) {

        String json = mapper.writeValueAsString(msg);

        Mono.defer(() ->
                wsClient.execute(URI.create(peer.wsUrl()),
                                 s -> s.send(Mono.just(s.textMessage(json))).then()))
            .retryWhen(Retry.backoff(5, Duration.ofSeconds(2)))
            .doOnError(e -> log.warn("❌  send to {} failed: {}", peer, e.getMessage()))
            .subscribe();               // fire-and-forget
    }
}
