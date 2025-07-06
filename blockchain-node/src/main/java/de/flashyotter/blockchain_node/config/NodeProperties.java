package de.flashyotter.blockchain_node.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
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

    /** Base directory for node data like the ID file. */
    private String dataPath = "data";

    /**
     * P2P transport mode. Either "legacy" (WebSocket), "libp2p" or "dual" for
     * both. Defaults to legacy to keep backward compatibility.
     */
    private String p2pMode = "legacy";

    /** Maximum number of transactions kept in the mempool */
    @Value("${mempool.maxSize:1000}")
    private int mempoolMaxSize = 1000;

    /** HTTP/WebSocket server port */
    @Value("${server.port:0}")
    private int port;

    /** Stable node identifier persisted in dataPath/nodeId */
    private String id;
    
    /**
     * Password for encrypting/decrypting the PKCS12 keystore.
     */
    private String walletPassword;

    @PostConstruct
    private void initId() throws IOException {
        if (id != null && !id.isBlank()) return;
        Path path = Path.of(dataPath, "nodeId");
        if (Files.exists(path)) {
            id = Files.readString(path).trim();
        } else {
            Files.createDirectories(path.getParent());
            id = java.util.UUID.randomUUID().toString();
            Files.writeString(path, id);
        }
    }
}
