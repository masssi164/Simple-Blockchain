package de.flashyotter.blockchain_node.grpc;

/**
 * Test stub for Block
 */
public class Block {
    private int height;
    private String hash;
    private String prevHash;
    private long timestamp;
    private String previousHashHex;
    private String merkleRootHex;
    private int compactBits;
    private int nonce;
    
    public int getHeight() {
        return height;
    }
    
    public String getHash() {
        return hash;
    }
    
    public String getPrevHash() {
        return prevHash;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public String getPreviousHashHex() {
        return previousHashHex;
    }
    
    public String getMerkleRootHex() {
        return merkleRootHex;
    }
    
    public int getCompactBits() {
        return compactBits;
    }
    
    public int getNonce() {
        return nonce;
    }
    
    public static Builder newBuilder() {
        return new Builder();
    }
    
    public static class Builder {
        private Block instance = new Block();
        
        public Builder setHeight(int height) {
            instance.height = height;
            return this;
        }
        
        public Builder setHash(String hash) {
            instance.hash = hash;
            return this;
        }
        
        public Builder setPrevHash(String prevHash) {
            instance.prevHash = prevHash;
            return this;
        }
        
        public Builder setTimestamp(long timestamp) {
            instance.timestamp = timestamp;
            return this;
        }
        
        public Builder setPreviousHashHex(String previousHashHex) {
            instance.previousHashHex = previousHashHex;
            return this;
        }
        
        public Builder setMerkleRootHex(String merkleRootHex) {
            instance.merkleRootHex = merkleRootHex;
            return this;
        }
        
        public Builder setCompactBits(int compactBits) {
            instance.compactBits = compactBits;
            return this;
        }
        
        public Builder setNonce(int nonce) {
            instance.nonce = nonce;
            return this;
        }
        
        public Builder addAllTxs(java.util.List<Tx> txs) {
            // Implement if needed
            return this;
        }
        
        public Block build() {
            return instance;
        }
    }
}
