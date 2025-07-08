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
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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

    private static final String MANIFEST = "manifest.txt";

    public record Snapshot(int height,
                           Block tip,
                           Map<String, TxOutput> utxo,
                           Map<String, Integer> coinbase) {}

    @Scheduled(fixedDelayString = "#{@nodeProperties.snapshotIntervalSec * 1000}")
    void snapshotTask() {
        io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start(metrics);
        try {
            writeSnapshot();
            pruneBlocks();
            compactSnapshots();
            metrics.counter(MetricsConfig.SNAPSHOT_SUCCESS).increment();
            log.info("Snapshot completed successfully");
        } catch (Exception e) {
            metrics.counter(MetricsConfig.SNAPSHOT_FAILURE).increment();
            log.warn("Snapshot failed", e);
        } finally {
            sample.stop(metrics.timer(MetricsConfig.SNAPSHOT_DURATION));
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
        String name = String.format("%07d.json.gz", snap.height());
        Path file = dir.resolve(name);

        IOException last = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            Path tmp = Files.createTempFile(dir, file.getFileName().toString(), ".tmp");
            try (OutputStream os = new GZIPOutputStream(Files.newOutputStream(tmp, StandardOpenOption.WRITE))) {
                mapper.writeValue(os, snap);
                os.flush();
                Files.move(tmp, file,
                        java.nio.file.StandardCopyOption.ATOMIC_MOVE,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                updateManifest(dir, name);
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

    private void updateManifest(Path dir, String name) throws IOException {
        Path manifest = dir.resolve(MANIFEST);
        List<String> lines = Files.exists(manifest) ? Files.readAllLines(manifest) : new ArrayList<>();
        lines.add(name);
        Files.write(manifest, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void compactSnapshots() {
        Path dir = Path.of(props.getDataPath(), "snapshots");
        if (!Files.isDirectory(dir)) return;
        try {
            List<Path> snaps = Files.list(dir)
                    .filter(p -> p.toString().endsWith(".json.gz"))
                    .sorted()
                    .toList();
            int keep = 5;
            if (snaps.size() <= keep) return;
            for (int i = 0; i < snaps.size() - keep; i++) {
                Files.deleteIfExists(snaps.get(i));
            }
            Path manifest = dir.resolve(MANIFEST);
            List<String> names = snaps.subList(snaps.size() - keep, snaps.size())
                    .stream().map(p -> p.getFileName().toString()).toList();
            Files.write(manifest, names, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            log.warn("Snapshot compaction failed", e);
        }
    }

    /** Loads the most recent snapshot if present. */
    public Snapshot loadLatest() {
        Path dir = Path.of(props.getDataPath(), "snapshots");
        if (!Files.isDirectory(dir)) return null;
        try {
            Path manifest = dir.resolve(MANIFEST);
            Path latest = null;
            if (Files.exists(manifest)) {
                List<String> lines = Files.readAllLines(manifest);
                if (!lines.isEmpty()) {
                    latest = dir.resolve(lines.get(lines.size() - 1));
                }
            }
            if (latest == null || !Files.exists(latest)) {
                latest = Files.list(dir)
                        .filter(p -> p.toString().endsWith(".json") || p.toString().endsWith(".json.gz"))
                        .max(Comparator.naturalOrder())
                        .orElse(null);
            }
            if (latest == null) return null;
            return readSnapshot(latest);
        } catch (IOException e) {
            return null;
        }
    }

    private Snapshot readSnapshot(Path p) throws IOException {
        if (p.toString().endsWith(".gz")) {
            try (InputStream is = new GZIPInputStream(Files.newInputStream(p))) {
                return mapper.readValue(is, Snapshot.class);
            }
        }
        return mapper.readValue(p.toFile(), Snapshot.class);
    }
}

