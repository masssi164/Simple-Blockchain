package de.flashyotter.blockchain_node.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "node")
public class NodeProperties {
    private List<String> peers = List.of();

    /** Stable node identifier persisted in data/nodeId */
    private String id;

    /** Publicly reachable host (detected automatically). */
    private String publicHost;
    
    /**
     * Password for encrypting/decrypting the PKCS12 keystore.
     */
    private String walletPassword;

    /** Optional helper for auto-detecting the public IP. */
    @Autowired(required = false)
    private de.flashyotter.blockchain_node.service.PublicIpService ipService;

    @PostConstruct
    private void initId() throws IOException {
        if (id != null && !id.isBlank()) return;
        Path path = Path.of("data", "nodeId");
        if (Files.exists(path)) {
            id = Files.readString(path).trim();
        } else {
            Files.createDirectories(path.getParent());
            id = java.util.UUID.randomUUID().toString();
            Files.writeString(path, id);
        }
    }

    /**
     * Determine the node's outward-facing address on startup.
     */
    @PostConstruct
    void initAddress() {
        if (ipService != null) {
            String ip = ipService.fetchPublicIp();
            if (ip != null && !ip.isBlank()) {
                publicHost = ip;
            }
        }
    }
}
