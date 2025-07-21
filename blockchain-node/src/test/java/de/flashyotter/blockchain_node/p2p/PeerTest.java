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
    void multiAddrUsesDnsForHostnames() {
        Peer p = new Peer("example.com", 0, 4001);
        assertEquals("/dns4/example.com/tcp/4001", p.multiAddr());
    }

    @Test
    void multiAddrUsesIp4ForNumericHosts() {
        Peer p = new Peer("1.2.3.4", 0, 4001);
        assertEquals("/ip4/1.2.3.4/tcp/4001", p.multiAddr());
    }

    @Test
    void multiAddrUsesIp6ForColonAddresses() {
        Peer p = new Peer("::1", 0, 4001);
        assertEquals("/ip6/::1/tcp/4001", p.multiAddr());
    }

    @Test
    void fromStringParsesValid() {
        Peer p = Peer.fromString("host:1234");
        assertEquals("host", p.getHost());
        assertEquals(1234, p.getRestPort());
    }

    @Test
    void fromStringThrowsOnBadInput() {
        assertThrows(IllegalArgumentException.class,
            () -> Peer.fromString("notvalid"));
    }
}
