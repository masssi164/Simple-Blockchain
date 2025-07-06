package blockchain.core.mempool;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import blockchain.core.crypto.AddressUtils;
import blockchain.core.exceptions.BlockchainException;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxInput;
import blockchain.core.model.TxOutput;

/** Priority mem-pool with simple fee-based eviction. */
public class Mempool {

    private static final int DEFAULT_MAX = 1000;

    private final Map<String, Entry> pool = new ConcurrentHashMap<>();
    private final PriorityQueue<Entry> byFee = new PriorityQueue<>(Comparator.comparingDouble(e -> e.fee));
    private final int maxSize;

    private record Entry(String txHash, Transaction tx, double fee) {}

    public Mempool() { this(DEFAULT_MAX); }

    public Mempool(int maxSize) { this.maxSize = maxSize; }

    /**
     * Adds a transaction after validating it against the provided UTXO view.
     * The map should reflect the confirmed outputs plus all spends already
     * present in this mempool to avoid accepting double spends.
     */
    public void add(Transaction tx, Map<String, TxOutput> effectiveUtxo) {
        if (tx.isCoinbase()) throw new BlockchainException("coinbase ➜ mempool");
        if (!tx.verifySignatures()) throw new BlockchainException("invalid signatures");

        for (TxInput in : tx.getInputs()) {
            /* 1 – must reference an existing UTXO */
            if (!effectiveUtxo.containsKey(in.getReferencedOutputId()))
                throw new BlockchainException("UTXO not found");
            TxOutput referenced = effectiveUtxo.get(in.getReferencedOutputId());

            /* 2 – public key must match the address in UTXO */
            String senderAddr = AddressUtils.publicKeyToAddress(in.getSender());
            if (!senderAddr.equals(referenced.recipientAddress()))
                throw new BlockchainException("pub-key mismatch");

            /* 3 – no double-spend inside the pool */
            if (isSpent(in.getReferencedOutputId()))
                throw new BlockchainException("double-spend in mempool");
        }
        double fee = calcFee(tx, effectiveUtxo);
        String id = tx.calcHashHex();
        Entry entry = new Entry(id, tx, fee);
        synchronized (this) {
            pool.put(id, entry);
            byFee.add(entry);
            if (pool.size() > maxSize) {
                Entry evicted = byFee.poll();
                pool.remove(evicted.tx().calcHashHex());
            }
        }
    }

    public List<Transaction> take(int max) {
        return pool.values().stream()
                   .sorted((a, b) -> Double.compare(b.fee, a.fee))
                   .limit(max)
                   .map(e -> e.tx)
                   .toList();
    }

    public void removeAll(Collection<Transaction> confirmed) {
        synchronized (this) {
            for (Transaction tx : confirmed) {
                Entry e = pool.remove(tx.calcHashHex());
                if (e != null) byFee.remove(e);
            }
        }
    }

    /** Current number of transactions in the pool. */
    public int size() {
        return pool.size();
    }

    private boolean isSpent(String refId) {
        return pool.values().stream()
                   .map(e -> e.tx)
                   .flatMap(t -> t.getInputs().stream())
                   .anyMatch(in -> in.getReferencedOutputId().equals(refId));
    }

    private double calcFee(Transaction tx, Map<String, TxOutput> utxo) {
        double inSum = 0.0;
        for (TxInput in : tx.getInputs()) {
            inSum += utxo.get(in.getReferencedOutputId()).value();
        }
        double outSum = tx.getOutputs().stream().mapToDouble(TxOutput::value).sum();
        return inSum - outSum;
    }
}
