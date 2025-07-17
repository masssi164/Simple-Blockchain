package de.flashyotter.blockchain_node.service;

import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.p2p.Peer;
import de.flashyotter.blockchain_node.p2p.libp2p.Libp2pService;
import de.flashyotter.blockchain_node.dto.PeerIdDto;
import de.flashyotter.blockchain_node.dto.FindNodeDto;
import de.flashyotter.blockchain_node.dto.HandshakeDto;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
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

    /** peers without a resolved id mapped to first-seen timestamp */
    private final java.util.Map<Peer, Long> unresolved =
            new java.util.concurrent.ConcurrentHashMap<>();
    private static final long RETRY_LIMIT_MS = java.util.concurrent.TimeUnit.MINUTES.toMillis(10);

    @PostConstruct
    public void init() {
        int offset = props.getLibp2pPort() - props.getPort();
        props.getPeers().forEach(addr -> {
            var sp = addr.split(":");
            String host = sp[0];
            int port = Integer.parseInt(sp[1]);
            Peer peer = new Peer(host, port);
            if (!resolvePeerId(peer, 60)) {
                org.slf4j.LoggerFactory.getLogger(PeerService.class)
                        .warn("Failed to resolve peer id for {}:{} after waiting", host, port - offset);
                unresolved.put(peer, System.currentTimeMillis());
            }
        });

        broadcaster.broadcastPeerList();
    }

    private boolean resolvePeerId(Peer peer, int attempts) {
        int offset = props.getLibp2pPort() - props.getPort();
        String host = peer.getHost();
        int port = peer.getPort();
        int httpPort = port - offset;
        PeerIdDto dto = null;
        for (int i = 0; i < attempts && dto == null; i++) {
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
            return false;
        }
        Peer withId = new Peer(host, port, dto.peerId());
        registry.add(withId);
        kademlia.store(withId);
        postAdd(withId);
        return true;
    }

    private void postAdd(Peer p) {
        syncService.followPeer(p).subscribe();
        libp2p.send(p, new FindNodeDto(kademlia.selfId()));
        libp2p.send(p, new HandshakeDto(
                props.getId(),
                libp2p.protocolVersion(),
                props.getLibp2pPort()));
    }

    // Retry more aggressively so newly started peers join within a few seconds
    @Scheduled(fixedDelay = 5000)
    public void retryMissingPeers() {
        long now = System.currentTimeMillis();
        unresolved.forEach((peer, since) -> {
            if (now - since > RETRY_LIMIT_MS) {
                unresolved.remove(peer);
            } else if (resolvePeerId(peer, 3)) {
                unresolved.remove(peer);
                broadcaster.broadcastPeerList();
            }
        });
    }
}
