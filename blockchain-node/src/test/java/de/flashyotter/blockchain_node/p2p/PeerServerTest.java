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
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.dto.HandshakeDto;
import de.flashyotter.blockchain_node.dto.P2PMessageDto;
import de.flashyotter.blockchain_node.dto.PeerListDto;
import de.flashyotter.blockchain_node.discovery.FindNodeDto;
import de.flashyotter.blockchain_node.discovery.PeerDiscoveryService;
import de.flashyotter.blockchain_node.discovery.PingDto;
import de.flashyotter.blockchain_node.discovery.PongDto;
import de.flashyotter.blockchain_node.discovery.NodesDto;
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_node.service.P2PBroadcastService;
import de.flashyotter.blockchain_node.service.PeerRegistry;
import de.flashyotter.blockchain_node.service.SyncService;
import de.flashyotter.blockchain_node.p2p.ConnectionManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Mono;

class PeerServerTest {

    @Mock ObjectMapper mapper;
    @Mock NodeService nodeService;
    @Mock PeerRegistry registry;
    @Mock P2PBroadcastService broadcastService;
    @Mock NodeProperties props;
    @Mock PeerDiscoveryService discovery;
    @Mock SyncService syncService;
    @Mock WebSocketSession session;
    @Mock HandshakeInfo info;
    @Mock reactor.netty.http.client.HttpClient httpClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        try { when(mapper.writeValueAsString(any())).thenReturn("{}"); } catch (Exception ignored) {}
        when(session.getHandshakeInfo()).thenReturn(info);
        when(session.receive()).thenReturn(Flux.never());
        when(session.send(org.mockito.ArgumentMatchers.any())).thenReturn(Mono.empty());
        when(session.closeStatus()).thenReturn(Mono.never());
        when(session.textMessage(org.mockito.ArgumentMatchers.anyString())).thenReturn(org.mockito.Mockito.mock(WebSocketMessage.class));
    }

    @Test
    void handleRegistersSession() {
        ConnectionManager manager = org.mockito.Mockito.mock(ConnectionManager.class);
        PeerServer peerServer = new PeerServer(mapper, nodeService, registry, broadcastService, props, discovery, manager, syncService);
        when(props.getId()).thenReturn("n1");
        when(info.getRemoteAddress()).thenReturn(new InetSocketAddress("host", 1));
        ConnectionManager.Conn conn = new ConnectionManager.Conn(Sinks.many().multicast().onBackpressureBuffer(),
                                                                Sinks.many().multicast().onBackpressureBuffer(),
                                                                Flux.empty());
        when(manager.registerServerSession(any(), any())).thenReturn(conn);

        peerServer.handle(session).subscribe();

        verify(manager).registerServerSession(new Peer("host", 1), session);
        verify(session).send(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void outboundMessagesAreSentThroughSession() {
        var wsClient = org.mockito.Mockito.mock(org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient.class);
        ConnectionManager manager = new ConnectionManager(wsClient, mapper, props);
        PeerServer peerServer = new PeerServer(mapper, nodeService, registry, broadcastService, props, discovery, manager, syncService);
        when(props.getId()).thenReturn("n1");
        when(info.getRemoteAddress()).thenReturn(new InetSocketAddress("host", 2));

        peerServer.handle(session).subscribe();
        ConnectionManager.Conn c = manager.connectAndSink(new Peer("host", 2));
        c.outbound().tryEmitNext("{\"hello\":1}");

        Awaitility.await().untilAsserted(() ->
            verify(session, times(2)).send(org.mockito.ArgumentMatchers.any())
        );
    }

    @Test
    void closingSessionRemovesConnection() {
        var wsClient = org.mockito.Mockito.mock(org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient.class);
        ConnectionManager manager = new ConnectionManager(wsClient, mapper, props);
        PeerServer peerServer = new PeerServer(mapper, nodeService, registry, broadcastService, props, discovery, manager, syncService);
        when(props.getId()).thenReturn("n1");
        when(info.getRemoteAddress()).thenReturn(new InetSocketAddress("host", 3));
        Sinks.One<CloseStatus> close = Sinks.one();
        when(session.closeStatus()).thenReturn(close.asMono());

        peerServer.handle(session).subscribe();
        Peer peer = new Peer("host", 3);
        ConnectionManager.Conn first = manager.connectAndSink(peer);

        close.tryEmitValue(CloseStatus.NORMAL);
        Awaitility.await().until(() -> !manager.connectAndSink(peer).equals(first));
    }

    @Test
    void handleHandshake_addsPeerAndBroadcasts() {
        ConnectionManager manager = new ConnectionManager(org.mockito.Mockito.mock(org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient.class), mapper, props);
        PeerServer peerServer = new PeerServer(mapper, nodeService, registry, broadcastService, props, discovery, manager, syncService);
        when(info.getRemoteAddress()).thenReturn(new InetSocketAddress("host", 9));
        HandshakeDto dto = new HandshakeDto("n2", "0.4.0");
        when(registry.add(any())).thenReturn(true);
        when(syncService.followPeer(any())).thenReturn(Flux.empty());

        peerServer.handle(session).subscribe();
        Peer peer = new Peer("host", 9);
        manager.emitInbound(peer, dto);

        Awaitility.await().untilAsserted(() -> {
            verify(registry).add(peer);
            verify(broadcastService).broadcastPeerList();
            verify(discovery).onMessage(dto, peer);
            verify(syncService).followPeer(peer);
        });
    }

    @Test
    void handlePeerList_addsPeers() {
        ConnectionManager manager = new ConnectionManager(org.mockito.Mockito.mock(org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient.class), mapper, props);
        PeerServer peerServer = new PeerServer(mapper, nodeService, registry, broadcastService, props, discovery, manager, syncService);
        when(info.getRemoteAddress()).thenReturn(new InetSocketAddress("src", 4));
        PeerListDto dto = new PeerListDto(java.util.List.of("h1:1", "h2:2"));

        peerServer.handle(session).subscribe();
        manager.emitInbound(new Peer("src", 4), dto);

        java.util.List<Peer> expected = java.util.List.of(new Peer("h1",1), new Peer("h2",2));
        Awaitility.await().untilAsserted(() -> verify(registry).addAll(expected));
    }

    @Test
    void handleDiscoveryMessages_delegateToService() {
        ConnectionManager manager = new ConnectionManager(org.mockito.Mockito.mock(org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient.class), mapper, props);
        PeerServer peerServer = new PeerServer(mapper, nodeService, registry, broadcastService, props, discovery, manager, syncService);
        when(info.getRemoteAddress()).thenReturn(new InetSocketAddress("h", 5));

        peerServer.handle(session).subscribe();
        manager.emitInbound(new Peer("h", 5), new PingDto("a"));
        manager.emitInbound(new Peer("h", 5), new PongDto("b"));
        manager.emitInbound(new Peer("h", 5), new FindNodeDto("c"));
        manager.emitInbound(new Peer("h", 5), new NodesDto(java.util.List.of()));

        Peer peer = new Peer("h", 5);
        Awaitility.await().untilAsserted(() ->
            verify(discovery, times(4)).onMessage(any(), org.mockito.ArgumentMatchers.eq(peer))
        );
    }
}
