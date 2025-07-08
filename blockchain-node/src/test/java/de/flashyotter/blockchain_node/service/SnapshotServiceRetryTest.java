package de.flashyotter.blockchain_node.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;

import blockchain.core.consensus.Chain;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.storage.BlockStore;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class SnapshotServiceRetryTest {

    @TempDir
    Path temp;

    @Test
    void retryOnFirstFailure() throws Exception {
        NodeProperties props = new NodeProperties();
        props.setDataPath(temp.toString());

        Chain chain = Mockito.mock(Chain.class);
        Block genesis = new Block(0, "g", List.of(new Transaction()), 0);
        Mockito.when(chain.getLatest()).thenReturn(genesis);
        Mockito.when(chain.getUtxoSnapshot()).thenReturn(Map.of());
        Mockito.when(chain.getCoinbaseHeightSnapshot()).thenReturn(Map.of());
        Mockito.when(chain.pruneOldBlocks(Mockito.anyInt())).thenReturn(List.of());

        BlockStore store = Mockito.mock(BlockStore.class);

        ObjectMapper mapper = Mockito.spy(new ObjectMapper());
        AtomicInteger calls = new AtomicInteger();
        Mockito.doAnswer(inv -> {
            if (calls.getAndIncrement() == 0) {
                throw new IOException("boom");
            }
            return inv.callRealMethod();
        }).when(mapper).writeValue(Mockito.any(java.io.OutputStream.class), Mockito.any());

        SimpleMeterRegistry metrics = new SimpleMeterRegistry();
        SnapshotService svc = new SnapshotService(chain, props, store, mapper, metrics);
        svc.snapshotTask();

        assertEquals(2, calls.get(), "should retry once");
        long files = Files.list(temp.resolve("snapshots"))
                          .filter(p -> p.toString().endsWith(".gz"))
                          .count();
        assertEquals(1, files, "snapshot file created");
    }
}
