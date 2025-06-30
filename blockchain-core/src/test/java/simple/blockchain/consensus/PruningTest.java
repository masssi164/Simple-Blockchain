package simple.blockchain.consensus;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import blockchain.core.consensus.Chain;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import blockchain.core.model.Wallet;

class PruningTest {
    @Test
    void pruneRemovesOldForkBlocks() {
        Chain chain = new Chain();
        Block genesis = chain.getLatest();
        Wallet miner = new Wallet();

        // extend main chain by three blocks
        Block prev = genesis;
        for (int i = 1; i <= 3; i++) {
            Transaction cb = new Transaction(miner.getPublicKey(), 50.0, String.valueOf(i));
            Block blk = new Block(i, prev.getHashHex(), List.of(cb), prev.getCompactDifficultyBits());
            blk.mineLocally();
            chain.addBlock(blk);
            prev = blk;
        }

        // side fork at height 1
        Transaction cbFork = new Transaction(miner.getPublicKey(), 50.0, "fork");
        Block fork = new Block(1, genesis.getHashHex(), List.of(cbFork), genesis.getCompactDifficultyBits());
        fork.mineLocally();
        chain.addBlock(fork);

        List<Block> removed = chain.pruneOldBlocks(2);

        assertTrue(removed.contains(fork), "fork block pruned");
        assertEquals(4, chain.getBlocks().size(), "active chain intact");
    }
}
