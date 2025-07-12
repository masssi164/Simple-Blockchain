package de.flashyotter.blockchain_node.p2p;

import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.p2p.libp2p.Libp2pService;
import de.flashyotter.blockchain_node.service.KademliaService;
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_node.service.PeerRegistry;
import io.libp2p.core.Host;
import io.libp2p.core.PeerId;
import io.libp2p.core.multiformats.Multiaddr;
import org.apache.tuweni.kademlia.KademliaRoutingTable;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class Libp2pAutoNatTest {
    @Test
    void discoverPublicAddrUsesFirstListenAddress() {
        Host host = mock(Host.class);
        NodeService node = mock(NodeService.class);
        NodeProperties props = new NodeProperties();
        props.setId("n1");
        Multiaddr addr = new Multiaddr("/ip4/9.9.9.9/tcp/4001");
        when(host.listenAddresses()).thenReturn(List.of(addr));
        when(host.getPeerId()).thenReturn(PeerId.random());

        KademliaRoutingTable<Peer> table = KademliaRoutingTable.create(
                props.getId().getBytes(StandardCharsets.UTF_8), 16,
                p -> p.toString().getBytes(StandardCharsets.UTF_8), p -> 0);
        PeerRegistry reg = new PeerRegistry();
        KademliaService kad = new KademliaService(table, reg, props);

        Libp2pService svc = new Libp2pService(host, props, node, kad);
        svc.discoverPublicAddr(new Peer("dummy", 1));

        assertEquals(addr.toString(), svc.getPublicAddr());
    }
}
