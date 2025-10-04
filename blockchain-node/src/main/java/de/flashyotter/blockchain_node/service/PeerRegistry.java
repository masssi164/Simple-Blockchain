package de.flashyotter.blockchain_node.service;

import de.flashyotter.blockchain_node.p2p.Peer;
import de.flashyotter.blockchain_node.config.NodeProperties;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe peer set shared by all networking services.
 * Duplicates are ignored.  Callers may mutate through returned view.
 */
@Component
public class PeerRegistry {

    private final NodeProperties props;
    private final Set<Peer> peers = ConcurrentHashMap.newKeySet();
    private final java.util.concurrent.BlockingQueue<Peer> pending;

    public PeerRegistry(NodeProperties props) {
        this.props = props;
        this.pending = new java.util.concurrent.LinkedBlockingQueue<>(props.getPendingQueueLimit());
    }

    public Set<Peer> all() {
         return peers; 
    }

    /**
     * Add a peer to the registry.
     *
     * @return {@code true} if the peer was not known yet
     */
    public boolean add(Peer p) {
        synchronized (peers) {
            Peer existing = peers.stream()
                    .filter(candidate -> samePeer(candidate, p))
                    .findFirst()
                    .orElse(null);

            if (existing != null) {
                Peer merged = merge(existing, p);
                if (!existing.equals(merged)) {
                    peers.remove(existing);
                    peers.add(merged);
                    pending.remove(existing);
                    if (pending.remainingCapacity() > 0) {
                        pending.add(merged);
                    }
                }
                return false;
            }

            boolean fresh = peers.add(p);
            if (fresh && pending.remainingCapacity() > 0) {
                pending.add(p);
            }
            return fresh;
        }
    }

    private boolean samePeer(Peer a, Peer b) {
        if (a.getId() != null && b.getId() != null) {
            return a.getId().equals(b.getId());
        }
        return a.getHost().equals(b.getHost()) && a.getLibp2pPort() == b.getLibp2pPort();
    }

    private Peer merge(Peer existing, Peer incoming) {
        String host = incoming.getHost() != null && !incoming.getHost().isBlank()
                ? incoming.getHost()
                : existing.getHost();
        int restPort = incoming.getRestPort() > 0 ? incoming.getRestPort() : existing.getRestPort();
        int libp2pPort = incoming.getLibp2pPort() > 0 ? incoming.getLibp2pPort() : existing.getLibp2pPort();
        String id = incoming.getId() != null && !incoming.getId().isBlank()
                ? incoming.getId()
                : existing.getId();
        return new Peer(host, restPort, libp2pPort, id);
    }

    public java.util.concurrent.BlockingQueue<Peer> pending() { return pending; }

    public void addAll(Iterable<Peer> newPeers) {
        newPeers.forEach(this::add);
    }
}
