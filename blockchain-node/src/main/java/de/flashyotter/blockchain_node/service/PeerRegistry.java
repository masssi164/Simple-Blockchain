package de.flashyotter.blockchain_node.service;

import de.flashyotter.blockchain_node.p2p.Peer;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe peer set shared by all networking services.
 * Duplicates are ignored.  Callers may mutate through returned view.
 */
@Component
public class PeerRegistry {

    private final Set<Peer> peers = ConcurrentHashMap.newKeySet();

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
        if (fresh) pending.add(p);          // mark for dial
        return fresh;
    }

    /* dial queue consumed by discovery loop */
    private final java.util.concurrent.BlockingQueue<Peer> pending =
            new java.util.concurrent.LinkedBlockingQueue<>();
    public java.util.concurrent.BlockingQueue<Peer> pending() { return pending; }

    public void addAll(Iterable<Peer> newPeers) {
         newPeers.forEach(peers::add); 
        }
}
