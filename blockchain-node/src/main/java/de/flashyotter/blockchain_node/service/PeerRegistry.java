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

    public void add(Peer p) { 
        peers.add(p); 
    }

    public void addAll(Iterable<Peer> newPeers) {
         newPeers.forEach(peers::add); 
        }
}
