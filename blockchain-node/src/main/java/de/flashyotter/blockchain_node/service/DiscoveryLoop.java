package de.flashyotter.blockchain_node.service;

import org.springframework.stereotype.Service;

import de.flashyotter.blockchain_node.p2p.Peer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

// blockchain-node/src/main/java/de/flashyotter/blockchain_node/service/DiscoveryLoop.java
@Service
@RequiredArgsConstructor
public class DiscoveryLoop {

    private final PeerRegistry reg;
    private final SyncService  sync;

    @PostConstruct
    void loop() {
        reactor.core.scheduler.Schedulers.boundedElastic().schedule(() -> {
            while (true) {
                try {
                    Peer p = reg.pending().take();
                    sync.followPeer(p).subscribe();
                } catch (InterruptedException ignored) { }
            }
        });
    }
}
