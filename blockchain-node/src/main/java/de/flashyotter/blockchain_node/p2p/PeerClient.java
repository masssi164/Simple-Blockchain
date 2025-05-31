package de.flashyotter.blockchain_node.p2p;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.flashyotter.blockchain_node.dto.P2PMessageDto;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component @RequiredArgsConstructor @Slf4j
public class PeerClient {

    private final ObjectMapper                mapper;
    private final ReactorNettyWebSocketClient wsClient;

    private final ConcurrentHashMap<Peer, CircuitBreaker> breakers =
            new ConcurrentHashMap<>();

    private CircuitBreaker cb(Peer p) {
        return breakers.computeIfAbsent(p,
                __ -> CircuitBreaker.ofDefaults("peer-" + p.toString()));
    }

    @SneakyThrows
    public void send(Peer peer, P2PMessageDto msg) {

        String json = mapper.writeValueAsString(msg);

        Mono<Void> pipeline = Mono.defer(() ->
                wsClient.execute(URI.create(peer.wsUrl()),
                                 s -> s.send(Mono.just(s.textMessage(json))).then()))
            .transformDeferred(CircuitBreakerOperator.of(cb(peer)))
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
            .doOnError(e -> log.warn("❌  send to {} failed: {}", peer, e.toString()));

        pipeline.subscribe();         // fire-and-forget
    }
}
