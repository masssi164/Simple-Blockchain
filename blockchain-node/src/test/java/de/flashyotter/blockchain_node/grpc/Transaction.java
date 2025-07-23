package de.flashyotter.blockchain_node.grpc;

/**
 * Test stub for Transaction
 */
public class Transaction {
    private String txId;
    private String timestamp;
    private double amount;
    private double maxFee;
    private double tip;
    
    public static Builder newBuilder() {
        return new Builder();
    }
    
    public String getTxId() {
        return txId;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public double getMaxFee() {
        return maxFee;
    }
    
    public double getTip() {
        return tip;
    }
    
    public static class Builder {
        private Transaction instance = new Transaction();
        
        public Builder setTxId(String txId) {
            instance.txId = txId;
            return this;
        }
        
        public Builder setTimestamp(String timestamp) {
            instance.timestamp = timestamp;
            return this;
        }
        
        public Builder setAmount(double amount) {
            instance.amount = amount;
            return this;
        }
        
        public Builder setMaxFee(double maxFee) {
            instance.maxFee = maxFee;
            return this;
        }
        
        public Builder setTip(double tip) {
            instance.tip = tip;
            return this;
        }
        
        public Transaction build() {
            return instance;
        }
    }
}
