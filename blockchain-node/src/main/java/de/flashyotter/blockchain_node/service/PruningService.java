package de.flashyotter.blockchain_node.service;

import blockchain.core.consensus.Chain;
import blockchain.core.model.Block;
import de.flashyotter.blockchain_node.storage.BlockStore;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/** Periodically prunes old fork data from the in-memory chain. */
@Service
@RequiredArgsConstructor
@Slf4j
public class PruningService {
    private final Chain chain;
    private final BlockStore store;

    private static final int KEEP_BLOCKS = 1000;

    @PostConstruct
    void start() {
        Schedulers.boundedElastic().schedule(() -> {
            while (true) {
                try {
                    Thread.sleep(60_000);
                    List<Block> removed = chain.pruneOldBlocks(KEEP_BLOCKS);
                    for (Block b : removed) {
                        store.save(b);
                    }
                    if (!removed.isEmpty()) {
                        log.info("Pruned {} blocks from DAG", removed.size());
                    }
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
    }
}
