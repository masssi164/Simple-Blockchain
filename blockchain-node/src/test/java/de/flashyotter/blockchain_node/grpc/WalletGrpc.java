package de.flashyotter.blockchain_node.grpc;

import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

/**
 * Test stub for WalletGrpc
 */
public class WalletGrpc {
    
    public static abstract class WalletImplBase {
        
        public void info(Empty request, StreamObserver<WalletInfo> responseObserver) {
            throw new UnsupportedOperationException("Method not implemented");
        }
        
        public void send(SendRequest request, StreamObserver<Transaction> responseObserver) {
            throw new UnsupportedOperationException("Method not implemented");
        }
        
        public void history(HistoryRequest request, StreamObserver<TxList> responseObserver) {
            throw new UnsupportedOperationException("Method not implemented");
        }
    }
    
    /**
     * Blocking stub for testing
     */
    public static class WalletBlockingStub {
        public WalletInfo info(Empty request) {
            return WalletInfo.newBuilder().build();
        }
        
        public TxList history(HistoryRequest request) {
            // Create a test Tx
            Tx tx = Tx.newBuilder()
                .setMaxFee(0.0)
                .setTip(0.0)
                .build();
            
            // Use builder to create TxList
            return TxList.newBuilder().build();
        }
        
        public Transaction send(SendRequest request) {
            return Transaction.newBuilder().build();
        }
    }
    
    /**
     * Creates a new blocking stub
     */
    public static WalletBlockingStub newBlockingStub(ManagedChannel channel) {
        return new WalletBlockingStub();
    }
}
