package de.flashyotter.blockchain_node.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "node")
public class NodeProperties {
    private List<String> peers = List.of();
    
    /**
     * Returns the list of known peers.
     */
    public List<String> getKnownPeers() {
        return peers;
    }

    /** Base directory for node data like the ID file. */
    private String dataPath = "data";
    
    /**
     * The base URL for this node
     */
    private String baseUrl = "http://localhost:8080";
    
    /**
     * Returns the base URL for this node
     */
    public String getBaseUrl() {
        return baseUrl;
    }


    /** Maximum number of transactions kept in the mempool */
    @Value("${mempool.maxSize:1000}")
    private int mempoolMaxSize = 1000;

    /** libp2p TCP listen port */
    private int libp2pPort = 4001;

    /** Enable Noise encryption for libp2p connections */
    private boolean libp2pEncrypted = true;

    /** File storing the persistent libp2p private key */
    private String libp2pKeyPath = "libp2p.key";

    /** HTTP/WebSocket server port */
    @Value("${server.port:0}")
    private int port;

    /** Stable node identifier persisted in dataPath/nodeId */
    private String id;
    
    /**
     * Password for encrypting/decrypting the PKCS12 keystore.
     */
    private String walletPassword;

    /** Shared secret used to sign and verify JWT tokens. */
    private String jwtSecret;

    /** Number of worker threads used for mining */
    private int miningThreads = Runtime.getRuntime().availableProcessors();

    /** Interval for writing UTXO snapshots in seconds */
    private int snapshotIntervalSec = 300;

    /** How many recent blocks to keep in memory and on disk */
    private int historyDepth = 1000;

    /** Whether peers push new blocks immediately after mining */
    private boolean p2pPushEnabled = true;

    /** Timeout in milliseconds for block sync requests */
    private int syncTimeoutMs = 10000;

    @PostConstruct
    private void init() throws IOException {
        String peersEnv = System.getenv("NODE_PEERS");
        if ((peers == null || peers.isEmpty()) && peersEnv != null && !peersEnv.isBlank()) {
            peers = Arrays.asList(peersEnv.split(","));
        }

        if (!java.nio.file.Path.of(libp2pKeyPath).isAbsolute()) {
            libp2pKeyPath = java.nio.file.Path.of(dataPath, libp2pKeyPath).toString();
        }

        if (id != null && !id.isBlank()) return;
        Path path = Path.of(dataPath, "nodeId");
        if (Files.exists(path)) {
            id = Files.readString(path).trim();
        } else {
            Files.createDirectories(path.getParent());
            id = java.util.UUID.randomUUID().toString();
            Files.writeString(path, id);
        }

        if (walletPassword == null || walletPassword.isBlank()) {
            walletPassword = System.getenv("NODE_WALLET_PASSWORD");
        }
        if (jwtSecret == null || jwtSecret.isBlank()) {
            jwtSecret = System.getenv("NODE_JWT_SECRET");
        }

        if (walletPassword == null || walletPassword.isBlank()) {
            throw new IllegalStateException("NODE_WALLET_PASSWORD must be set");
        }
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException("NODE_JWT_SECRET must be set");
        }
    }
}
