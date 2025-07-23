package de.flashyotter.blockchain_node.grpc;

/**
 * Test stub for PageRequest
 */
public class PageRequest {
    private int page;
    private int size;
    
    public int getPage() {
        return page;
    }
    
    public int getSize() {
        return size;
    }
    
    public static Builder newBuilder() {
        return new Builder();
    }
    
    public static class Builder {
        private PageRequest instance = new PageRequest();
        
        public Builder setPage(int page) {
            instance.page = page;
            return this;
        }
        
        public Builder setSize(int size) {
            instance.size = size;
            return this;
        }
        
        public PageRequest build() {
            return instance;
        }
    }
}
