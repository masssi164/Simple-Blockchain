package de.flashyotter.blockchain_node.grpc;

/**
 * Test stub for SendRequest
 */
public class SendRequest {
    private String recipient;
    private double amount;
    
    public String getRecipient() {
        return recipient;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public static Builder newBuilder() {
        return new Builder();
    }
    
    public static class Builder {
        private SendRequest instance = new SendRequest();
        
        public Builder setRecipient(String recipient) {
            instance.recipient = recipient;
            return this;
        }
        
        public Builder setAmount(double amount) {
            instance.amount = amount;
            return this;
        }
        
        public SendRequest build() {
            return instance;
        }
    }
}
