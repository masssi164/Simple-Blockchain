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
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;
import reactor.core.Disposable;
import reactor.core.scheduler.Schedulers;
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
        Sinks.Many<String> out = Sinks.many().multicast().onBackpressureBuffer(100);
        Sinks.Many<P2PMessageDto> inSink = Sinks.many().multicast().onBackpressureBuffer(100);
        Conn conn = new Conn(out, inSink, inSink.asFlux());

        java.util.concurrent.atomic.AtomicReference<Peer> actual = new java.util.concurrent.atomic.AtomicReference<>();
        java.util.Queue<P2PMessageDto> preHandshake = new java.util.concurrent.ConcurrentLinkedQueue<>();

        Disposable timeout = Schedulers.single().schedule(() -> {
            if (actual.get() == null) {
                preHandshake.clear();
                session.close().subscribe();
            }
        }, 5, java.util.concurrent.TimeUnit.SECONDS);

        // forward outbound messages to this session
        session.send(out.asFlux().map(session::textMessage))
               .doOnError(e -> log.warn("❌  send to {} failed: {}", peer, e.getMessage()))
               .subscribe();

        // forward inbound messages from the session, buffering until handshake
        session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .map(this::toDto)
                .doOnNext(dto -> {
                    Peer a = actual.get();
                    if (a == null) {
                        preHandshake.add(dto);
                        if (dto instanceof HandshakeDto hs) {
                            Peer real = new Peer(peer.getHost(), hs.listenPort());
                            if (connections.putIfAbsent(real, conn) == null) {
                                actual.set(real);
                            } else {
                                actual.set(real);
                            }
                            preHandshake.forEach(msg -> emitChecked(inSink, msg, peer, session));
                            preHandshake.clear();
                            timeout.dispose();
                        }
                    } else {
                        emitChecked(inSink, dto, peer, session);
                    }
                })
                .subscribe();

        // remove mapping once the session closes
        session.closeStatus()
                .doFinally(sig -> {
                    Peer a = actual.get();
                    if (a != null) connections.remove(a);
                })
                .subscribe();

        return conn;
    }

    /** Emit a message received from a server session into the connection's sink. */
    public void emitInbound(Peer peer, P2PMessageDto dto) {
        Conn c = connections.get(peer);
        if (c != null) emitChecked(c.inboundSink(), dto, peer, null);
    }

    /* --------------------------------------------------------------- */
    /* internal helpers                                                 */
    /* --------------------------------------------------------------- */

    private Conn createConnection(Peer peer) {
        Sinks.Many<String> out = Sinks.many().multicast().onBackpressureBuffer(100);
        Sinks.Many<P2PMessageDto> in = Sinks.many().multicast().onBackpressureBuffer(100);
        Flux<P2PMessageDto> inbound = maintainConnection(peer, out, in).share();
        return new Conn(out, in, inbound);
    }

    private Flux<P2PMessageDto> maintainConnection(Peer peer, Sinks.Many<String> out,
                                                   Sinks.Many<P2PMessageDto> inSink) {

        Mono<Void> pipeline = Mono.defer(() ->
                wsClient.execute(URI.create(peer.wsUrl()), session -> {
                    java.util.Queue<P2PMessageDto> preHandshake = new java.util.concurrent.ConcurrentLinkedQueue<>();
                    java.util.concurrent.atomic.AtomicBoolean done = new java.util.concurrent.atomic.AtomicBoolean(false);

                    Disposable timeout = Schedulers.single().schedule(() -> {
                        if (!done.get()) {
                            preHandshake.clear();
                            session.close().subscribe();
                        }
                    }, 5, java.util.concurrent.TimeUnit.SECONDS);

                    String hello = toJson(new HandshakeDto(props.getId(), "0.4.0", props.getPort()));
                    Flux<WebSocketMessage> sendFlux = Flux.concat(
                            Mono.just(hello),
                            out.asFlux()
                    ).map(session::textMessage);

                    Mono<Void> send = session.send(sendFlux);
                    Mono<Void> recv = session.receive()
                            .map(WebSocketMessage::getPayloadAsText)
                            .map(this::toDto)
                            .doOnNext(dto -> {
                                if (!done.get()) {
                                    preHandshake.add(dto);
                                    if (dto instanceof HandshakeDto) {
                                        done.set(true);
                                        preHandshake.forEach(msg -> emitChecked(inSink, msg, peer, session));
                                        preHandshake.clear();
                                        timeout.dispose();
                                    }
                                } else {
                                    emitChecked(inSink, dto, peer, session);
                                }
                            })
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

    /** Emit to a sink and close the session on overflow. */
    private <T> void emitChecked(Sinks.Many<T> sink, T msg, Peer peer, WebSocketSession session) {
        EmitResult result = sink.tryEmitNext(msg);
        if (result == EmitResult.FAIL_OVERFLOW) {
            log.warn("❌  buffer overflow for {}", peer);
            if (session != null) {
                session.close().subscribe();
            }
        }
    }
}
