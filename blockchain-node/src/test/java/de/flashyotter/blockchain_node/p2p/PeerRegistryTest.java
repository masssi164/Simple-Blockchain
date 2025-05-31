package de.flashyotter.blockchain_node.p2p;
import de.flashyotter.blockchain_node.service.PeerRegistry;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PeerRegistryTest {

    @Test
    void addAndAllWorks() {
        PeerRegistry reg = new PeerRegistry();
        Peer a = new Peer("a", 1);
        Peer b = new Peer("b", 2);

        reg.add(a);
        reg.add(b);
        // duplicate
        reg.add(new Peer("a", 1));

        assertEquals(2, reg.all().size());
        assertTrue(reg.all().contains(a));
        assertTrue(reg.all().contains(b));
    }

    @Test
    void addAllWorks() {
        PeerRegistry reg = new PeerRegistry();
        var list = java.util.List.of(new Peer("x", 9), new Peer("y", 8));
        reg.addAll(list);
        assertEquals(2, reg.all().size());
    }
}
