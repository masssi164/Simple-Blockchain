package blockchain.core.mempool;

import blockchain.core.exceptions.BlockchainException;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxInput;
import blockchain.core.model.TxOutput;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory transaction pool.
 *
 * * Rejects    – bad sig, coinbase, double-spend inside pool or unknown UTXO
 * * No mining  – selection / eviction kept minimal; real node may add fee/rbf logic
 */
public class Mempool {

    private final Map<String, Transaction> pool = new ConcurrentHashMap<>();

    /**
     * Validate & add a TX.
     *
     * @param utxo snapshot from the chain (read-only)
     */
    public void add(Transaction tx, Map<String, TxOutput> utxo) {

        if (tx.isCoinbase())                       throw new BlockchainException("coinbase → mempool");
        if (!tx.verifySignatures())                throw new BlockchainException("invalid signatures");

        /* ensure every input exists & is not already spent in pool */
        for (TxInput in : tx.getInputs()) {
            if (isSpent(in.getReferencedOutputId()))
                throw new BlockchainException("double spend in mempool: " + in.getReferencedOutputId());
            if (!utxo.containsKey(in.getReferencedOutputId()))
                throw new BlockchainException("unknown UTXO " + in.getReferencedOutputId());
        }
        pool.put(tx.calcHashHex(), tx);
    }

    /** True iff some TX in pool already spends {@code refId}. */
    private boolean isSpent(String refId) {
        return pool.values().stream()
                .flatMap(t -> t.getInputs().stream())
                .anyMatch(in -> in.getReferencedOutputId().equals(refId));
    }

    /** Snapshot for block assembly. */
    public List<Transaction> take(int max) {
        return pool.values().stream().limit(max).toList();
    }

    /** Purge confirmed transactions. */
    public void removeAll(Collection<Transaction> confirmed) {
        confirmed.forEach(tx -> pool.remove(tx.calcHashHex()));
    }
}
