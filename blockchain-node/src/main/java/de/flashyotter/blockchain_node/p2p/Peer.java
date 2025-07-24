package de.flashyotter.blockchain_node.p2p;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Value object representing another node in the network. */
@Data
@AllArgsConstructor
public class Peer {
    private final String host;
    /** HTTP/WebSocket API port */
    private final int    restPort;
    /** TCP port for libp2p connections */
    private final int    libp2pPort;
    /** Optional libp2p peer ID */
    private final String id;

    public Peer(String host, int restPort) {
        this(host, restPort, 0, null);
    }

    public Peer(String host, int restPort, int libp2pPort) {
        this(host, restPort, libp2pPort, null);
    }


    /** Multiaddr for libp2p connections. */
    public String multiAddr() {
        String prefix;
        if (host.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
            prefix = "/ip4/";
        } else if (host.contains(":")) {
            prefix = "/ip6/";
        } else {
            // default to IPv4 DNS name since compose services resolve to IPv4
            prefix = "/dns4/";
        }
        String base = prefix + host + "/tcp/" + libp2pPort;
        return id == null ? base : base + "/p2p/" + id;
    }

    /** Convert canonical “host:port” string back into a {@link Peer}. */
    public static Peer fromString(String s) {
        String[] parts = s.split(":");
        if (parts.length != 2)
            throw new IllegalArgumentException("host:port expected");
        return new Peer(parts[0], Integer.parseInt(parts[1]));
    }

    /** Parse either host:port or multiaddress notation. */
    public static Peer parse(String s) {
        if (s.startsWith("/")) {
            String[] tokens = s.split("/");
            String host = tokens.length > 2 ? tokens[2] : "";
            int idx = java.util.Arrays.asList(tokens).indexOf("tcp");
            int port = idx > 0 && idx + 1 < tokens.length ? Integer.parseInt(tokens[idx + 1]) : 0;
            String id = null;
            for (int i = idx + 2; i < tokens.length - 1; i++) {
                if ("p2p".equals(tokens[i])) { id = tokens[i + 1]; break; }
            }
            return new Peer(host, 0, port, id);
        }

        String[] parts = s.split(":");
        if (parts.length != 2)
            throw new IllegalArgumentException("host:port expected");
        return new Peer(parts[0], 0, Integer.parseInt(parts[1]));
    }

    @Override
    public String toString() {
        return host + ':' + restPort;
    }
}
