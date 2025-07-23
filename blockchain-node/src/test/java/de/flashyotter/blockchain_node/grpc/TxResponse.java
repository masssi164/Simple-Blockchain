package de.flashyotter.blockchain_node.grpc;

/**
 * Stub class for TxResponse (for testing)
 */
public class TxResponse {
    private boolean success;
    private String message;
    
    public boolean getSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public static Builder newBuilder() {
        return new Builder();
    }
    
    public static class Builder {
        private TxResponse instance = new TxResponse();
        
        public Builder setSuccess(boolean success) {
            instance.success = success;
            return this;
        }
        
        public Builder setMessage(String message) {
            instance.message = message;
            return this;
        }
        
        public TxResponse build() {
            return instance;
        }
    }
}
