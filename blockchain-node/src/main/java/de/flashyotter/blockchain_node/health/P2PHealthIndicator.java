package de.flashyotter.blockchain_node.health;

import de.flashyotter.blockchain_node.p2p.libp2p.Libp2pService;
import de.flashyotter.blockchain_node.service.NodeService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("p2p")
public class P2PHealthIndicator implements HealthIndicator {
    private final Libp2pService libp2p;
    private final NodeService node;

    public P2PHealthIndicator(Libp2pService libp2p, NodeService node) {
        this.libp2p = libp2p;
        this.node = node;
    }

    @Override
    public Health health() {
        boolean up = libp2p.peerCount() > 0 && node.latestBlock() != null;
        return up ? Health.up().build() : Health.down().build();
    }
}
