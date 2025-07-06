package de.flashyotter.blockchain_node.config;

import blockchain.core.consensus.Chain;
import de.flashyotter.blockchain_node.service.MempoolService;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
public class MetricsConfig {

    private final MeterRegistry registry;
    private final Chain chain;
    private final MempoolService mempool;

    @PostConstruct
    void init() {
        Gauge.builder("node_block_height", chain, c -> c.getLatest().getHeight())
             .description("Current chain height")
             .register(registry);

        Gauge.builder("node_mempool_size", mempool, MempoolService::size)
             .description("Current number of pending transactions")
             .register(registry);
    }
}
