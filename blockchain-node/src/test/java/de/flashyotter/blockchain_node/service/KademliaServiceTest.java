package de.flashyotter.blockchain_node.service;

import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.dto.NodesDto;
import de.flashyotter.blockchain_node.p2p.Peer;
import org.apache.tuweni.kademlia.KademliaRoutingTable;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KademliaServiceTest {

    private KademliaService service(KademliaRoutingTable<Peer>[] holder) {
        NodeProperties props = new NodeProperties();
        props.setId("self");
        KademliaRoutingTable<Peer> table = KademliaRoutingTable.create(
                props.getId().getBytes(StandardCharsets.UTF_8),
                16,
                p -> p.toString().getBytes(StandardCharsets.UTF_8),
                p -> 0);
        holder[0] = table;
        return new KademliaService(table, new PeerRegistry(), props);
    }

    @Test
    void storeAddsPeer() {
        KademliaRoutingTable<Peer>[] holder = new KademliaRoutingTable[1];
        KademliaService svc = service(holder);
        Peer peer = new Peer("a",1);
        svc.store(peer);
        assertEquals(1, holder[0].size());
    }

    @Test
    void mergeAddsAllPeers() {
        KademliaRoutingTable<Peer>[] holder = new KademliaRoutingTable[1];
        KademliaService svc = service(holder);
        svc.merge(new NodesDto(List.of("h:1", "b:2")));
        assertEquals(2, holder[0].size());
    }
}
