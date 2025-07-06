package de.flashyotter.blockchain_node.service;

import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.p2p.Peer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;          // ← switched to Jakarta namespace

/**
 * Boot-time peer connector; advertises peer list afterwards.
 */
@Service
@RequiredArgsConstructor
public class PeerService {

    private final NodeProperties       props;
    private final SyncService          syncService;
    private final PeerRegistry         registry;
    private final P2PBroadcastService  broadcaster;

    @PostConstruct
    public void init() {
        props.getPeers().forEach(addr -> {
            var sp = addr.split(":");
            registry.add(new Peer(sp[0], Integer.parseInt(sp[1])));
        });

        registry.all()
                .forEach(p -> syncService.followPeer(p).subscribe());

        broadcaster.broadcastPeerList();
    }
}
