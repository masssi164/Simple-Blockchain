package de.flashyotter.blockchain_node.service;

import blockchain.core.consensus.Chain;
import blockchain.core.model.Block;
import blockchain.core.model.TxOutput;
import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.storage.BlockStore;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final Chain chain;
    private final NodeProperties props;
    private final BlockStore store;
    private final ObjectMapper mapper;

    public record Snapshot(int height,
                           Block tip,
                           Map<String, TxOutput> utxo,
                           Map<String, Integer> coinbase) {}

    @Scheduled(fixedDelayString = "#{@nodeProperties.snapshotIntervalSec * 1000}")
    void snapshotTask() {
        try {
            writeSnapshot();
            pruneBlocks();
        } catch (Exception e) {
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

        IOException last = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            Path tmp = Files.createTempFile(dir, file.getFileName().toString(), ".tmp");
            try {
                mapper.writeValue(tmp.toFile(), snap);
                Files.move(tmp, file,
                        java.nio.file.StandardCopyOption.ATOMIC_MOVE,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                log.info("Wrote snapshot {}", file.getFileName());
                return;
            } catch (IOException e) {
                last = e;
                try {
                    Files.deleteIfExists(tmp);
                } catch (IOException ex) {
                    // ignore cleanup failure
                }
                if (attempt < 3) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        if (last != null) throw last;
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

