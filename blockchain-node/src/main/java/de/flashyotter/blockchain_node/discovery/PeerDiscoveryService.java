package de.flashyotter.blockchain_node.discovery;

import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.p2p.Peer;
import de.flashyotter.blockchain_node.p2p.PeerClient;
import de.flashyotter.blockchain_node.service.PeerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Minimal peer discovery using Kademlia-like FIND_NODE queries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PeerDiscoveryService {

    private final NodeProperties props;
    private final PeerClient client;
    private final PeerRegistry registry;

    /** NodeID -> Peer mapping */
    private final Map<String, Peer> routing = new ConcurrentHashMap<>();

    @PostConstruct
    void bootstrap() {
        props.getPeers().forEach(addr -> {
            Peer p = Peer.fromString(addr);
            registry.add(p);
            routing.putIfAbsent(p.toString(), p);
            client.send(p, new FindNodeDto(props.getId()));
        });
    }

    /** Send a FIND_NODE request to the given peer */
    public void query(Peer p) {
        client.send(p, new FindNodeDto(props.getId()));
    }

    /** Handle incoming discovery messages. */
    public void onMessage(Object dto, Peer from) {
        if (dto instanceof de.flashyotter.blockchain_node.dto.HandshakeDto hs) {
            routing.putIfAbsent(hs.nodeId(), from);
            return;
        }
        if (dto instanceof PingDto) {
            client.send(from, new PongDto(props.getId()));
            return;
        }
        if (dto instanceof FindNodeDto fn) {
            var list = routing.values().stream()
                    .map(Peer::toString)
                    .collect(Collectors.toList());
            client.send(from, new NodesDto(list));
            return;
        }
        if (dto instanceof NodesDto nodes) {
            nodes.peers().stream()
                    .map(Peer::fromString)
                    .forEach(p -> {
                        routing.putIfAbsent(p.toString(), p);
                        registry.add(p);
                    });
        }
    }
}
