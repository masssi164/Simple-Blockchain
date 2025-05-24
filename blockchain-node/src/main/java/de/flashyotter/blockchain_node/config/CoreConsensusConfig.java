// blockchain-node/src/main/java/de/flashyotter/blockchain_node/config/CoreConsensusConfig.java
package de.flashyotter.blockchain_node.config;

import blockchain.core.consensus.Chain;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Exposes a singleton {@link Chain} so that SyncService, MiningService,
 * NodeService … can @Autowired it.
 */
@Configuration
public class CoreConsensusConfig {

    @Bean
    public Chain chain() {
        return new Chain();
    }
}
