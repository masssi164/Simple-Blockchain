package de.flashyotter.blockchain_node.service;

import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.dto.NodesDto;
import de.flashyotter.blockchain_node.p2p.Peer;
import org.apache.tuweni.kademlia.KademliaRoutingTable;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Thin wrapper around {@link KademliaRoutingTable} keeping {@link PeerRegistry}
 * in sync.
 */
@Service
@RequiredArgsConstructor
public class KademliaService {

    private final KademliaRoutingTable<Peer> table;
    private final PeerRegistry              registry;
    private final NodeProperties            props;

    /** Add a peer to the routing table and registry. */
    public void store(Peer peer) {
        try {
            java.lang.reflect.Method target = null;
            for (var m : table.getClass().getMethods()) {
                if (m.getName().equals("add") && m.getParameterCount() == 1 && m.getReturnType() != boolean.class) {
                    target = m; break;
                }
            }
            if (target != null) {
                target.invoke(table, peer);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        registry.add(peer);
    }

    /** Merge peers from a {@link NodesDto} response. */
    public void merge(NodesDto dto) {
        dto.peers().stream().map(Peer::fromString).forEach(this::store);
    }

    /** Return up to {@code limit} peers closest to the given node ID. */
    public List<Peer> closest(String nodeId, int limit) {
        return table.nearest(nodeId.getBytes(StandardCharsets.UTF_8), limit);
    }

    /** ID of this node used for routing table distance calculations. */
    public String selfId() {
        return props.getId();
    }

    /** Number of peers currently stored in the routing table */
    public int peerCount() {
        return registry.all().size();
    }
}
