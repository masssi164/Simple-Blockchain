package de.flashyotter.blockchain_node.grpc;

import java.util.ArrayList;
import java.util.List;

/**
 * Stub class for TxList (for testing)
 */
public class TxList {
    // Using ArrayList for direct access and modification
    private final List<Tx> txs;
    
    // Private constructor for builder pattern
    private TxList(List<Tx> txs) {
        this.txs = txs;
    }
    
    public int getTxsCount() {
        return txs.size();
    }
    
    public Tx getTxs(int index) {
        return txs.get(index);
    }
    
    public static TxList getDefaultInstance() {
        return newBuilder().build();
    }
    
    public static Builder newBuilder() {
        return new Builder();
    }
    
    public static class Builder {
        private final List<Tx> txs = new ArrayList<>();
        
        // Add a test Tx object to the list
        public Builder addTxs(Tx tx) {
            txs.add(tx);
            return this;
        }
        
        // Add all Txs from a list
        public Builder addAllTxs(List<Tx> txList) {
            txs.addAll(txList);
            return this;
        }
        
        // Build and return the TxList
        public TxList build() {
            // If no transactions were added, add a default one
            if (txs.isEmpty()) {
                Tx defaultTx = Tx.newBuilder()
                    .setMaxFee(0.0)
                    .setTip(0.0)
                    .build();
                txs.add(defaultTx);
            }
            return new TxList(new ArrayList<>(txs));
        }
    }
}
