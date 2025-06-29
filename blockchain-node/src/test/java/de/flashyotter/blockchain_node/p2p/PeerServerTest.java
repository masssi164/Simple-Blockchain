package de.flashyotter.blockchain_node.p2p;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import java.net.InetSocketAddress;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentCaptor;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.discovery.PeerDiscoveryService;
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_node.service.P2PBroadcastService;
import de.flashyotter.blockchain_node.service.PeerRegistry;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

class PeerServerTest {

    @Mock ObjectMapper mapper;
    @Mock NodeService nodeService;
    @Mock PeerRegistry registry;
    @Mock P2PBroadcastService broadcastService;
    @Mock NodeProperties props;
    @Mock PeerDiscoveryService discovery;
    @Mock WebSocketSession session;
    @Mock reactor.netty.http.client.HttpClient httpClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        try { when(mapper.writeValueAsString(any())).thenReturn("{}"); } catch (Exception ignored) {}
    }

    @Test
    void afterConnectionEstablished_registersSession() throws Exception {
        ConnectionManager manager = org.mockito.Mockito.mock(ConnectionManager.class);
        PeerServer peerServer = new PeerServer(mapper, nodeService, registry, broadcastService, props, discovery, manager);
        when(props.getId()).thenReturn("n1");
        when(session.getRemoteAddress()).thenReturn(new InetSocketAddress("host", 1));

        peerServer.afterConnectionEstablished(session);

        verify(manager).registerServerSession(new Peer("host", 1), session);
        verify(session).sendMessage(any(TextMessage.class));
    }

    @Test
    void outboundMessagesAreSentThroughSession() throws Exception {
        var wsClient = org.mockito.Mockito.mock(org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient.class);
        ConnectionManager manager = new ConnectionManager(wsClient, mapper, props);
        PeerServer peerServer = new PeerServer(mapper, nodeService, registry, broadcastService, props, discovery, manager);
        when(props.getId()).thenReturn("n1");
        when(session.getRemoteAddress()).thenReturn(new InetSocketAddress("host", 2));
        when(session.isOpen()).thenReturn(true, false);

        peerServer.afterConnectionEstablished(session);
        ConnectionManager.Conn c = manager.connectAndSink(new Peer("host", 2));
        c.outbound().tryEmitNext("{\"hello\":1}");

        Awaitility.await().untilAsserted(() ->
            verify(session, times(2)).sendMessage(any(TextMessage.class))
        );
    }

    @Test
    void closingSessionRemovesConnection() throws Exception {
        var wsClient = org.mockito.Mockito.mock(org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient.class);
        ConnectionManager manager = new ConnectionManager(wsClient, mapper, props);
        PeerServer peerServer = new PeerServer(mapper, nodeService, registry, broadcastService, props, discovery, manager);
        when(props.getId()).thenReturn("n1");
        when(session.getRemoteAddress()).thenReturn(new InetSocketAddress("host", 3));
        when(session.isOpen()).thenReturn(true, false);

        peerServer.afterConnectionEstablished(session);
        Peer peer = new Peer("host", 3);
        ConnectionManager.Conn first = manager.connectAndSink(peer);

        Awaitility.await().until(() -> !manager.connectAndSink(peer).equals(first));
    }
}
