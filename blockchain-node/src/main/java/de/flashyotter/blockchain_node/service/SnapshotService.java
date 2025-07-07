package de.flashyotter.blockchain_node.service;

import blockchain.core.consensus.Chain;
import blockchain.core.model.Block;
import blockchain.core.model.TxOutput;
import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.storage.BlockStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.flashyotter.blockchain_node.config.MetricsConfig;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Periodically writes the UTXO set to disk and prunes old blocks. */
@Service
@RequiredArgsConstructor
@Slf4j
public class SnapshotService {
    private final Chain          chain;
    private final NodeProperties props;
    private final BlockStore     store;
    private final ObjectMapper   mapper;
    private final MeterRegistry  metrics;

    public record Snapshot(int height,
                           Block tip,
                           Map<String, TxOutput> utxo,
                           Map<String, Integer> coinbase) {}

    @Scheduled(fixedDelayString = "#{@nodeProperties.snapshotIntervalSec * 1000}")
    void snapshotTask() {
        try {
            writeSnapshot();
            pruneBlocks();
            metrics.counter(MetricsConfig.SNAPSHOT_SUCCESS).increment();
            log.info("Snapshot completed successfully");
        } catch (Exception e) {
            metrics.counter(MetricsConfig.SNAPSHOT_FAILURE).increment();
            log.warn("Snapshot failed", e);
        }
    }

    private void writeSnapshot() throws IOException {
        Path dir = Path.of(props.getDataPath(), "snapshots");
        Files.createDirectories(dir);

        Block tip = chain.getLatest();
        Snapshot snap = new Snapshot(
                tip.getHeight(),
                tip,
                chain.getUtxoSnapshot(),
                chain.getCoinbaseHeightSnapshot()
        );
        Path file = dir.resolve(String.format("%07d.json", snap.height()));
        mapper.writeValue(file.toFile(), snap);
        log.info("Wrote snapshot {}", file.getFileName());
    }

    private void pruneBlocks() {
        List<Block> removed = chain.pruneOldBlocks(props.getHistoryDepth());
        for (Block b : removed) {
            store.save(b);
        }
        if (!removed.isEmpty()) {
            log.info("Pruned {} blocks from DAG", removed.size());
        }
    }

    /** Loads the most recent snapshot if present. */
    public Snapshot loadLatest() {
        Path dir = Path.of(props.getDataPath(), "snapshots");
        if (!Files.isDirectory(dir)) return null;
        try {
            return Files.list(dir)
                    .filter(p -> p.toString().endsWith(".json"))
                    .max(Comparator.naturalOrder())
                    .map(p -> {
                        try {
                            return mapper.readValue(p.toFile(), Snapshot.class);
                        } catch (IOException e) {
                            log.warn("Failed to read snapshot {}", p, e);
                            return null;
                        }
                    }).orElse(null);
        } catch (IOException e) {
            return null;
        }
    }
}

