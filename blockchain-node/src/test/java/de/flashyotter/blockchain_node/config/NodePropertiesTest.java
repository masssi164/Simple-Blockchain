package de.flashyotter.blockchain_node.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NodePropertiesTest {

    @Test
    void defaultLibp2pPortIs4001() {
        NodeProperties props = new NodeProperties();
        assertEquals(4001, props.getLibp2pPort());
    }

    @Test
    void defaultMiningThreadsMatchesCpuCount() {
        NodeProperties props = new NodeProperties();
        assertEquals(Runtime.getRuntime().availableProcessors(),
                     props.getMiningThreads());
    }

    @Test
    void encryptionEnabledByDefault() {
        NodeProperties props = new NodeProperties();
        assertTrue(props.isLibp2pEncrypted());
    }
}
