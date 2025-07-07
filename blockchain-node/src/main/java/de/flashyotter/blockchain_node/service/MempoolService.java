package de.flashyotter.blockchain_node.service;

import blockchain.core.mempool.Mempool;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxOutput;
import de.flashyotter.blockchain_node.config.NodeProperties;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Thin wrapper exposing core.Mempool to Spring beans.
 * Holds a reference to the global UTXO map of the Chain.
 */
@Service
public class MempoolService {

    private final Mempool mempool;

    public MempoolService(NodeProperties props) {
        this.mempool = new Mempool(props.getMempoolMaxSize());
    }

    public void submit(Transaction tx, Map<String, TxOutput> utxo) {
        mempool.add(tx, utxo);
    }

    public List<Transaction> take(int max) {
        return mempool.take(max);
    }

    public double getBaseFee() {
        return mempool.getBaseFee();
    }

    public double tipFor(Transaction tx) {
        return mempool.tipFor(tx);
    }

    public void purge(List<Transaction> confirmed) {
        mempool.removeAll(confirmed);
    }

    /** Current number of transactions in the pool. */
    public int size() {
        return mempool.size();
    }
}
