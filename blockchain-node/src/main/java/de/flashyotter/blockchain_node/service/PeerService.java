package de.flashyotter.blockchain_node.service;

import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.p2p.Peer;
import de.flashyotter.blockchain_node.p2p.libp2p.Libp2pService;
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
    private final KademliaService      kademlia;
    private final de.flashyotter.blockchain_node.p2p.libp2p.Libp2pService libp2p;

    @PostConstruct
    public void init() {
        props.getPeers().forEach(addr -> {
            var sp = addr.split(":");
            Peer p = new Peer(sp[0], Integer.parseInt(sp[1]));
            registry.add(p);
            kademlia.store(p);
        });

        registry.all()
                .forEach(p -> {
                    syncService.followPeer(p).subscribe();
                    // bootstrap discovery via kademlia
                    libp2p.send(p, new de.flashyotter.blockchain_node.dto.FindNodeDto(kademlia.selfId()));
                    libp2p.send(p, new de.flashyotter.blockchain_node.dto.HandshakeDto(
                            props.getId(),
                            libp2p.protocolVersion(),
                            props.getLibp2pPort(),
                            libp2p.getPublicAddr()));
                });

        broadcaster.broadcastPeerList();
    }
}
