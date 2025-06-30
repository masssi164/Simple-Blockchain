package simple.blockchain.consensus;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import blockchain.core.consensus.Chain;
import blockchain.core.consensus.ConsensusParams;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import blockchain.core.model.Wallet;

/**
 * Ensures that Chain#nextCompactBits() actually re-targets after every
 * {@link ConsensusParams#RETARGET_SPAN} blocks and that fast blocks lead
 * to *higher* difficulty (numerically smaller “bits” value).
 */
class DifficultyRetargetIT {

    @Test
    @DisplayName("Difficulty is adjusted after 10 blocks (bounded at ¼ × old target)")
    void retargetsAfterWindow() {
        Chain   chain        = new Chain();
        int     initialBits  = chain.getLatest().getCompactDifficultyBits();
        Block   prev         = chain.getLatest();
        Wallet  miner        = new Wallet();

        /* 9 very fast blocks (≈ 12 s apart instead of 60 s) —— */
        long fastInterval = ConsensusParams.TARGET_BLOCK_INTERVAL_MS / 5;

        for (int h = 1; h <= ConsensusParams.RETARGET_SPAN; h++) {
            Transaction coinbase = new Transaction(miner.getPublicKey(),
                                                   ConsensusParams.blockReward(h),
                                                   String.valueOf(h));
            Block b = new Block(
                    h,
                    prev.getHashHex(),
                    List.of(coinbase),
                    prev.getCompactDifficultyBits(),
                    prev.getTimeMillis() + fastInterval,
                    0);
            b.mineLocally();               //  ➜ valid PoW
            chain.addBlock(b);
            prev = b;
        }

        /* ask Chain for the bits of the *next* block → triggers retarget */
        int newBits = chain.nextCompactBits();

        assertNotEquals(initialBits, newBits, "must retarget");
        assertTrue(Integer.compareUnsigned(newBits, initialBits) < 0,
                   "difficulty increases (compact-bits value becomes smaller)");
    }
}
