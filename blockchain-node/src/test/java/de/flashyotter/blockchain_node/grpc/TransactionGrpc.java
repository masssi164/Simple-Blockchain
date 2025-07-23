package de.flashyotter.blockchain_node.grpc;

import io.grpc.stub.StreamObserver;

/**
 * Stub class for transaction gRPC service (for testing)
 */
public class TransactionGrpc {
    
    public static abstract class TransactionImplBase {
        public void pending(Empty request, StreamObserver<TxList> responseObserver) {
            throw new UnsupportedOperationException("Method not implemented");
        }
        
        public void submit(Tx request, StreamObserver<TxResponse> responseObserver) {
            throw new UnsupportedOperationException("Method not implemented");
        }
    }
}
