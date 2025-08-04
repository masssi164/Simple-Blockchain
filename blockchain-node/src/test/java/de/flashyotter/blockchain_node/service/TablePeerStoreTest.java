package de.flashyotter.blockchain_node.service;

import de.flashyotter.blockchain_node.p2p.Peer;
import org.apache.tuweni.kademlia.KademliaRoutingTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class TablePeerStoreTest {

    private TablePeerStore store;
    private KademliaRoutingTable<Peer> table;

    @BeforeEach
    void setUp() {
        table = KademliaRoutingTable.create(
                "self".getBytes(StandardCharsets.UTF_8),
                16,
                p -> p.toString().getBytes(StandardCharsets.UTF_8),
                p -> 0);
        store = new TablePeerStore(table);
    }

    @Test
    void addPeerAddsToTable() {
        Peer peer = new Peer("h", 1);
        store.addPeer(peer);
        assertTrue(table.contains(peer));
    }

    @Test
    void removePeerEvictsFromTable() {
        Peer peer = new Peer("h", 1);
        store.addPeer(peer);
        store.removePeer(peer);
        assertFalse(table.contains(peer));
    }

    @Test
    void removingMissingPeerThrows() {
        Peer peer = new Peer("h", 1);
        assertThrows(IllegalStateException.class, () -> store.removePeer(peer));
    }
}
