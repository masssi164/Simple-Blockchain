package de.flashyotter.blockchain_node.p2p;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.flashyotter.blockchain_node.dto.P2PMessageDto;
import de.flashyotter.blockchain_node.dto.HandshakeDto;
import de.flashyotter.blockchain_node.config.NodeProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component @RequiredArgsConstructor @Slf4j
public class PeerClient {

    private final ObjectMapper                mapper;
    private final ReactorNettyWebSocketClient wsClient;
    private final NodeProperties              props;

    private final ConcurrentHashMap<Peer, CircuitBreaker> breakers =
            new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Peer, reactor.core.publisher.Sinks.Many<String>> channels =
            new ConcurrentHashMap<>();

    private CircuitBreaker cb(Peer p) {
        return breakers.computeIfAbsent(p,
                __ -> CircuitBreaker.ofDefaults("peer-" + p.toString()));
    }

    /** Establish a persistent connection to the given peer. */
    public void connect(Peer peer) {
        ensureChannel(peer);             // lazy creation starts connection
    }

    @SneakyThrows
    public void send(Peer peer, P2PMessageDto msg) {
        var sink = ensureChannel(peer);
        String json = mapper.writeValueAsString(msg);
        sink.tryEmitNext(json);
    }

    /* ----------------------------------------------------------- */
    /* internal helpers                                             */
    /* ----------------------------------------------------------- */

    private reactor.core.publisher.Sinks.Many<String> ensureChannel(Peer peer) {
        return channels.computeIfAbsent(peer, p -> {
            var sink = reactor.core.publisher.Sinks.many().unicast().<String>onBackpressureBuffer();

            Flux<Void> pipeline = Flux.defer(() ->
                    wsClient.execute(URI.create(p.wsUrl()), session -> {
                        HandshakeDto hello = new HandshakeDto(props.getId(), "0.4.0");
                        Mono<Void> hs = session.send(Mono.just(session.textMessage(toJson(hello))));
                        Mono<Void> out = session.send(sink.asFlux().map(session::textMessage));
                        return hs.then(out.then());
                    }))
                .transformDeferred(CircuitBreakerOperator.of(cb(p)))
                .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(5)))
                .doOnError(e -> log.warn("❌  conn {} failed: {}", p, e.getMessage()))
                .doFinally(sig -> channels.remove(p));

            pipeline.subscribe();
            return sink;
        });
    }

    @SneakyThrows
    private String toJson(Object o) { return mapper.writeValueAsString(o); }
}
