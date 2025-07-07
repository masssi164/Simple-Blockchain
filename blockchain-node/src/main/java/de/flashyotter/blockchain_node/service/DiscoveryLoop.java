package de.flashyotter.blockchain_node.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import de.flashyotter.blockchain_node.p2p.Peer;
import lombok.RequiredArgsConstructor;

// blockchain-node/src/main/java/de/flashyotter/blockchain_node/service/DiscoveryLoop.java
@Service
@RequiredArgsConstructor
public class DiscoveryLoop {

    private final PeerRegistry   reg;
    private final SyncService    sync;
    private final KademliaService kademlia;
    private final de.flashyotter.blockchain_node.p2p.libp2p.Libp2pService libp2p;

    @Scheduled(fixedDelay = 1000)
    void pollPendingPeers() {
        Peer p;
        while ((p = reg.pending().poll()) != null) {
            kademlia.store(p);
            sync.followPeer(p).subscribe();
            libp2p.send(p, new de.flashyotter.blockchain_node.dto.FindNodeDto(kademlia.selfId()));
        }
    }
}

