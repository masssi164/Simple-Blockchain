package de.flashyotter.blockchain_node.service;

import java.util.List;

import org.springframework.stereotype.Service;

import blockchain.core.consensus.Chain;
import blockchain.core.consensus.ConsensusParams;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import de.flashyotter.blockchain_node.wallet.WalletService;
import lombok.RequiredArgsConstructor;

/**
 * Builds a candidate block from the mempool then performs Proof-of-Work.
 * Uses the previous block's difficulty bits so refactor does not touch core.
 */
// blockchain-node/src/main/java/de/flashyotter/blockchain_node/service/MiningService.java
@Service
@RequiredArgsConstructor
public class MiningService {

    private final Chain          chain;
    private final MempoolService mempool;
    private final WalletService  wallet;          // ➊ neu

    public Block mine() {

        /* 1) alle pending TXs holen ------------------------------------ */
        List<Transaction> memTx = mempool.take(500);

        /* 2) Coinbase für lokale Wallet bauen --------------------------- */
        int height   = chain.getLatest().getHeight() + 1;
        double reward = ConsensusParams.blockReward(height);
        Transaction coinbase = new Transaction(
                wallet.getLocalWallet().getPublicKey(),
                reward,
                String.valueOf(height));

        /* 3) Liste zusammenstellen  (coinbase immer an Position 0) ------ */
        List<Transaction> txs = new java.util.ArrayList<>(1 + memTx.size());
        txs.add(coinbase);
        txs.addAll(memTx);

        /* 4) Block-Header füllen & PoW lösen ---------------------------- */
        String prevHash = chain.getLatest().getHashHex();
        int    bits     = chain.nextCompactBits();

        Block candidate = new Block(height, prevHash, txs, bits);
        candidate.mineLocally();
        return candidate;
    }
}
