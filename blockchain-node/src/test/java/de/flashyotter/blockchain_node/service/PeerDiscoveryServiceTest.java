package de.flashyotter.blockchain_node.service;

import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.discovery.NodesDto;
import de.flashyotter.blockchain_node.discovery.PeerDiscoveryService;
import de.flashyotter.blockchain_node.p2p.Peer;
import de.flashyotter.blockchain_node.p2p.PeerClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

class PeerDiscoveryServiceTest {

    @Test
    void discoveredNodesAreAddedToRegistry() {
        NodeProperties props = new NodeProperties();
        props.setId("nodeA");
        PeerRegistry registry = new PeerRegistry();
        PeerClient client = mock(PeerClient.class);
        PeerDiscoveryService svc = new PeerDiscoveryService(props, client, registry);

        svc.onMessage(new NodesDto(List.of("host1:1111")), new Peer("seed", 0));

        assertTrue(registry.all().contains(new Peer("host1",1111)), "peer added");
        assertEquals(1, registry.pending().size(), "pending queue populated");
        verify(client).connect(new Peer("host1", 1111));
    }
}
