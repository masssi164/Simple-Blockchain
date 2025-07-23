package de.flashyotter.blockchain_node.grpc;

import blockchain.core.model.Block;
import blockchain.core.model.BlockHeader;
import de.flashyotter.blockchain_node.grpc.Empty;
import de.flashyotter.blockchain_node.service.NodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import io.grpc.stub.StreamObserver;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MiningGrpcServiceTest {
    @Mock 
    NodeService nodeService;

    MiningGrpcService service;

    @BeforeEach
    void setup() {
        service = new MiningGrpcService(nodeService);
    }

    @Test
    void testMine() {
        // Arrange
        BlockHeader header = new BlockHeader(1, "0x123", "0xabc", 0x1d00ffff, 123456789L, 42);
        Block mockBlock = new Block(header, new ArrayList<>());
        when(nodeService.mineNow()).thenReturn(Mono.just(mockBlock));

        @SuppressWarnings("unchecked")
        StreamObserver<de.flashyotter.blockchain_node.grpc.Block> responseObserver = mock(StreamObserver.class);
        ArgumentCaptor<de.flashyotter.blockchain_node.grpc.Block> blockCaptor = 
            ArgumentCaptor.forClass(de.flashyotter.blockchain_node.grpc.Block.class);

        // Act
        service.mine(Empty.getDefaultInstance(), responseObserver);
        
        // Assert
        verify(responseObserver).onNext(blockCaptor.capture());
        verify(responseObserver).onCompleted();
        verifyNoMoreInteractions(responseObserver);

        de.flashyotter.blockchain_node.grpc.Block protoBlock = blockCaptor.getValue();
        assertThat(protoBlock).isNotNull();
        assertThat(protoBlock.getHeight()).isEqualTo(1);
        assertThat(protoBlock.getPreviousHashHex()).isEqualTo("0x123");
        assertThat(protoBlock.getMerkleRootHex()).isEqualTo("0xabc");
        assertThat(protoBlock.getCompactBits()).isEqualTo(0x1d00ffff);
        assertThat(protoBlock.getNonce()).isEqualTo(42);
    }

    @Test
    void testMine_Error() {
        // Arrange
        when(nodeService.mineNow()).thenReturn(Mono.error(new RuntimeException("test error")));

        @SuppressWarnings("unchecked")
        StreamObserver<de.flashyotter.blockchain_node.grpc.Block> responseObserver = mock(StreamObserver.class);

        // Act
        service.mine(Empty.getDefaultInstance(), responseObserver);
        
        // Assert
        verify(responseObserver).onError(any(RuntimeException.class));
        verifyNoMoreInteractions(responseObserver);
    }
}
