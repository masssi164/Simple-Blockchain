package blockchain.core.mempool;

import blockchain.core.exceptions.BlockchainException;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxInput;
import blockchain.core.model.TxOutput;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/** Very small, non-prioritised mem-pool suitable for demo nodes. */
public class Mempool {

    private final Map<String, Transaction> pool = new ConcurrentHashMap<>();

    public void add(Transaction tx, Map<String, TxOutput> utxo) {
        if (tx.isCoinbase()) throw new BlockchainException("coinbase ➜ mempool");

        if (!tx.verifySignatures()) throw new BlockchainException("invalid signatures");

        // reject double-spends
        for (TxInput in : tx.getInputs()) {
            if (!utxo.containsKey(in.getReferencedOutputId()))
                throw new BlockchainException("UTXO not found");

            if (isSpent(in.getReferencedOutputId()))
                throw new BlockchainException("double-spend in mempool");
        }
        pool.put(tx.calcHashHex(), tx);
    }

    public List<Transaction> take(int max) {
        return pool.values().stream()
                   .limit(max)
                   .collect(Collectors.toList());
    }

    public void removeAll(Collection<Transaction> confirmed) {
        confirmed.forEach(tx -> pool.remove(tx.calcHashHex()));
    }

    private boolean isSpent(String refId) {
        return pool.values().stream()
                   .flatMap(t -> t.getInputs().stream())
                   .anyMatch(in -> in.getReferencedOutputId().equals(refId));
    }
}
