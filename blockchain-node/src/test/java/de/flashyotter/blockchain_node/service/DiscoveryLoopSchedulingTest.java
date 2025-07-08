package de.flashyotter.blockchain_node.service;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;

import de.flashyotter.blockchain_node.p2p.Peer;
import de.flashyotter.blockchain_node.config.NodeProperties;

class DiscoveryLoopSchedulingTest {

    private AnnotationConfigApplicationContext ctx;
    private PeerRegistry reg;
    private KademliaService kademlia;

    @BeforeEach
    void setUp() {
        reg = new PeerRegistry();
        SyncService sync = mock(SyncService.class);
        kademlia = mock(KademliaService.class);
        de.flashyotter.blockchain_node.p2p.libp2p.Libp2pService libp2p = mock(de.flashyotter.blockchain_node.p2p.libp2p.Libp2pService.class);
        NodeProperties props = new NodeProperties();

        DiscoveryLoop target = new DiscoveryLoop(reg, sync, kademlia, libp2p, props);
        DiscoveryLoop spySvc = spy(target);

        ctx = new AnnotationConfigApplicationContext();
        ctx.registerBean(PeerRegistry.class, () -> reg);
        ctx.registerBean(SyncService.class, () -> sync);
        ctx.registerBean(KademliaService.class, () -> kademlia);
        ctx.registerBean(de.flashyotter.blockchain_node.p2p.libp2p.Libp2pService.class, () -> libp2p);
        ctx.registerBean(NodeProperties.class, () -> props);
        ctx.registerBean(DiscoveryLoop.class, () -> spySvc);
        ctx.registerBean(ScheduledAnnotationBeanPostProcessor.class);
        ctx.refresh();
    }

    @AfterEach
    void tearDown() {
        ctx.close();
    }

    @Test
    void pollingStopsAfterShutdown() throws Exception {
        Peer p = new Peer("h", 1);
        reg.pending().offer(p);

        await().atMost(Duration.ofSeconds(2))
              .untilAsserted(() -> verify(kademlia, atLeastOnce()).store(p));

        long before = mockingDetails(kademlia).getInvocations().stream()
                .filter(inv -> inv.getMethod().getName().equals("store"))
                .count();

        ctx.close();
        reg.pending().offer(new Peer("x", 1));
        Thread.sleep(1500);

        long after = mockingDetails(kademlia).getInvocations().stream()
                .filter(inv -> inv.getMethod().getName().equals("store"))
                .count();

        assertEquals(before, after, "discovery loop should stop after context close");
    }
}

