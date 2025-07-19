package de.flashyotter.blockchain_node.service;

import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.p2p.Peer;
import de.flashyotter.blockchain_node.p2p.libp2p.Libp2pService;
import de.flashyotter.blockchain_node.dto.FindNodeDto;
import de.flashyotter.blockchain_node.dto.HandshakeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.scheduler.Schedulers;
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

    /** initial connection attempts during startup */
    private static final int MAX_INIT_ATTEMPTS = 40;

    @PostConstruct
    public void init() {
        props.getPeers().forEach(addr -> {
            var sp = addr.split(":");
            String host = sp[0];
            int port = Integer.parseInt(sp[1]);
            Peer peer = new Peer(host, port);
            registry.add(peer);
            kademlia.store(peer);
            postAdd(peer);
        });

        broadcaster.broadcastPeerList();
    }

    private void postAdd(Peer p) {
        syncService.followPeer(p)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
        libp2p.send(p, new FindNodeDto(kademlia.selfId()));
        libp2p.send(p, new HandshakeDto(
                props.getId(),
                libp2p.peerId(),
                libp2p.protocolVersion(),
                props.getLibp2pPort(),
                props.getPort()));
    }
}
