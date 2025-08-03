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
        Method candidate = null;
        for (Method m : KademliaRoutingTable.class.getDeclaredMethods()) {
            if (m.getName().equals("add") && m.getParameterCount() == 1 && m.getReturnType() != boolean.class) {
                m.setAccessible(true);
                candidate = m;
                break;
            }
        }
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
            ADD_METHOD.invoke(table, peer);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to add peer", e);
        }
    }

    @Override
    public void removePeer(Peer peer) {
        if (!table.evict(peer)) {
            throw new IllegalStateException("Failed to remove peer");
        }
    }
}
