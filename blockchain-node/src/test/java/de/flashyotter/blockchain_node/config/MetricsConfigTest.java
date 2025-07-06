package de.flashyotter.blockchain_node.config;

import static org.junit.jupiter.api.Assertions.*;

import blockchain.core.consensus.Chain;
import blockchain.core.consensus.ConsensusParams;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import blockchain.core.model.Wallet;
import de.flashyotter.blockchain_node.service.MempoolService;
import de.flashyotter.blockchain_node.config.NodeProperties;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class MetricsConfigTest {

    private Chain chain;
    private MempoolService mempool;
    private SimpleMeterRegistry registry;

    @BeforeEach
    void setup() {
        chain = new Chain();
        mempool = new MempoolService(new NodeProperties());
        registry = new SimpleMeterRegistry();
        new MetricsConfig(registry, chain, mempool).init();
    }

    @Test
    void gaugesReflectChainAndMempool() {
        Gauge height = registry.find("node_block_height").gauge();
        Gauge size = registry.find("node_mempool_size").gauge();
        assertNotNull(height);
        assertNotNull(size);
        assertEquals(0.0, height.value());
        assertEquals(0.0, size.value());

        // add a mined block
        Wallet miner = new Wallet();
        Transaction cb = new Transaction(miner.getPublicKey(),
                ConsensusParams.blockReward(1), "1");
        Block b1 = new Block(1, chain.getLatest().getHashHex(), List.of(cb),
                chain.getLatest().getCompactDifficultyBits());
        b1.mineLocally();
        chain.addBlock(b1);
        assertEquals(1.0, height.value());

        // submit a transaction to the mempool
        String utxoId = cb.getOutputs().get(0).id(cb.calcHashHex(), 0);
        Transaction tx = new Transaction();
        tx.getInputs().add(new blockchain.core.model.TxInput(utxoId, new byte[0], miner.getPublicKey()));
        tx.getOutputs().add(new blockchain.core.model.TxOutput(1.0, new Wallet().getPublicKey()));
        tx.signInputs(miner.getPrivateKey());
        mempool.submit(tx, chain.getUtxoSnapshot());
        assertEquals(1.0, size.value());
    }
}
