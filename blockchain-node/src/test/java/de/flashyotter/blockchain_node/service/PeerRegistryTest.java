package de.flashyotter.blockchain_node.service;

import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.p2p.Peer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PeerRegistryTest {

    @Test
    void addAndAllWorks() {
        PeerRegistry reg = new PeerRegistry(new NodeProperties());
        Peer a = new Peer("a", 1);
        Peer b = new Peer("b", 2);

        reg.add(a);
        reg.add(b);
        // duplicate
        reg.add(new Peer("a", 1));

        assertEquals(2, reg.all().size());
        assertTrue(reg.all().contains(a));
        assertTrue(reg.all().contains(b));
    }

    @Test
    void addAllWorks() {
        PeerRegistry reg = new PeerRegistry(new NodeProperties());
        var list = java.util.List.of(new Peer("x", 9), new Peer("y", 8));
        reg.addAll(list);
        assertEquals(2, reg.all().size());
    }

    @Test
    void mergesAdditionalPeerDetails() {
        PeerRegistry reg = new PeerRegistry(new NodeProperties());
        Peer placeholder = new Peer("1.2.3.4", 0, 4001, null);
        assertTrue(reg.add(placeholder));

        Peer enriched = new Peer("1.2.3.4", 3333, 4001, "peer-1");
        assertFalse(reg.add(enriched));

        assertEquals(1, reg.all().size());
        assertTrue(reg.all().contains(enriched));
        assertEquals(enriched, reg.pending().poll());
    }
}
