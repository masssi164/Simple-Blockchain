package de.flashyotter.blockchain_node.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import blockchain.core.consensus.Chain;
import blockchain.core.model.Block;

class NodeServicePageTest {

    private Chain chain;
    private NodeService svc;

    @BeforeEach
    void setUp() {
        chain = Mockito.mock(Chain.class);
        svc = new NodeService(chain,
                Mockito.mock(MempoolService.class),
                Mockito.mock(MiningService.class),
                Mockito.mock(P2PBroadcastService.class),
                Mockito.mock(de.flashyotter.blockchain_node.storage.BlockStore.class),
                new io.micrometer.core.instrument.simple.SimpleMeterRegistry());
    }

    @Test
    void returnsPagedBlocks() {
        List<Block> all = List.of(
            new Block(0, "g", List.of(new blockchain.core.model.Transaction()), 0),
            new Block(1, "a", List.of(new blockchain.core.model.Transaction()), 0),
            new Block(2, "b", List.of(new blockchain.core.model.Transaction()), 0),
            new Block(3, "c", List.of(new blockchain.core.model.Transaction()), 0),
            new Block(4, "d", List.of(new blockchain.core.model.Transaction()), 0),
            new Block(5, "e", List.of(new blockchain.core.model.Transaction()), 0),
            new Block(6, "f", List.of(new blockchain.core.model.Transaction()), 0)
        );
        when(chain.getBlocks()).thenReturn(all);

        List<Block> page0 = svc.blockPage(0, 5);
        assertEquals(5, page0.size());
        assertEquals(2, page0.get(0).getHeight());

        List<Block> page1 = svc.blockPage(1, 5);
        assertEquals(2, page1.size());
        assertEquals(0, page1.get(0).getHeight());
    }
}
