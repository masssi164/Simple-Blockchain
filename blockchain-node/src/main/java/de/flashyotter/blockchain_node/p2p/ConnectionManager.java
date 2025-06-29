package de.flashyotter.blockchain_node.p2p;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.dto.HandshakeDto;
import de.flashyotter.blockchain_node.dto.P2PMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains one persistent WebSocket connection per {@link Peer}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConnectionManager {

    private final ReactorNettyWebSocketClient wsClient;
    private final ObjectMapper mapper;
    private final NodeProperties props;

    /** Holder object returned by {@link #connectAndSink(Peer)}. */
    public record Conn(Sinks.Many<String> outbound,
                       Sinks.Many<P2PMessageDto> inboundSink,
                       Flux<P2PMessageDto> inbound) {}

    private final ConcurrentHashMap<Peer, Conn> connections = new ConcurrentHashMap<>();

    /** Establish or reuse a persistent connection to the given peer. */
    public Conn connectAndSink(Peer peer) {
        return connections.computeIfAbsent(peer, this::createConnection);
    }

    /** Register a server-side WebSocket session for the given peer. */
    public Conn registerServerSession(Peer peer, WebSocketSession session) {
        Conn conn = connections.computeIfAbsent(peer, p -> {
            Sinks.Many<String> out = Sinks.many().multicast().onBackpressureBuffer();
            Sinks.Many<P2PMessageDto> in = Sinks.many().multicast().onBackpressureBuffer();
            return new Conn(out, in, in.asFlux());
        });

        // forward outbound messages to this session
        conn.outbound().asFlux()
                .takeWhile(v -> session.isOpen())
                .subscribe(payload -> {
                    try {
                        session.sendMessage(new TextMessage(payload));
                    } catch (Exception e) {
                        log.warn("❌  send to {} failed: {}", peer, e.getMessage());
                    }
                });

        // subscribe to inbound messages if the session supports it
        try {
            java.lang.reflect.Method m = session.getClass().getMethod("receive");
            @SuppressWarnings("unchecked")
            Flux<WebSocketMessage> flux = (Flux<WebSocketMessage>) m.invoke(session);
            flux.map(WebSocketMessage::getPayloadAsText)
                .map(this::toDto)
                .doOnNext(conn.inboundSink()::tryEmitNext)
                .subscribe();
        } catch (Exception ignored) {
            // fallback: handleTextMessage will forward inbound events
        }

        // remove mapping once the session closes
        try {
            java.lang.reflect.Method cs = session.getClass().getMethod("closeStatus");
            Mono<?> close = (Mono<?>) cs.invoke(session);
            close.doFinally(sig -> connections.remove(peer)).subscribe();
        } catch (Exception e) {
            new Thread(() -> {
                while (session.isOpen()) {
                    try { Thread.sleep(200); } catch (InterruptedException ignored1) {}
                }
                connections.remove(peer);
            }, "ws-close-watch").start();
        }

        return conn;
    }

    /** Emit a message received from a server session into the connection's sink. */
    public void emitInbound(Peer peer, P2PMessageDto dto) {
        Conn c = connections.get(peer);
        if (c != null) c.inboundSink().tryEmitNext(dto);
    }

    /* --------------------------------------------------------------- */
    /* internal helpers                                                 */
    /* --------------------------------------------------------------- */

    private Conn createConnection(Peer peer) {
        Sinks.Many<String> out = Sinks.many().multicast().onBackpressureBuffer();
        Sinks.Many<P2PMessageDto> in = Sinks.many().multicast().onBackpressureBuffer();
        Flux<P2PMessageDto> inbound = maintainConnection(peer, out, in).share();
        return new Conn(out, in, inbound);
    }

    private Flux<P2PMessageDto> maintainConnection(Peer peer, Sinks.Many<String> out,
                                                   Sinks.Many<P2PMessageDto> inSink) {

        Mono<Void> pipeline = Mono.defer(() ->
                wsClient.execute(URI.create(peer.wsUrl()), session -> {
                    String hello = toJson(new HandshakeDto(props.getId(), "0.4.0"));
                    Flux<WebSocketMessage> sendFlux = Flux.concat(
                            Mono.just(hello),
                            out.asFlux()
                    ).map(session::textMessage);

                    Mono<Void> send = session.send(sendFlux);
                    Mono<Void> recv = session.receive()
                            .map(WebSocketMessage::getPayloadAsText)
                            .map(this::toDto)
                            .doOnNext(inSink::tryEmitNext)
                            .then();
                    return send.then(recv);
                })
        )
        .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(5)))
        .doOnError(e -> log.warn("❌  connection {} – {}", peer, e.getMessage()));

        pipeline.subscribe();

        return inSink.asFlux();
    }

    @SneakyThrows
    private String toJson(Object o) { return mapper.writeValueAsString(o); }
    @SneakyThrows
    private P2PMessageDto toDto(String j) { return mapper.readValue(j, P2PMessageDto.class); }
}
