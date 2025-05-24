package de.flashyotter.blockchain_node.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Binds <code>node.*</code> properties from <em>application.yml</em>.
 *
 * Example configuration:
 * <pre>
 * node:
 *   peers:
 *     - "localhost:8081"
 *     - "example.com:9090"
 * </pre>
 */
@Data
@Component
@ConfigurationProperties(prefix = "node")
public class NodeProperties {

    /** List of <host>:<port> peers to connect to at start-up. */
    private List<String> peers = List.of();
}
