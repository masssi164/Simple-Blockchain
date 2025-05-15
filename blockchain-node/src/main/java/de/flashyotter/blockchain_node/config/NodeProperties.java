package de.flashyotter.blockchain_node.config;

import java.net.URI;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties("node")
@Data
public class NodeProperties {

    public enum Role { FULL, MINER, LIGHT }

    private Role      role = Role.FULL;

    private String    host      = "localhost";
    private List<URI> bootstrap = List.of();
    private long      syncIntervalMs = 30_000;

    /* ---- MINER ---- */
    private Mining mining = new Mining();

    @Data 
    public static class Mining {
        private boolean enabled     = false;
        private long    intervalMs  = 10_000;
    }

    /* ---- LIGHT ---- */
    private Storage storage = new Storage();

    @Data 
    public static class Storage {
        private int pruneAfterBlocks = 2_016;
    }

    /* Helper */
    public boolean isMiner()  { 
        return role == Role.MINER; 
    }
    public boolean isLight()  {
        return role == Role.LIGHT; 
    }
    public boolean isFull()   { 
        return role == Role.FULL || isMiner(); 
    }
}
