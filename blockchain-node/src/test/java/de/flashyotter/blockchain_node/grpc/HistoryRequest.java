package de.flashyotter.blockchain_node.grpc;

/**
 * Test stub for HistoryRequest
 */
public class HistoryRequest {
    private String address;
    private int limit;
    
    public String getAddress() {
        return address;
    }
    
    public int getLimit() {
        return limit;
    }
    
    public static Builder newBuilder() {
        return new Builder();
    }
    
    public static class Builder {
        private HistoryRequest instance = new HistoryRequest();
        
        public Builder setAddress(String address) {
            instance.address = address;
            return this;
        }
        
        public Builder setLimit(int limit) {
            instance.limit = limit;
            return this;
        }
        
        public HistoryRequest build() {
            return instance;
        }
    }
}
