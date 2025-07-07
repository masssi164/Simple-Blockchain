package de.flashyotter.blockchain_node.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import static org.junit.jupiter.api.Assertions.*;

class NodePropertiesBindingIT {

    @TestConfiguration
    @EnableConfigurationProperties(NodeProperties.class)
    static class Config {}

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(Config.class)
            .withPropertyValues(
                    "node.p2p-mode=libp2p",
                    "node.libp2p-encrypted=true",
                    "node.mining-threads=8",
                    "node.snapshot-interval-sec=42",
                    "node.history-depth=123"
            );

    @Test
    void valuesFromPropertiesAreBound() {
        contextRunner.run(context -> {
            NodeProperties props = context.getBean(NodeProperties.class);
            assertEquals("libp2p", props.getP2pMode());
            assertTrue(props.isLibp2pEncrypted());
            assertEquals(8, props.getMiningThreads());
            assertEquals(42, props.getSnapshotIntervalSec());
            assertEquals(123, props.getHistoryDepth());
        });
    }
}
