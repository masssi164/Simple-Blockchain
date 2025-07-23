package de.flashyotter.blockchain_node.grpc;

/**
 * Test stub for WalletInfo
 */
public class WalletInfo {
    private String address;
    private double balance;
    
    public String getAddress() {
        return address;
    }
    
    public double getBalance() {
        return balance;
    }
    
    public static Builder newBuilder() {
        return new Builder();
    }
    
    public static class Builder {
        private WalletInfo instance = new WalletInfo();
        
        public Builder setAddress(String address) {
            instance.address = address;
            return this;
        }
        
        public Builder setBalance(double balance) {
            instance.balance = balance;
            return this;
        }
        
        public WalletInfo build() {
            return instance;
        }
    }
}
