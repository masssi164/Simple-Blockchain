package de.flashyotter.blockchain_node.grpc;

import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

/**
 * Test stub for MiningGrpc
 */
public class MiningGrpc {
    
    /**
     * Base implementation for service methods
     */
    public static abstract class MiningImplBase {
        
        public void mine(Empty request, StreamObserver<Block> responseObserver) {
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
    }
    
    /**
     * Blocking stub for testing
     */
    public static class MiningBlockingStub {
        public Block mine(Empty request) {
            return Block.newBuilder()
                .setHeight(1)
                .setPreviousHashHex("0x123")
                .setMerkleRootHex("0xabc")
                .setCompactBits(0x1d00ffff)
                .setNonce(42)
                .build();
        }
    }
    
    /**
     * Creates a new blocking stub
     */
    public static MiningBlockingStub newBlockingStub(ManagedChannel channel) {
        return new MiningBlockingStub();
    }
}
