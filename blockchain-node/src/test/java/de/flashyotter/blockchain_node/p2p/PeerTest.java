package de.flashyotter.blockchain_node.p2p;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PeerTest {

    @Test
    void wsUrlFormatsCorrectly() {
        Peer p = new Peer("example.com", 4242);
        assertEquals("ws://example.com:4242/ws", p.wsUrl());
    }

    @Test
    void fromStringParsesValid() {
        Peer p = Peer.fromString("host:1234");
        assertEquals("host", p.getHost());
        assertEquals(1234, p.getPort());
    }

    @Test
    void fromStringThrowsOnBadInput() {
        assertThrows(IllegalArgumentException.class,
            () -> Peer.fromString("notvalid"));
    }
}
