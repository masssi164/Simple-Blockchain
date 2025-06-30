// blockchain-node/src/test/java/de/flashyotter/blockchain_node/service/NodeServiceUtxoRebuildIT.java
package de.flashyotter.blockchain_node.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import blockchain.core.consensus.Chain;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxOutput;
import blockchain.core.model.Wallet;
import de.flashyotter.blockchain_node.storage.InMemoryBlockStore;

/**
 * Walks through:  ▸ genesis  ▸ +1 mined block
 * and checks that NodeService.currentUtxo() sees *both* coinbase outputs.
 */
class NodeServiceUtxoRebuildIT {

    @Test
    @DisplayName("UTXO set contains outputs from genesis *and* the first mined block")
    void utxoRebuildAfterNewBlock() {
        /* build a tiny chain with one extra block ---------------------- */
        Chain   chain   = new Chain();
        Wallet  miner   = new Wallet();

        Transaction coinbase = new Transaction(miner.getPublicKey(), 50.0, "1");
        Block b1 = new Block(
                1,
                chain.getLatest().getHashHex(),
                List.of(coinbase),
                chain.getLatest().getCompactDifficultyBits());
        b1.mineLocally();
        chain.addBlock(b1);

        /* wire up NodeService exactly like the real node --------------- */
        NodeService node = new NodeService(
                chain,
                Mockito.mock(MempoolService.class),      // ↙ neue Reihenfolge
                Mockito.mock(MiningService.class),
                Mockito.mock(P2PBroadcastService.class),
                new InMemoryBlockStore());

        /* verify UTXO snapshot ----------------------------------------- */
        Map<String, TxOutput> utxo = node.currentUtxo();
        assertEquals(2, utxo.size(),            // 1 from genesis + 1 from b1
                     "UTXO must contain both coinbase outputs");
    }
}
