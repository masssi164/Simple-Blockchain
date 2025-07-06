package de.flashyotter.blockchain_node.service;

import org.springframework.stereotype.Service;

import de.flashyotter.blockchain_node.p2p.Peer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

// blockchain-node/src/main/java/de/flashyotter/blockchain_node/service/DiscoveryLoop.java
@Service
@RequiredArgsConstructor
public class DiscoveryLoop {

    private final PeerRegistry   reg;
    private final SyncService    sync;
    private final KademliaService kademlia;
    private final de.flashyotter.blockchain_node.p2p.libp2p.Libp2pService libp2p;

    @PostConstruct
    void loop() {
        reactor.core.scheduler.Schedulers.boundedElastic().schedule(() -> {
            while (true) {
                try {
                    Peer p = reg.pending().take();
                    kademlia.store(p);
                    sync.followPeer(p).subscribe();
                    libp2p.send(p, new de.flashyotter.blockchain_node.dto.FindNodeDto(kademlia.selfId()));
                } catch (InterruptedException ignored) { }
            }
        });
    }
}
