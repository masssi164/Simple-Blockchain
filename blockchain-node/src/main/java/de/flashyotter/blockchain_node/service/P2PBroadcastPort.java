package de.flashyotter.blockchain_node.service;

import de.flashyotter.blockchain_node.dto.NewBlockDto;
import de.flashyotter.blockchain_node.dto.NewTxDto;
import de.flashyotter.blockchain_node.p2p.Peer;

/** Abstraction to allow mocking the broadcast layer in tests. */
public interface P2PBroadcastPort {

    void broadcastTx(NewTxDto dto, Peer origin);

    void broadcastBlock(NewBlockDto dto, Peer origin);

    /** Broadcast the full peer list to everybody. */
    void broadcastPeerList();
}
