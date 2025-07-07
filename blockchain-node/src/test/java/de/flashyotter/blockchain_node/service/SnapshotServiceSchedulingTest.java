package de.flashyotter.blockchain_node.service;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;

import com.fasterxml.jackson.databind.ObjectMapper;

import blockchain.core.consensus.Chain;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.storage.BlockStore;

class SnapshotServiceSchedulingTest {

    @TempDir
    Path temp;

    private AnnotationConfigApplicationContext ctx;
    private SnapshotService spySvc;

    @BeforeEach
    void setUp() {
        NodeProperties props = new NodeProperties();
        props.setDataPath(temp.toString());
        props.setSnapshotIntervalSec(1);

        Chain chain = mock(Chain.class);
        Block genesis = new Block(0, "g", List.of(new Transaction()), 0);
        org.mockito.Mockito.when(chain.getLatest()).thenReturn(genesis);
        org.mockito.Mockito.when(chain.getUtxoSnapshot()).thenReturn(Map.of());
        org.mockito.Mockito.when(chain.getCoinbaseHeightSnapshot()).thenReturn(Map.of());

        BlockStore store = mock(BlockStore.class);
        ObjectMapper mapper = new ObjectMapper();

        SnapshotService target = new SnapshotService(chain, props, store, mapper);
        spySvc = spy(target);

        ctx = new AnnotationConfigApplicationContext();
        ctx.registerBean(NodeProperties.class, () -> props);
        ctx.registerBean(Chain.class, () -> chain);
        ctx.registerBean(BlockStore.class, () -> store);
        ctx.registerBean(ObjectMapper.class, () -> mapper);
        ctx.registerBean(SnapshotService.class, () -> spySvc);
        ctx.registerBean(ScheduledAnnotationBeanPostProcessor.class);
        ctx.refresh();
    }

    @AfterEach
    void tearDown() {
        ctx.close();
    }

    @Test
    void taskStopsAfterContextShutdown() throws Exception {
        await().atMost(Duration.ofSeconds(2))
              .untilAsserted(() -> verify(spySvc, atLeastOnce()).snapshotTask());

        long before = mockingDetails(spySvc).getInvocations().stream()
                .filter(inv -> inv.getMethod().getName().equals("snapshotTask"))
                .count();

        ctx.close();

        Thread.sleep(1500);

        long after = mockingDetails(spySvc).getInvocations().stream()
                .filter(inv -> inv.getMethod().getName().equals("snapshotTask"))
                .count();

        assertEquals(before, after, "scheduled task should stop after context close");
    }
}

