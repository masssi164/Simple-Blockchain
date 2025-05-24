package de.flashyotter.blockchain_node.service;

import blockchain.core.consensus.Chain;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Builds a candidate block from the mempool then performs Proof-of-Work.
 * Uses the previous block's difficulty bits so refactor does not touch core.
 */
@Service
@RequiredArgsConstructor
public class MiningService {

    private final Chain          chain;
    private final MempoolService mempool;

    public Block mine() {
        List<Transaction> txs = mempool.take(500);

        int nextHeight = chain.getLatest().getHeight() + 1;
        String prevHash = chain.getLatest().getHashHex();
        int bits = chain.getLatest().getCompactDifficultyBits(); // reuse current difficulty

        Block candidate = new Block(nextHeight, prevHash, txs, bits);
        candidate.mineLocally();
        return candidate;
    }
}
