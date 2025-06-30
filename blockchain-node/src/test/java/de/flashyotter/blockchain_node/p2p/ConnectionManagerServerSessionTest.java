package de.flashyotter.blockchain_node.p2p;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.flashyotter.blockchain_node.dto.HandshakeDto;
import de.flashyotter.blockchain_node.dto.P2PMessageDto;
import de.flashyotter.blockchain_node.dto.PeerListDto;
import de.flashyotter.blockchain_node.dto.GetBlocksDto;
import de.flashyotter.blockchain_node.config.NodeProperties;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

class ConnectionManagerServerSessionTest {

    @Mock ObjectMapper mapper;
    @Mock WebSocketSession session;
    @Mock HandshakeInfo info;
    @Mock NodeProperties props;

    ConnectionManager manager;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        manager = new ConnectionManager(Mockito.mock(org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient.class), mapper, props);
        when(session.getHandshakeInfo()).thenReturn(info);
        when(session.send(any())).thenReturn(Mono.empty());
        when(session.closeStatus()).thenReturn(Mono.never());
        when(session.textMessage(any())).thenReturn(Mockito.mock(WebSocketMessage.class));
    }

    @Test
    void buffersFramesUntilHandshake() throws Exception {
        when(info.getRemoteAddress()).thenReturn(new InetSocketAddress("h", 5));
        when(props.getId()).thenReturn("n1");
        when(props.getPort()).thenReturn(1);

        Sinks.Many<WebSocketMessage> incoming = Sinks.many().unicast().onBackpressureBuffer();
        when(session.receive()).thenReturn(incoming.asFlux());

        Peer temp = new Peer("h",5);
        HandshakeDto hs = new HandshakeDto("n2","0.4.0",42);
        PeerListDto before = new PeerListDto(List.of());
        GetBlocksDto after = new GetBlocksDto(0);

        when(mapper.writeValueAsString(any())).thenReturn("{}");
        when(mapper.readValue("pre", P2PMessageDto.class)).thenReturn(before);
        when(mapper.readValue("hs", P2PMessageDto.class)).thenReturn(hs);
        when(mapper.readValue("post", P2PMessageDto.class)).thenReturn(after);

        ConnectionManager.Conn conn = manager.registerServerSession(temp, session);
        List<P2PMessageDto> got = new CopyOnWriteArrayList<>();
        conn.inbound().subscribe(got::add);

        WebSocketMessage m1 = Mockito.mock(WebSocketMessage.class); when(m1.getPayloadAsText()).thenReturn("pre");
        WebSocketMessage m2 = Mockito.mock(WebSocketMessage.class); when(m2.getPayloadAsText()).thenReturn("hs");
        WebSocketMessage m3 = Mockito.mock(WebSocketMessage.class); when(m3.getPayloadAsText()).thenReturn("post");

        incoming.tryEmitNext(m1);
        incoming.tryEmitNext(m2);
        incoming.tryEmitNext(m3);

        Peer actual = new Peer("h",42);
        Awaitility.await().until(() -> got.size() == 3);

        assertEquals(List.of(before, hs, after), got);
        assertSame(conn, manager.connectAndSink(actual));
        assertNotSame(conn, manager.connectAndSink(temp));
    }
}
