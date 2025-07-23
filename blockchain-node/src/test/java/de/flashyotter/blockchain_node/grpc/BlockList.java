package de.flashyotter.blockchain_node.grpc;

import java.util.ArrayList;
import java.util.List;

/**
 * Test stub for BlockList
 */
public class BlockList {
    private final List<Block> blocks = new ArrayList<>();
    
    public Block getBlocks(int index) {
        return blocks.get(index);
    }
    
    public int getBlocksCount() {
        return blocks.size();
    }
    
    public List<Block> getBlocksList() {
        return new ArrayList<>(blocks);
    }
    
    public static BlockList getDefaultInstance() {
        return new BlockList();
    }
    
    public static Builder newBuilder() {
        return new Builder();
    }
    
    public static class Builder {
        private BlockList instance = new BlockList();
        
        public Builder addBlocks(Block block) {
            instance.blocks.add(block);
            return this;
        }
        
        public Builder addAllBlocks(List<Block> blocks) {
            instance.blocks.addAll(blocks);
            return this;
        }
        
        // Overload with Iterable to match the signature used in the service
        public Builder addAllBlocks(Iterable<Block> blocks) {
            blocks.forEach(instance.blocks::add);
            return this;
        }
        
        public BlockList build() {
            return instance;
        }
    }
}
