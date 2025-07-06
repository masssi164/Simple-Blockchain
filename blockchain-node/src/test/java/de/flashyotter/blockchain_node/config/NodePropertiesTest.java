package de.flashyotter.blockchain_node.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NodePropertiesTest {
    @Test
    void defaultP2pModeIsLegacy() {
        NodeProperties props = new NodeProperties();
        assertEquals("legacy", props.getP2pMode());
    }
}
