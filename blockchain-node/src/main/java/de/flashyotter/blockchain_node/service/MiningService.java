package de.flashyotter.blockchain_node.service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Service;

import blockchain.core.consensus.Chain;
import blockchain.core.consensus.ConsensusParams;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import de.flashyotter.blockchain_node.wallet.WalletService;
import de.flashyotter.blockchain_node.config.NodeProperties;
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

        int threads = Math.max(1, props.getMiningThreads());
        if (threads == 1) {
            candidate.mineLocally();
            return candidate;
        }

        long fixedTime = candidate.getTimeMillis();
        AtomicReference<Block> result = new AtomicReference<>();
        ForkJoinPool pool = new ForkJoinPool(threads);
        try {
            pool.submit(() -> java.util.stream.IntStream.range(0, threads).parallel().forEach(t -> {
                Block work = new Block(height, prevHash, txs, bits, fixedTime, t);
                while (result.get() == null) {
                    if (work.isProofValid()) {
                        result.compareAndSet(null, work);
                        break;
                    }
                    for (int i = 0; i < threads && result.get() == null; i++) {
                        work.getHeader().incrementNonce();
                    }
                }
            })).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
        } finally {
            pool.shutdownNow();
        }

        return result.get();
    }
}
