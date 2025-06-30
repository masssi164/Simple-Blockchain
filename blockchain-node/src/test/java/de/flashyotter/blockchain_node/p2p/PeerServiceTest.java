package de.flashyotter.blockchain_node.p2p;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.p2p.Peer;
import de.flashyotter.blockchain_node.service.P2PBroadcastService;
import de.flashyotter.blockchain_node.service.PeerRegistry;
import de.flashyotter.blockchain_node.service.PeerService;
import de.flashyotter.blockchain_node.service.SyncService;
import de.flashyotter.blockchain_node.discovery.PeerDiscoveryService;
import reactor.core.publisher.Flux;

class PeerServiceTest {

    @Mock
    private SyncService sync;

    @Spy
    private PeerRegistry reg = new PeerRegistry();

    @Mock
    private P2PBroadcastService broad;

    @Mock
    private PeerDiscoveryService discovery;

    private PeerService svc;
    private NodeProperties props;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        props = new NodeProperties();
        // set two peers
        props.setPeers(java.util.List.of("one:100", "two:200"));
        svc = new PeerService(props, sync, reg, broad, discovery);
    }

    @Test
    void initAddsAndSyncsAndBroadcasts() {
        // stub sync to return an empty flux
        when(sync.followPeer(org.mockito.ArgumentMatchers.any(Peer.class))).thenReturn(Flux.empty());

        svc.init();

        // registry.add for each peer string
        verify(reg).add(new Peer("one", 100));
        verify(reg).add(new Peer("two", 200));

        // followPeer called for each wsUrl
        verify(sync).followPeer(new Peer("one", 100));
        verify(sync).followPeer(new Peer("two", 200));

        // broadcastPeerList at end
        verify(broad).broadcastPeerList();
    }
}
