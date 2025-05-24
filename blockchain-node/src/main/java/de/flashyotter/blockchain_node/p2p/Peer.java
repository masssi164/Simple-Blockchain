package de.flashyotter.blockchain_node.p2p;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Simple peer descriptor (host + port) plus helper to build the WS URL.
 */
@Data
@AllArgsConstructor
public class Peer {

    private String host;
    private int    port;

    /** Returns the full WebSocket URL expected by our Node. */
    public String wsUrl() {
        return "ws://" + host + ':' + port + "/ws";
    }
}
