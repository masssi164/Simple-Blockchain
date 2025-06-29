package de.flashyotter.blockchain_node.service;

import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.p2p.Peer;
import de.flashyotter.blockchain_node.p2p.PeerClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;          // ← switched to Jakarta namespace
import de.flashyotter.blockchain_node.discovery.PeerDiscoveryService;

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
    private final PeerDiscoveryService discovery;
    private final PeerClient           client;

    @PostConstruct
    public void init() {
        props.getPeers().forEach(addr -> {
            var sp = addr.split(":");
            Peer p = new Peer(sp[0], Integer.parseInt(sp[1]));
            registry.add(p);
            client.connect(p);
        });

        registry.all()
                .forEach(p -> syncService.followPeer(p.wsUrl()).subscribe());

        broadcaster.broadcastPeerList();
        // trigger discovery after initial connections
        registry.all().forEach(discovery::query);
    }
}
