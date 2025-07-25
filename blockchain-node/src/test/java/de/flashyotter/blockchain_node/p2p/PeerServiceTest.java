package de.flashyotter.blockchain_node.p2p;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.Mockito;

import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.p2p.Peer;
import de.flashyotter.blockchain_node.service.P2PBroadcastService;
import de.flashyotter.blockchain_node.service.PeerRegistry;
import de.flashyotter.blockchain_node.service.PeerService;
import de.flashyotter.blockchain_node.service.SyncService;
import de.flashyotter.blockchain_node.service.KademliaService;
import de.flashyotter.blockchain_node.p2p.libp2p.Libp2pService;
import de.flashyotter.blockchain_node.dto.FindNodeDto;
import reactor.core.publisher.Flux;

class PeerServiceTest {

    @Mock
    private SyncService sync;

    @Spy
    private PeerRegistry reg = new PeerRegistry();

    @Mock
    private P2PBroadcastService broad;

    @Mock
    private KademliaService kademlia;

    @Mock
    private Libp2pService libp2p;


    private PeerService svc;
    private NodeProperties props;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        props = new NodeProperties();
        // set two peers
        props.setPeers(java.util.List.of("one:100", "two:200"));
        svc = new PeerService(props, sync, reg, broad, kademlia, libp2p);
    }

    @Test
    void initAddsAndSyncsAndBroadcasts() {
        // stub sync to return an empty flux
        when(sync.followPeer(any())).thenReturn(Flux.empty());

        svc.init();

        // registry.add for each peer string
        verify(reg).add(new Peer("one", 0, 100));
        verify(reg).add(new Peer("two", 0, 200));
        verify(kademlia).store(new Peer("one", 0, 100));
        verify(kademlia).store(new Peer("two", 0, 200));

        // followPeer called for each peer
        verify(sync).followPeer(new Peer("one", 0, 100));
        verify(sync).followPeer(new Peer("two", 0, 200));
        verify(libp2p, times(2)).send(any(Peer.class), any(FindNodeDto.class));

        // broadcastPeerList at end
        verify(broad).broadcastPeerList();
    }
}
