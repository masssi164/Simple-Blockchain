package blockchain.core.mempool;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import blockchain.core.crypto.AddressUtils;
import blockchain.core.exceptions.BlockchainException;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxInput;
import blockchain.core.model.TxOutput;

/** Very small, non-prioritised mem-pool suitable for demo nodes. */
public class Mempool {

    private final Map<String, Transaction> pool = new ConcurrentHashMap<>();

    public void add(Transaction tx, Map<String, TxOutput> utxo) {
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
        pool.put(tx.calcHashHex(), tx);
    }

    public List<Transaction> take(int max) {
        return pool.values().stream().limit(max).collect(Collectors.toList());
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
