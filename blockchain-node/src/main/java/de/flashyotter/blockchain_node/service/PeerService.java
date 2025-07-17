package de.flashyotter.blockchain_node.service;

import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.p2p.Peer;
import de.flashyotter.blockchain_node.p2p.libp2p.Libp2pService;
import de.flashyotter.blockchain_node.dto.PeerIdDto;
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
        int offset = props.getLibp2pPort() - props.getPort();
        props.getPeers().forEach(addr -> {
            var sp = addr.split(":");
            String host = sp[0];
            int port = Integer.parseInt(sp[1]);
            PeerIdDto dto = null;
            int httpPort = port - offset;
            // nodes may take a while to start in CI. wait up to ~60s
            for (int i = 0; i < 60 && dto == null; i++) {
                try {
                    dto = webClient.get()
                            .uri("http://" + host + ':' + httpPort + "/node/peer-id")
                            .retrieve()
                            .bodyToMono(PeerIdDto.class)
                            .block(java.time.Duration.ofSeconds(3));
                } catch (Exception e) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            if (dto == null) {
                // warn so tests can diagnose missing peer-id and skip peer
                org.slf4j.LoggerFactory.getLogger(PeerService.class)
                        .warn("Failed to resolve peer id for {}:{} after waiting", host, httpPort);
                return; // do not add peer without id
            }

            Peer p = new Peer(host, port, dto.peerId());
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
                            props.getLibp2pPort()));
                });

        broadcaster.broadcastPeerList();
    }
}
