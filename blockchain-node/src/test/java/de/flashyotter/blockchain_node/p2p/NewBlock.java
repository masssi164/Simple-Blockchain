package de.flashyotter.blockchain_node.p2p;

/**
 * Test stub for NewBlock
 */
public class NewBlock {
    private String blockJson;
    
    private NewBlock() {
    }
    
    public static Builder newBuilder() {
        return new Builder();
    }
    
    public String getBlockJson() {
        return blockJson;
    }
    
    public static class Builder {
        private NewBlock instance = new NewBlock();
        
        public Builder setBlockJson(String blockJson) {
            instance.blockJson = blockJson;
            return this;
        }
        
        public NewBlock build() {
            return instance;
        }
    }
}
