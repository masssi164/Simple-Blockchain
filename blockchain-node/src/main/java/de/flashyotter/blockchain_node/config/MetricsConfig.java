package de.flashyotter.blockchain_node.config;

import blockchain.core.consensus.Chain;
import de.flashyotter.blockchain_node.service.MempoolService;
import de.flashyotter.blockchain_node.service.PeerRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
public class MetricsConfig {

    private final MeterRegistry registry;
    private final Chain chain;
    private final MempoolService mempool;
    private final PeerRegistry peerRegistry;

    /** Time it takes to broadcast a block to all peers. */
    public static final String BLOCK_BROADCAST_TIME = "node_block_broadcast_time";
    /** Number of connected peers. */
    public static final String PEER_COUNT = "node_peer_count";
    /** Propagation delay from block timestamp to reception. */
    public static final String BLOCK_PROPAGATION_DELAY = "block_propagation_delay";
    /** Time spent writing a snapshot to disk. */
    public static final String SNAPSHOT_DURATION = "snapshot_duration";
    /** Count of successful UTXO snapshots. */
    public static final String SNAPSHOT_SUCCESS = "snapshot_success_total";
    /** Count of failed UTXO snapshots. */
    public static final String SNAPSHOT_FAILURE = "snapshot_failure_total";

    @PostConstruct
    void init() {
        Gauge.builder("node_block_height", chain, c -> c.getLatest().getHeight())
             .description("Current chain height")
             .register(registry);

        Gauge.builder("node_mempool_size", mempool, MempoolService::size)
             .description("Current number of pending transactions")
             .register(registry);

        Gauge.builder(PEER_COUNT, peerRegistry, r -> r.all().size())
             .description("Current number of known peers")
             .register(registry);

        Timer.builder(BLOCK_BROADCAST_TIME)
             .description("Time to broadcast a block to all peers")
             .register(registry);

        Timer.builder(BLOCK_PROPAGATION_DELAY)
             .description("Delay between block timestamp and reception")
             .register(registry);

        Timer.builder(SNAPSHOT_DURATION)
             .description("Time spent writing a snapshot")
             .register(registry);

        Counter.builder(SNAPSHOT_SUCCESS)
               .description("Number of successful snapshots")
               .register(registry);

        Counter.builder(SNAPSHOT_FAILURE)
               .description("Number of failed snapshots")
               .register(registry);
    }
}
