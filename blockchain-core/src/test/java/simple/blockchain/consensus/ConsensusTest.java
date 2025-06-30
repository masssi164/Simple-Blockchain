// blockchain-core/src/test/java/simple/blockchain/consensus/ChainTest.java
package simple.blockchain.consensus;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import blockchain.core.consensus.Chain;
import blockchain.core.consensus.ConsensusParams;
import blockchain.core.exceptions.BlockchainException;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import blockchain.core.model.Wallet;

/**
 * Quick, deterministic sanity-checks for the {@link Chain} class.
 */
class ChainTest {

    @Test
    @DisplayName("Genesis block: height 0, correct prev-hash, UTXO populated")
    void genesisBlockProperties() {
        Chain chain = new Chain();

        Block g = chain.getLatest();
        assertEquals(0, g.getHeight());
        assertEquals("0".repeat(64), g.getPreviousHashHex());
        // No PoW assertion – genesis is hard-coded, not mined.
        assertEquals(1, chain.getBlocks().size(), "only genesis in chain");
        assertFalse(chain.getUtxoSnapshot().isEmpty(), "coinbase UTXO present");
    }
    
    @Test
    @DisplayName("Appending a well-formed block updates height, UTXO and total work")
    void addValidBlock() {
        Chain chain = new Chain();
        Block prev  = chain.getLatest();
        BigInteger totalWorkBefore = chain.getTotalWork();   // ← NEW

        // craft coinbase & candidate block
        Wallet miner     = new Wallet();
        Transaction cb   = new Transaction(miner.getPublicKey(),
                                           ConsensusParams.blockReward(1),
                                           "1");
        Block candidate  = new Block(
                1, prev.getHashHex(), List.of(cb), prev.getCompactDifficultyBits());

        candidate.mineLocally();

        // attach
        chain.addBlock(candidate);

        assertEquals(2, chain.getBlocks().size(), "chain length");
        assertEquals(candidate, chain.getLatest(), "tip advanced");

        // coinbase now in global UTXO
        String id = cb.getOutputs().get(0).id(cb.calcHashHex(), 0);
        assertTrue(chain.getUtxoSnapshot().containsKey(id));

        // total work increased
        assertTrue(chain.getTotalWork().compareTo(totalWorkBefore) > 0,
                   "cumulative work increased");
    }

    @Test
    @DisplayName("Block whose prev-hash does not match the tip is rejected")
    void addBlockWithWrongPrevHashFails() {
        Chain chain = new Chain();

        Wallet miner = new Wallet();
        Transaction cb = new Transaction(miner.getPublicKey(),
                                         ConsensusParams.blockReward(1),
                                         "1");

        Block bogus = new Block(
                1, "deadbeef".repeat(8), List.of(cb),
                chain.getLatest().getCompactDifficultyBits());
        bogus.mineLocally();

        assertThrows(BlockchainException.class, () -> chain.addBlock(bogus));
    }
}
