package de.flashyotter.blockchain_node.service;

import de.flashyotter.blockchain_node.p2p.Peer;

public interface PeerStore {
    void addPeer(Peer peer);
    void removePeer(Peer peer);
}
