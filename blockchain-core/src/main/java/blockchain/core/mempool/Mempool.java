package blockchain.core.mempool;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import blockchain.core.crypto.AddressUtils;
import blockchain.core.exceptions.BlockchainException;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxInput;
import blockchain.core.model.TxOutput;

/** Priority-based mem-pool with a configurable capacity. */
public class Mempool {

    private record Entry(Transaction tx, double feeRate, long seq) {}

    private final Map<String, Entry> index = new ConcurrentHashMap<>();
    private final PriorityQueue<Entry> queue;
    private final int limit;
    private long seq = 0L;

    public Mempool() {
        this(5000);
    }

    public Mempool(int limit) {
        this.limit = limit;
        this.queue = new PriorityQueue<>(Comparator
            .comparingDouble((Entry e) -> e.feeRate)
            .thenComparingLong(e -> e.seq));
    }

    public synchronized void add(Transaction tx, Map<String, TxOutput> utxo) {
        if (tx.isCoinbase()) throw new BlockchainException("coinbase ➜ mempool");
        if (!tx.verifySignatures()) throw new BlockchainException("invalid signatures");

        for (TxInput in : tx.getInputs()) {
            /* 1 – must reference an existing UTXO */
            if (!utxo.containsKey(in.getReferencedOutputId()))
                throw new BlockchainException("UTXO not found");
            TxOutput referenced = utxo.get(in.getReferencedOutputId());

            /* 2 – public key must match the address in UTXO */
            String senderAddr = AddressUtils.publicKeyToAddress(in.getSender());
            if (!senderAddr.equals(referenced.recipientAddress()))
                throw new BlockchainException("pub-key mismatch");

            /* 3 – no double-spend inside the pool */
            if (isSpent(in.getReferencedOutputId()))
                throw new BlockchainException("double-spend in mempool");
        }
        double inTotal = tx.getInputs().stream()
            .mapToDouble(in -> utxo.get(in.getReferencedOutputId()).value())
            .sum();
        double outTotal = tx.getOutputs().stream()
            .mapToDouble(TxOutput::value)
            .sum();
        double fee = inTotal - outTotal;
        int size   = tx.getInputs().size() + tx.getOutputs().size();
        double rate = fee / Math.max(1, size);

        Entry entry = new Entry(tx, rate, seq++);

        if (queue.size() >= limit) {
            Entry worst = queue.peek();
            if (worst != null && worst.feeRate >= rate) return; // drop new tx
            queue.poll();
            index.remove(worst.tx.calcHashHex());
        }

        queue.add(entry);
        index.put(tx.calcHashHex(), entry);
    }

    public synchronized List<Transaction> take(int max) {
        return queue.stream()
                    .sorted(Comparator
                        .comparingDouble((Entry e) -> e.feeRate).reversed()
                        .thenComparingLong(e -> e.seq))
                    .limit(max)
                    .map(Entry::tx)
                    .collect(Collectors.toList());
    }

    public synchronized void removeAll(Collection<Transaction> confirmed) {
        for (Transaction tx : confirmed) {
            Entry e = index.remove(tx.calcHashHex());
            if (e != null) queue.remove(e);
        }
    }

    private boolean isSpent(String refId) {
        return index.values().stream()
                     .flatMap(e -> e.tx.getInputs().stream())
                     .anyMatch(in -> in.getReferencedOutputId().equals(refId));
    }
}
