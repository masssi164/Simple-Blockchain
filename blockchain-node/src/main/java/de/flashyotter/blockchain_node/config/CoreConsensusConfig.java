// blockchain-node/src/main/java/de/flashyotter/blockchain_node/config/CoreConsensusConfig.java
package de.flashyotter.blockchain_node.config;

import blockchain.core.consensus.Chain;
import blockchain.core.exceptions.BlockchainException;
import blockchain.core.model.Block;
import de.flashyotter.blockchain_node.storage.BlockStore;
import de.flashyotter.blockchain_node.service.SnapshotService;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Exposes a singleton {@link Chain} so that SyncService, MiningService,
 * NodeService … can @Autowired it.
 */
@Configuration
public class CoreConsensusConfig {

    @Bean
    public Chain chain(BlockStore store, SnapshotService snapshots) {
        Chain chain = new Chain();

        java.util.List<Block> blocks = new ArrayList<>();
        for (Block b : store.loadAll()) {
            if (b.getHeight() > 0) blocks.add(b);
        }
        blocks.sort(Comparator.comparingInt(Block::getHeight));

        for (Block b : blocks) {
            try {
                chain.addBlock(b);
            } catch (BlockchainException e) {
                LoggerFactory.getLogger(CoreConsensusConfig.class)
                        .warn("Skipping invalid block {}: {}", b.getHashHex(), e.getMessage());
            }
        }

        SnapshotService.Snapshot snap = snapshots.loadLatest();
        if (snap != null && snap.height() == chain.getLatest().getHeight()) {
            chain.loadUtxoSnapshot(snap.utxo(), snap.coinbase());
        }

        return chain;
    }
}
