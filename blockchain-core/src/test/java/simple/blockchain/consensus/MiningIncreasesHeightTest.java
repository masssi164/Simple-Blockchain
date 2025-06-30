package simple.blockchain.consensus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import blockchain.core.consensus.Chain;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import blockchain.core.model.Wallet;
import org.junit.jupiter.api.Test;

/** Regression – chain height must increase after mining a block locally. */
class MiningIncreasesHeightTest {

    @Test
    void heightAdvances() {
        Chain c     = new Chain();
        Block g     = c.getLatest();
        Wallet miner = new Wallet();

        Transaction cb = new Transaction(miner.getPublicKey(), 50.0, "1");
        Block b = new Block(
                1, g.getHashHex(), List.of(cb), g.getCompactDifficultyBits());
        b.mineLocally();

        c.addBlock(b);

        assertEquals(2, c.getBlocks().size(), "genesis + mined block");
        assertEquals(1, c.getLatest().getHeight(), "tip height = 1");
    }
}
