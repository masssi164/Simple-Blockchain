package de.flashyotter.blockchain_node.p2p;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Value object representing another node in the network. */
@Data
@AllArgsConstructor
public class Peer {
    private final String host;
    private final int    port;

    /** WebSocket URL of this peer’s raw-JSON P2P endpoint. */
    public String wsUrl() {
        // was "/p2p" but our server registers on "/ws"
        return "ws://" + host + ':' + port + "/ws";
    }

    /** Convert canonical “host:port” string back into a {@link Peer}. */
    public static Peer fromString(String s) {
        String[] parts = s.split(":");
        if (parts.length != 2)
            throw new IllegalArgumentException("host:port expected");
        return new Peer(parts[0], Integer.parseInt(parts[1]));
    }

    @Override
    public String toString() {
        return host + ':' + port;
    }
}
