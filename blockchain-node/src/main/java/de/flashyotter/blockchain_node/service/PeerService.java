package de.flashyotter.blockchain_node.service;

import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.p2p.Peer;
import de.flashyotter.blockchain_node.p2p.libp2p.Libp2pService;
import de.flashyotter.blockchain_node.dto.NodeIdDto;
import org.springframework.web.reactive.function.client.WebClient;
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
    private final WebClient            webClient;

    @PostConstruct
    public void init() {
        int delta = props.getLibp2pPort() - props.getPort();

        props.getPeers().forEach(addr -> {
            var sp = addr.split(":");
            String host = sp[0];
            int port = Integer.parseInt(sp[1]);
            int httpPort = port - delta;
            try {
                NodeIdDto dto = webClient.get()
                        .uri("http://" + host + ':' + httpPort + "/node/id")
                        .retrieve()
                        .bodyToMono(NodeIdDto.class)
                        .block(java.time.Duration.ofSeconds(3));
                Peer p = new Peer(host, port, dto != null ? dto.nodeId() : null);
                registry.add(p);
                kademlia.store(p);
            } catch (Exception e) {
                Peer p = new Peer(host, port);
                registry.add(p);
                kademlia.store(p);
            }
        });

        registry.all()
                .forEach(p -> {
                    syncService.followPeer(p).subscribe();
                    // bootstrap discovery via kademlia
                    libp2p.send(p, new de.flashyotter.blockchain_node.dto.FindNodeDto(kademlia.selfId()));
                    libp2p.send(p, new de.flashyotter.blockchain_node.dto.HandshakeDto(
                            props.getId(),
                            libp2p.protocolVersion(),
                            props.getLibp2pPort()));
                });

        broadcaster.broadcastPeerList();
    }
}
