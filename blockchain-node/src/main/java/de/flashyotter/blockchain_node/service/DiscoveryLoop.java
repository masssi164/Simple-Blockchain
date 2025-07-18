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
    private final de.flashyotter.blockchain_node.config.NodeProperties props;

    @Scheduled(fixedDelay = 1000)
    void pollPendingPeers() {
        Peer p;
        while ((p = reg.pending().poll()) != null) {
            kademlia.store(p);
            sync.followPeer(p)
                    .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
                    .subscribe();
            libp2p.send(p, new de.flashyotter.blockchain_node.dto.FindNodeDto(kademlia.selfId()));
            libp2p.send(p, new de.flashyotter.blockchain_node.dto.HandshakeDto(
                    kademlia.selfId(),
                    libp2p.protocolVersion(),
                    props.getLibp2pPort()));
        }
    }

    /**
     * Periodically poll all known peers for new blocks. This acts as a safety
     * net in case the initial sync happens before peers start mining or a
     * broadcast gets lost.
     */
    @Scheduled(fixedDelay = 5000)
    void refreshKnownPeers() {
        for (Peer p : reg.all()) {
            sync.followPeer(p)
                    .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
                    .subscribe();
        }
    }
}

