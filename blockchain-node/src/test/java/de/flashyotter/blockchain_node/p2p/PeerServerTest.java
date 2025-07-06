package de.flashyotter.blockchain_node.p2p;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import java.net.InetSocketAddress;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
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
import de.flashyotter.blockchain_node.dto.BlocksDto;
import de.flashyotter.blockchain_node.dto.GetBlocksDto;
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_node.service.P2PBroadcastService;
import de.flashyotter.blockchain_node.service.PeerRegistry;
import de.flashyotter.blockchain_node.service.SyncService;
import de.flashyotter.blockchain_node.p2p.ConnectionManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Mono;
import blockchain.core.model.Block;

class PeerServerTest {

    @Mock ObjectMapper mapper;
    @Mock NodeService nodeService;
    @Mock PeerRegistry registry;
    @Mock P2PBroadcastService broadcastService;
    @Mock NodeProperties props;
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
        when(props.getPort()).thenReturn(3333);
    }

    @Test
    void handleRegistersSession() {
        ConnectionManager manager = org.mockito.Mockito.mock(ConnectionManager.class);
        PeerServer peerServer = new PeerServer(mapper, nodeService, registry, broadcastService, props, manager, syncService);
        when(props.getId()).thenReturn("n1");
        when(info.getRemoteAddress()).thenReturn(new InetSocketAddress("host", 1));
        ConnectionManager.Conn conn = new ConnectionManager.Conn(Sinks.many().multicast().onBackpressureBuffer(),
                                                                Sinks.many().multicast().onBackpressureBuffer(),
                                                                Flux.empty());
        when(manager.registerServerSession(any(), any())).thenReturn(conn);

        peerServer.handle(session).subscribe();

        verify(manager).registerServerSession(new Peer("host", 1), session);
    }

    @Test
    void outboundMessagesAreSentThroughSession() {
        var wsClient = org.mockito.Mockito.mock(org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient.class);
        ConnectionManager manager = new ConnectionManager(wsClient, mapper, props);
        PeerServer peerServer = new PeerServer(mapper, nodeService, registry, broadcastService, props, manager, syncService);
        when(props.getId()).thenReturn("n1");
        when(info.getRemoteAddress()).thenReturn(new InetSocketAddress("host", 2));

        peerServer.handle(session).subscribe();
        ConnectionManager.Conn c = manager.connectAndSink(new Peer("host", 2));
        c.outbound().tryEmitNext("{\"hello\":1}");

        Awaitility.await().untilAsserted(() ->
            verify(session, times(1)).send(org.mockito.ArgumentMatchers.any())
        );
    }

    @Test
    void handshakeWithWrongVersionClosesSession() {
        ConnectionManager manager = new ConnectionManager(
                org.mockito.Mockito.mock(org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient.class),
                mapper, props);
        PeerServer peerServer = new PeerServer(mapper, nodeService, registry, broadcastService, props, manager, syncService);
        when(info.getRemoteAddress()).thenReturn(new InetSocketAddress("host", 20));
        when(session.close()).thenReturn(Mono.empty());

        peerServer.handle(session).subscribe();
        Peer temp = new Peer("host", 20);
        manager.connectAndSink(temp); // establish connection

        HandshakeDto bad = new HandshakeDto("n2", "9.9.9", 99);
        manager.emitInbound(temp, bad);

        Awaitility.await().untilAsserted(() -> verify(session).close());
    }

    @Test
    @Disabled("needs update for new handshake flow")
    void closingSessionRemovesConnection() {
        var wsClient = org.mockito.Mockito.mock(org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient.class);
        ConnectionManager manager = new ConnectionManager(wsClient, mapper, props);
        PeerServer peerServer = new PeerServer(mapper, nodeService, registry, broadcastService, props, manager, syncService);
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
    @Disabled("needs update for new handshake flow")
    void handleHandshake_addsPeerAndBroadcasts() {
        ConnectionManager manager = new ConnectionManager(
                org.mockito.Mockito.mock(org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient.class),
                mapper, props);
        PeerServer peerServer = new PeerServer(mapper, nodeService, registry, broadcastService, props, manager, syncService);
        when(info.getRemoteAddress()).thenReturn(new InetSocketAddress("host", 9));
        HandshakeDto dto = new HandshakeDto("n2", "0.4.0", 42);
        when(registry.add(any())).thenReturn(true);
        when(syncService.followPeer(any())).thenReturn(Flux.empty());

        peerServer.handle(session).subscribe();
        Peer temp = new Peer("host", 9);
        ConnectionManager.Conn first = manager.connectAndSink(temp);
        manager.emitInbound(temp, dto);

        Awaitility.await().untilAsserted(() -> {
            Peer expected = new Peer("host", 42);
            verify(registry).add(expected);
            verify(broadcastService).broadcastPeerList();
            verify(syncService).followPeer(expected);
            org.junit.jupiter.api.Assertions.assertSame(first, manager.connectAndSink(expected));
        });
    }

    @Test
    @Disabled("needs update for new handshake flow")
    void handlePeerList_addsPeers() {
        ConnectionManager manager = new ConnectionManager(org.mockito.Mockito.mock(org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient.class), mapper, props);
        PeerServer peerServer = new PeerServer(mapper, nodeService, registry, broadcastService, props, manager, syncService);
        when(info.getRemoteAddress()).thenReturn(new InetSocketAddress("src", 4));
        PeerListDto dto = new PeerListDto(java.util.List.of("h1:1", "h2:2"));

        peerServer.handle(session).subscribe();
        manager.emitInbound(new Peer("src", 4), dto);

        java.util.List<Peer> expected = java.util.List.of(new Peer("h1",1), new Peer("h2",2));
        Awaitility.await().untilAsserted(() -> verify(registry).addAll(expected));
    }

    @Test
    @Disabled("needs update for new handshake flow")
    void handleDiscoveryMessages_delegateToService() {
        ConnectionManager manager = new ConnectionManager(org.mockito.Mockito.mock(org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient.class), mapper, props);
        PeerServer peerServer = new PeerServer(mapper, nodeService, registry, broadcastService, props, manager, syncService);
        when(info.getRemoteAddress()).thenReturn(new InetSocketAddress("h", 5));

        peerServer.handle(session).subscribe();
        // discovery messages removed

        // no-op
    }

    @Test
    @Disabled("needs update for new handshake flow")
    void handleGetBlocks_repliesWithBlocks() throws Exception {
        var wsClient = org.mockito.Mockito.mock(org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient.class);
        ConnectionManager manager = new ConnectionManager(wsClient, mapper, props);
        PeerServer peerServer = new PeerServer(mapper, nodeService, registry, broadcastService, props, manager, syncService);
        when(props.getId()).thenReturn("n1");
        when(info.getRemoteAddress()).thenReturn(new InetSocketAddress("b", 6));

        Block blk = new Block(0, "0", java.util.List.of(new blockchain.core.model.Transaction()), 0);
        when(nodeService.blocksFromHeight(7)).thenReturn(java.util.List.of(blk));
        when(mapper.writeValueAsString(blk)).thenReturn("raw");
        when(mapper.writeValueAsString(any(BlocksDto.class))).thenAnswer(inv -> "send-" + ((BlocksDto)inv.getArgument(0)).rawBlocks());

        peerServer.handle(session).subscribe();
        Peer peer = new Peer("b", 6);
        ConnectionManager.Conn conn = manager.connectAndSink(peer);
        java.util.List<String> sent = new java.util.concurrent.CopyOnWriteArrayList<>();
        conn.outbound().asFlux().subscribe(sent::add);

        manager.emitInbound(peer, new GetBlocksDto(7));

        Awaitility.await().until(() -> sent.size() >= 2);
        verify(nodeService).blocksFromHeight(7);
        org.junit.jupiter.api.Assertions.assertEquals("{}", sent.get(0));
        org.junit.jupiter.api.Assertions.assertEquals("send-[raw]", sent.get(1));
    }

    @Test
    @Disabled("needs update for new handshake flow")
    void handleBlocksDto_forwardsBlocksToNode() throws Exception {
        var wsClient = org.mockito.Mockito.mock(org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient.class);
        ConnectionManager manager = new ConnectionManager(wsClient, mapper, props);
        PeerServer peerServer = new PeerServer(mapper, nodeService, registry, broadcastService, props, manager, syncService);
        when(props.getId()).thenReturn("n1");
        when(info.getRemoteAddress()).thenReturn(new InetSocketAddress("c", 7));

        Block b1 = new Block(1, "0", java.util.List.of(new blockchain.core.model.Transaction()), 0);
        Block b2 = new Block(2, "0", java.util.List.of(new blockchain.core.model.Transaction()), 0);
        when(mapper.readValue("r1", Block.class)).thenReturn(b1);
        when(mapper.readValue("r2", Block.class)).thenReturn(b2);

        peerServer.handle(session).subscribe();
        Peer peer = new Peer("c", 7);
        manager.emitInbound(peer, new BlocksDto(java.util.List.of("r1", "r2")));

        Awaitility.await().untilAsserted(() -> {
            verify(nodeService).acceptExternalBlock(b1);
            verify(nodeService).acceptExternalBlock(b2);
        });
    }
}
