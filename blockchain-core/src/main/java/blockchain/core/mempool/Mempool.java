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
    private final PriorityQueue<Entry> byFee = new PriorityQueue<>(Comparator.comparingDouble(e -> e.maxFee));
    private final int maxSize;
    private volatile double baseFee;

    private record Entry(String txHash, Transaction tx, double maxFee, double tip) {}

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
        double diff = calcFee(tx, effectiveUtxo);
        if (tx.getMaxFee() == 0.0) tx.setMaxFee(diff);
        Entry entry = new Entry(tx.calcHashHex(), tx, tx.getMaxFee(), tx.getTip());
        synchronized (this) {
            pool.put(entry.txHash(), entry);
            byFee.add(entry);
            if (pool.size() > maxSize) {
                Entry evicted = byFee.poll();
                pool.remove(evicted.tx().calcHashHex());
            }
        }
    }

    public synchronized List<Transaction> take(int max) {
        baseFee = pool.values().stream()
                       .mapToDouble(e -> e.maxFee)
                       .average().orElse(0.0);

        return pool.values().stream()
                   .sorted((a, b) -> Double.compare(b.tip, a.tip))
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
        if (outSum > inSum)
            throw new BlockchainException("outputs exceed inputs");
        return inSum - outSum;
    }

    /** Base fee calculated during the last {@link #take(int)} call. */
    public double getBaseFee() { return baseFee; }

    /** Tip specified by the given transaction. */
    public double tipFor(Transaction tx) {
        Entry e = pool.get(tx.calcHashHex());
        return e == null ? 0.0 : e.tip;
    }
}
