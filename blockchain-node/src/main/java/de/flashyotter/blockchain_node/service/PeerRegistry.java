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
        boolean fresh = peers.add(p);
        if (fresh) pending.offer(p);
        return fresh;
    }

    public java.util.concurrent.BlockingQueue<Peer> pending() { return pending; }

    public void addAll(Iterable<Peer> newPeers) {
        newPeers.forEach(this::add);
    }
}
