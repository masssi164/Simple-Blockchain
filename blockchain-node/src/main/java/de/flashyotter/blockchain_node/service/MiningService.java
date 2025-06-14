package de.flashyotter.blockchain_node.service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Service;

import blockchain.core.consensus.Chain;
import blockchain.core.consensus.ConsensusParams;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import de.flashyotter.blockchain_node.config.NodeProperties;
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
    private final NodeProperties props;

    public Block mine() {

        /* 1) alle pending TXs holen ------------------------------------ */
        List<Transaction> memTx = mempool.take(500);

        /* 2) Coinbase für lokale Wallet bauen --------------------------- */
        int height   = chain.getLatest().getHeight() + 1;
        double reward = ConsensusParams.blockReward(height);
        Transaction coinbase = new Transaction(
                wallet.getLocalWallet().getPublicKey(), reward);

        /* 3) Liste zusammenstellen  (coinbase immer an Position 0) ------ */
        List<Transaction> txs = new java.util.ArrayList<>(1 + memTx.size());
        txs.add(coinbase);
        txs.addAll(memTx);

        /* 4) Block-Header füllen & PoW lösen ---------------------------- */
        String prevHash = chain.getLatest().getHashHex();
        int    bits     = chain.nextCompactBits();

        return mineParallel(height, prevHash, txs, bits);
    }

    /**
     * Searches for a valid nonce using multiple worker threads.
     */
    private Block mineParallel(int height, String prevHash,
                               List<Transaction> txs, int bits) {
        int threads = Math.max(1, props.getMiningThreads());
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        AtomicReference<Block> result = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(threads);

        long time = Instant.now().toEpochMilli();

        for (int i = 0; i < threads; i++) {
            final int start = i;
            pool.execute(() -> {
                try {
                    int nonce = start;
                    while (result.get() == null) {
                        Block candidate = new Block(height, prevHash, txs,
                                                     bits, time, nonce);
                        if (candidate.isProofValid()) {
                            result.compareAndSet(null, candidate);
                            break;
                        }
                        nonce += threads;
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            pool.shutdownNow();
        }

        return result.get();
    }
}
