package de.flashyotter.blockchain_node.service;

import de.flashyotter.blockchain_node.p2p.Peer;
import org.apache.tuweni.kademlia.KademliaRoutingTable;

import java.lang.reflect.Method;

/**
 * {@link PeerStore} backed by a {@link KademliaRoutingTable}. The routing table
 * from the Tuweni library exposes mutation methods that are hidden by the
 * read-only {@code Set} interface, so reflection is used to invoke them.
 */
public class TablePeerStore implements PeerStore {
    private static final Method ADD_METHOD;
    static {
        Method preferred = null;
        Method fallback = null;
        for (Method m : KademliaRoutingTable.class.getDeclaredMethods()) {
            if (m.getName().equals("add") && m.getParameterCount() == 1) {
                // Prefer the variant returning the added peer to avoid the unmodifiable
                // Set.add implementation. Fallback to the boolean-returning method if
                // older library versions do not expose the former.
                m.setAccessible(true);
                if (m.getReturnType() == boolean.class) {
                    fallback = m;
                } else {
                    preferred = m;
                    break;
                }
            }
        }
        Method candidate = preferred != null ? preferred : fallback;
        if (candidate == null) {
            throw new ExceptionInInitializerError("KademliaRoutingTable.add method not found");
        }
        ADD_METHOD = candidate;
    }

    private final KademliaRoutingTable<Peer> table;

    public TablePeerStore(KademliaRoutingTable<Peer> table) {
        this.table = table;
    }

    @Override
    public void addPeer(Peer peer) {
        try {
            Object result = ADD_METHOD.invoke(table, peer);
            if (ADD_METHOD.getReturnType() == boolean.class && Boolean.FALSE.equals(result)) {
                throw new IllegalStateException("Failed to add peer", null);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to add peer", e);
        }
    }

    @Override
    public void removePeer(Peer peer) {
        if (!table.evict(peer)) {
            throw new IllegalStateException("Failed to remove peer", null);
        }
    }
}
