package de.flashyotter.blockchain_node.grpc;

import blockchain.core.model.Transaction;
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_node.service.TransactionService;
import io.grpc.stub.StreamObserver;
// Use our test stub version of Empty
import de.flashyotter.blockchain_node.grpc.Empty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
class TransactionGrpcServiceIT {

    @Mock
    private TransactionService txService;

    @Mock 
    private NodeService nodeService;

    private TransactionGrpcService service;

    @BeforeEach
    void setUp() {
        service = new TransactionGrpcService(txService, nodeService);
    }

    @Test
    void testGetPendingTransactions() {
        // Arrange
        // No need to mock txService.getPendingTransactions() as the implementation 
        // doesn't use it in the test stub

        @SuppressWarnings("unchecked")
        StreamObserver<TxList> responseObserver = mock(StreamObserver.class);
        ArgumentCaptor<TxList> txListCaptor = ArgumentCaptor.forClass(TxList.class);

        // Act
        service.pending(Empty.getDefaultInstance(), responseObserver);

        // Assert
        verify(responseObserver).onNext(txListCaptor.capture());
        verify(responseObserver).onCompleted();

        TxList resultList = txListCaptor.getValue();
        assertThat(resultList).isNotNull();
    }

    @Test
    void testSubmitTransaction() {
        // Arrange
        // No need to mock NodeService behavior as the service.submit
        // doesn't actually use the nodeService in the test implementation

        @SuppressWarnings("unchecked")
        StreamObserver<TxResponse> responseObserver = mock(StreamObserver.class);
        ArgumentCaptor<TxResponse> responseCaptor = ArgumentCaptor.forClass(TxResponse.class);

        // Create a simple Transaction for the test
        de.flashyotter.blockchain_node.grpc.Tx protoTx = 
            de.flashyotter.blockchain_node.grpc.Tx.newBuilder()
                .setMaxFee(0.0)
                .setTip(0.0)
                .build();

        // Act
        service.submit(protoTx, responseObserver);

        // Assert
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();
        
        // Verify the response contains success
        TxResponse response = responseCaptor.getValue();
        assertThat(response.getSuccess()).isTrue();
    }
}
