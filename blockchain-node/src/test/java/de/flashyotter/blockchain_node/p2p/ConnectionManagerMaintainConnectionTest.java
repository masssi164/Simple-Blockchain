package de.flashyotter.blockchain_node.p2p;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.dto.GetBlocksDto;
import de.flashyotter.blockchain_node.dto.HandshakeDto;
import de.flashyotter.blockchain_node.dto.P2PMessageDto;
import de.flashyotter.blockchain_node.dto.PeerListDto;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

class ConnectionManagerMaintainConnectionTest {

    @Mock ReactorNettyWebSocketClient client;
    @Mock ObjectMapper mapper;
    @Mock NodeProperties props;
    @Mock WebSocketSession session;

    ConnectionManager manager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        manager = new ConnectionManager(client, mapper, props);
        when(session.send(any())).thenReturn(Mono.empty());
        when(session.close()).thenReturn(Mono.empty());
        when(session.textMessage(anyString())).thenReturn(Mockito.mock(WebSocketMessage.class));
        when(client.execute(any(URI.class), any(WebSocketHandler.class)))
                .thenAnswer(inv -> ((WebSocketHandler) inv.getArgument(1)).handle(session));
    }

    @Test
    void buffersFramesUntilHandshake() throws Exception {
        when(props.getId()).thenReturn("n1");
        when(props.getPort()).thenReturn(1);

        Sinks.Many<WebSocketMessage> incoming = Sinks.many().unicast().onBackpressureBuffer();
        when(session.receive()).thenReturn(incoming.asFlux());

        Peer peer = new Peer("h", 5);

        HandshakeDto hs = new HandshakeDto("n2", "0.4.0", 42);
        PeerListDto before = new PeerListDto(List.of());
        GetBlocksDto after = new GetBlocksDto(0);

        when(mapper.writeValueAsString(any())).thenReturn("{}");
        when(mapper.readValue("pre", P2PMessageDto.class)).thenReturn(before);
        when(mapper.readValue("hs", P2PMessageDto.class)).thenReturn(hs);
        when(mapper.readValue("post", P2PMessageDto.class)).thenReturn(after);

        ConnectionManager.Conn conn = manager.connectAndSink(peer);
        List<P2PMessageDto> got = new CopyOnWriteArrayList<>();
        conn.inbound().subscribe(got::add);

        WebSocketMessage m1 = Mockito.mock(WebSocketMessage.class); when(m1.getPayloadAsText()).thenReturn("pre");
        WebSocketMessage m2 = Mockito.mock(WebSocketMessage.class); when(m2.getPayloadAsText()).thenReturn("hs");
        WebSocketMessage m3 = Mockito.mock(WebSocketMessage.class); when(m3.getPayloadAsText()).thenReturn("post");

        incoming.tryEmitNext(m1);
        incoming.tryEmitNext(m2);
        incoming.tryEmitNext(m3);

        Awaitility.await().until(() -> got.size() == 3);
        assertEquals(List.of(before, hs, after), got);
        assertSame(conn, manager.connectAndSink(peer));
    }

    @Test
    void closesIfNoHandshake() {
        when(props.getId()).thenReturn("n1");
        when(props.getPort()).thenReturn(1);

        when(session.receive()).thenReturn(Flux.never());

        Peer peer = new Peer("h", 7);
        manager.connectAndSink(peer);

        Awaitility.await().atMost(Duration.ofSeconds(6))
                  .untilAsserted(() -> Mockito.verify(session).close());
    }
}

