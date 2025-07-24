package de.flashyotter.blockchain_node.service;

import de.flashyotter.blockchain_node.p2p.Peer;
import org.apache.tuweni.kademlia.KademliaRoutingTable;

public class TablePeerStore implements PeerStore {
    private final KademliaRoutingTable<Peer> table;

    public TablePeerStore(KademliaRoutingTable<Peer> table) {
        this.table = table;
    }

    @Override
    public void addPeer(Peer peer) {
        table.add(peer);
    }

    @Override
    public void removePeer(Peer peer) {
        table.remove(peer);
    }
}
