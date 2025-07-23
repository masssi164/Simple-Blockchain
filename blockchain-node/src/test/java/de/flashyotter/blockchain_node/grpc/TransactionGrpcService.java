package de.flashyotter.blockchain_node.grpc;

import blockchain.core.model.Transaction;
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_node.service.TransactionService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * Stub implementation for testing
 */
public class TransactionGrpcService extends TransactionGrpc.TransactionImplBase {
    private final TransactionService txService;
    private final NodeService nodeService;
    
    public TransactionGrpcService(TransactionService txService, NodeService nodeService) {
        this.txService = txService;
        this.nodeService = nodeService;
    }
    
    @Override
    public void pending(Empty request, StreamObserver<TxList> responseObserver) {
        // Just create an empty list for testing
        responseObserver.onNext(TxList.getDefaultInstance());
        responseObserver.onCompleted();
    }
    
    @Override
    public void submit(de.flashyotter.blockchain_node.grpc.Tx request, StreamObserver<TxResponse> responseObserver) {
        // Dummy implementation for test
        TxResponse response = TxResponse.newBuilder()
            .setSuccess(true)
            .setMessage("Transaction accepted for test")
            .build();
            
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
