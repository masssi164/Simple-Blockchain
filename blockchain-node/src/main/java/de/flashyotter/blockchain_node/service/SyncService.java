package de.flashyotter.blockchain_node.service;

import org.springframework.stereotype.Service;

import de.flashyotter.blockchain_node.p2p.libp2p.Libp2pService;
import de.flashyotter.blockchain_node.p2p.Peer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor @Slf4j
public class SyncService {

    private final NodeService  node;
    private final Libp2pService libp2p;

    /** Currently gossip-based sync is handled by libp2p handlers. */
    public Flux<Void> followPeer(Peer peer) {
        // libp2p streams gossip blocks and transactions automatically
        return Flux.empty();
    }

}
