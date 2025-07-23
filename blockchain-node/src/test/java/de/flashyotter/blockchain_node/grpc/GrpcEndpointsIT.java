package de.flashyotter.blockchain_node.grpc;

import blockchain.core.model.Block;
// Using our own Empty class
import de.flashyotter.blockchain_node.service.NodeService;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrpcEndpointsIT {

    @Mock
    private NodeService nodeService;

    private ChainGrpcService service;

    @BeforeEach
    void setUp() {
        service = new ChainGrpcService(nodeService);
    }

    @Test
    void testLatestBlock() {
        // Arrange
        Block coreBlock = new Block(1, "prevHash", List.of(), 1, System.currentTimeMillis(), 42);
        when(nodeService.latestBlock()).thenReturn(coreBlock);
        
        @SuppressWarnings("unchecked")
        StreamObserver<de.flashyotter.blockchain_node.grpc.Block> responseObserver = mock(StreamObserver.class);
        ArgumentCaptor<de.flashyotter.blockchain_node.grpc.Block> blockCaptor = 
            ArgumentCaptor.forClass(de.flashyotter.blockchain_node.grpc.Block.class);

        // Act
        service.latest(Empty.getDefaultInstance(), responseObserver);

        // Assert
        verify(responseObserver).onNext(blockCaptor.capture());
        verify(responseObserver).onCompleted();
        
        de.flashyotter.blockchain_node.grpc.Block protoBlock = blockCaptor.getValue();
        assertThat(protoBlock).isNotNull();
        assertThat(protoBlock.getPreviousHashHex()).isEqualTo("prevHash");
        assertThat(protoBlock.getHeight()).isEqualTo(1);
        assertThat(protoBlock.getNonce()).isEqualTo(42);
    }

    @Test
    void testBlockPage() {
        // Arrange
        int page = 0;
        int size = 10;
        List<Block> blocks = List.of(
            new Block(1, "prevHash1", List.of(), 1, System.currentTimeMillis(), 42),
            new Block(2, "prevHash2", List.of(), 1, System.currentTimeMillis(), 43)
        );
        when(nodeService.blockPage(page, size)).thenReturn(blocks);
        
        @SuppressWarnings("unchecked")
        StreamObserver<de.flashyotter.blockchain_node.grpc.BlockList> responseObserver = mock(StreamObserver.class);
        ArgumentCaptor<de.flashyotter.blockchain_node.grpc.BlockList> blockListCaptor = 
            ArgumentCaptor.forClass(de.flashyotter.blockchain_node.grpc.BlockList.class);

        // Act
        service.page(de.flashyotter.blockchain_node.grpc.PageRequest.newBuilder()
            .setPage(page)
            .setSize(size)
            .build(), responseObserver);

        // Assert
        verify(responseObserver).onNext(blockListCaptor.capture());
        verify(responseObserver).onCompleted();
        
        de.flashyotter.blockchain_node.grpc.BlockList blockList = blockListCaptor.getValue();
        assertThat(blockList).isNotNull();
        assertThat(blockList.getBlocksList()).hasSize(2);
        assertThat(blockList.getBlocks(0).getPreviousHashHex()).isEqualTo("prevHash1");
        assertThat(blockList.getBlocks(1).getPreviousHashHex()).isEqualTo("prevHash2");
    }
}
