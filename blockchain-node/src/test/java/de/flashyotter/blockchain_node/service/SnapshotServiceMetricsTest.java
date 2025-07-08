package de.flashyotter.blockchain_node.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;

import blockchain.core.consensus.Chain;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.storage.BlockStore;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class SnapshotServiceMetricsTest {

    @TempDir
    Path temp;

    private SimpleMeterRegistry registry;
    private SnapshotService svc;
    private Chain chain;
    private NodeProperties props;

    @BeforeEach
    void setUp() {
        props = new NodeProperties();
        props.setDataPath(temp.toString());

        chain = mock(Chain.class);
        Block genesis = new Block(0, "g", List.of(new Transaction()), 0);
        when(chain.getLatest()).thenReturn(genesis);
        when(chain.getUtxoSnapshot()).thenReturn(Map.of());
        when(chain.getCoinbaseHeightSnapshot()).thenReturn(Map.of());

        BlockStore store = mock(BlockStore.class);
        registry = new SimpleMeterRegistry();
        svc = new SnapshotService(chain, props, store, new ObjectMapper(), registry);
    }

    @Test
    void successIncrementsCounter() {
        svc.snapshotTask();
        assertEquals(1, registry.get("snapshot_success_total").counter().count());
        assertEquals(1, registry.get("snapshot_duration").timer().count());
    }

    @Test
    void failureIncrementsCounter() throws Exception {
        ObjectMapper failingMapper = org.mockito.Mockito.mock(ObjectMapper.class);
        doThrow(new IOException()).when(failingMapper)
                .writeValue(org.mockito.ArgumentMatchers.any(java.io.File.class), org.mockito.ArgumentMatchers.any());
        SnapshotService failing = new SnapshotService(chain, props, mock(BlockStore.class), failingMapper, registry);
        failing.snapshotTask();
        assertEquals(1, registry.get("snapshot_failure_total").counter().count());
        assertEquals(1, registry.get("snapshot_duration").timer().count());
    }
}
