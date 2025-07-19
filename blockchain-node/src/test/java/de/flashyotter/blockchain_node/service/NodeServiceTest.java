package de.flashyotter.blockchain_node.service;

import static blockchain.core.serialization.JsonUtils.toJson;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import blockchain.core.consensus.Chain;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxOutput;
import de.flashyotter.blockchain_node.dto.NewBlockDto;
import de.flashyotter.blockchain_node.dto.NewTxDto;
import de.flashyotter.blockchain_node.storage.BlockStore;
import de.flashyotter.blockchain_node.config.NodeProperties;
import reactor.core.publisher.Mono;

class NodeServiceTest {

    private Chain chain;
    private MempoolService mempool;
    private MiningService mining;
    private P2PBroadcastService broadcaster; // ✔ korrekte Typ-Deklaration
    private BlockStore store;
    private io.micrometer.core.instrument.simple.SimpleMeterRegistry metrics;
    private NodeService svc;

    @BeforeEach
    void setUp() {
        chain       = mock(Chain.class);
        mempool     = mock(MempoolService.class);
        mining      = mock(MiningService.class);
        broadcaster = mock(P2PBroadcastService.class);
        store       = mock(BlockStore.class);
        metrics     = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();

        when(chain.getBlocks()).thenReturn(List.of());
        when(mempool.take(Integer.MAX_VALUE)).thenReturn(List.of());

        NodeProperties props = new NodeProperties();
        svc = new NodeService(chain, mempool, mining, broadcaster, props, store, metrics);
    }

    @Test
    void submitTxBroadcastsAndAddsToMempool() {
        Transaction tx = mock(Transaction.class);
        Map<String, TxOutput> utxo = Map.of("foo", mock(TxOutput.class));
        when(chain.getUtxoSnapshot()).thenReturn(utxo);

        svc.submitTx(tx);

        verify(mempool,   times(1)).submit(eq(tx), eq(utxo));
        verify(broadcaster, times(1)).broadcastTx(any(NewTxDto.class), isNull());
    }

    @Test
    void acceptExternalTxAddsOnlyToMempool() {
        Transaction tx = mock(Transaction.class);
        Map<String, TxOutput> utxo = Map.of();
        when(chain.getUtxoSnapshot()).thenReturn(utxo);

        svc.acceptExternalTx(tx);

        verify(mempool, times(1)).submit(eq(tx), eq(utxo));
        verifyNoInteractions(broadcaster);
    }

    @Test
    void mineNowProcessesBlockCorrectly() {
        Block b = mock(Block.class);
        when(mining.mine()).thenReturn(b);
        when(chain.getLatest()).thenReturn(b);
        when(b.getTxList()).thenReturn(List.of());

        Mono<Block> mono = svc.mineNow();
        Block result = mono.block();
        assertSame(b, result);

        verify(chain,   times(1)).addBlock(b);
        verify(store,   times(1)).save(b);
        verify(mempool, times(1)).purge(b.getTxList());
        verify(broadcaster, times(1))
            .broadcastBlock(argThat(dto ->
                dto instanceof NewBlockDto &&
                ((NewBlockDto)dto).rawBlockJson().equals(toJson(b))
            ), isNull());
    }

    @Test
    void acceptExternalBlockAppendsAndPurges() {
        Block b = mock(Block.class);
        when(b.getTxList()).thenReturn(List.of());

        svc.acceptExternalBlock(b);

        verify(chain,   times(1)).addBlock(b);
        verify(store,   times(1)).save(b);
        verify(mempool, times(1)).purge(b.getTxList());
    }

    @Test
    void reorgRestoresOrphanedTransactions() {
        Transaction orphanTx = mock(Transaction.class);
        Block oldBlock = mock(Block.class);
        when(oldBlock.getTxList()).thenReturn(List.of(mock(Transaction.class), orphanTx));
        when(oldBlock.getHashHex()).thenReturn("old");

        Block newBlock = mock(Block.class);
        when(newBlock.getTxList()).thenReturn(List.of());
        when(newBlock.getHashHex()).thenReturn("new");

        when(chain.getBlocks()).thenReturn(List.of(oldBlock), List.of(newBlock));
        Map<String, TxOutput> utxo = Map.of();
        when(chain.getUtxoSnapshot()).thenReturn(utxo);

        svc.acceptExternalBlock(oldBlock); // populate snapshot
        svc.acceptExternalBlock(newBlock); // triggers reorg

        verify(mempool, times(1)).submit(eq(orphanTx), eq(utxo));
    }
}
