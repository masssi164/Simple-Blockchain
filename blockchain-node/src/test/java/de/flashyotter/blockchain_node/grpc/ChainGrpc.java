package de.flashyotter.blockchain_node.grpc;

import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;

/**
 * Test stub for ChainGrpc
 */
public class ChainGrpc {

    /**
     * Base implementation for service methods
     */
    public static abstract class ChainImplBase {
        
        public void latest(Empty request, StreamObserver<Block> responseObserver) {
            try {
                Block response = Block.newBuilder()
                    .setHeight(1)
                    .setPreviousHashHex("0x123")
                    .setMerkleRootHex("0xabc")
                    .setCompactBits(0x1d00ffff)
                    .setNonce(42)
                    .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } catch (Exception e) {
                responseObserver.onError(e);
            }
        }
        
        public void page(PageRequest request, StreamObserver<BlockList> responseObserver) {
            try {
                Block block1 = Block.newBuilder()
                    .setHeight(1)
                    .setPreviousHashHex("0x123")
                    .setMerkleRootHex("0xabc")
                    .setCompactBits(0x1d00ffff)
                    .setNonce(42)
                    .build();
                
                Block block2 = Block.newBuilder()
                    .setHeight(2)
                    .setPreviousHashHex("0x456")
                    .setMerkleRootHex("0xdef")
                    .setCompactBits(0x1d00ffff)
                    .setNonce(43)
                    .build();
                
                BlockList response = BlockList.newBuilder()
                    .addBlocks(block1)
                    .addBlocks(block2)
                    .build();
                
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } catch (Exception e) {
                responseObserver.onError(e);
            }
        }
    }
    
    /**
     * Blocking stub for testing
     */
    public static class ChainBlockingStub {
        public Block latest(Empty request) {
            return Block.newBuilder()
                .setHeight(1)
                .setPreviousHashHex("0x123")
                .setMerkleRootHex("0xabc")
                .setCompactBits(0x1d00ffff)
                .setNonce(42)
                .build();
        }
        
        public BlockList page(PageRequest request) {
            Block block1 = Block.newBuilder()
                .setHeight(1)
                .setPreviousHashHex("0x123")
                .setMerkleRootHex("0xabc")
                .setCompactBits(0x1d00ffff)
                .setNonce(42)
                .build();
            
            Block block2 = Block.newBuilder()
                .setHeight(2)
                .setPreviousHashHex("0x456")
                .setMerkleRootHex("0xdef")
                .setCompactBits(0x1d00ffff)
                .setNonce(43)
                .build();
            
            return BlockList.newBuilder()
                .addBlocks(block1)
                .addBlocks(block2)
                .build();
        }
    }
    
    /**
     * Creates a new blocking stub
     */
    public static ChainBlockingStub newBlockingStub(ManagedChannel channel) {
        return new ChainBlockingStub();
    }
}
