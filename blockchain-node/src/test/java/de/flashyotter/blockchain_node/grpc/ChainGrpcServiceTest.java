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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChainGrpcServiceTest {
    @Mock 
    NodeService nodeService;

    ChainGrpcService service;

    @BeforeEach
    void setup() {
        service = new ChainGrpcService(nodeService);
    }

    @Test
    void testLatestBlock() {
        // Arrange
        BlockHeader header = new BlockHeader(1, "0x123", "0xabc", 0x1d00ffff, 123456789L, 42);
        Block mockBlock = new Block(header, new ArrayList<>());
        when(nodeService.latestBlock()).thenReturn(mockBlock);

        @SuppressWarnings("unchecked")
        StreamObserver<de.flashyotter.blockchain_node.grpc.Block> responseObserver = mock(StreamObserver.class);
        ArgumentCaptor<de.flashyotter.blockchain_node.grpc.Block> blockCaptor = 
            ArgumentCaptor.forClass(de.flashyotter.blockchain_node.grpc.Block.class);

        // Act
        service.latest(Empty.getDefaultInstance(), responseObserver);
        
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
    }

    @Test
    void testLatestBlock_Error() {
        // Arrange
        when(nodeService.latestBlock()).thenThrow(new RuntimeException("test error"));

        @SuppressWarnings("unchecked")
        StreamObserver<de.flashyotter.blockchain_node.grpc.Block> responseObserver = mock(StreamObserver.class);

        // Act
        service.latest(Empty.getDefaultInstance(), responseObserver);
        
        // Assert
        verify(responseObserver).onError(any(RuntimeException.class));
        verifyNoMoreInteractions(responseObserver);
    }

    @Test
    void testGetPage() {
        // Arrange
        List<Block> blocks = new ArrayList<>();

        BlockHeader header1 = new BlockHeader(1, "0x123", "0xabc", 0x1d00ffff, 123456789L, 42);
        blocks.add(new Block(header1, new ArrayList<>()));

        BlockHeader header2 = new BlockHeader(2, "0xdef", "0xdef", 0x1d00ffff, 123456790L, 43);
        blocks.add(new Block(header2, new ArrayList<>()));

        when(nodeService.blockPage(0, 10)).thenReturn(blocks);

        @SuppressWarnings("unchecked")
        StreamObserver<BlockList> responseObserver = mock(StreamObserver.class);
        ArgumentCaptor<BlockList> blockListCaptor = ArgumentCaptor.forClass(BlockList.class);

        // Act
        service.page(PageRequest.newBuilder()
            .setPage(0)
            .setSize(10)
            .build(), responseObserver);
        
        // Assert
        verify(responseObserver).onNext(blockListCaptor.capture());
        verify(responseObserver).onCompleted();
        verifyNoMoreInteractions(responseObserver);

        BlockList blockList = blockListCaptor.getValue();
        assertThat(blockList).isNotNull();
        assertThat(blockList.getBlocksCount()).isEqualTo(2);
        assertThat(blockList.getBlocks(0).getHeight()).isEqualTo(1);
        assertThat(blockList.getBlocks(0).getMerkleRootHex()).isEqualTo("0xabc");
        assertThat(blockList.getBlocks(1).getHeight()).isEqualTo(2);
        assertThat(blockList.getBlocks(1).getMerkleRootHex()).isEqualTo("0xdef");
    }

    @Test
    void testGetPage_Empty() {
        // Arrange
        when(nodeService.blockPage(0, 10)).thenReturn(List.of());

        @SuppressWarnings("unchecked")
        StreamObserver<BlockList> responseObserver = mock(StreamObserver.class);
        ArgumentCaptor<BlockList> blockListCaptor = ArgumentCaptor.forClass(BlockList.class);

        // Act
        service.page(PageRequest.newBuilder()
            .setPage(0)
            .setSize(10)
            .build(), responseObserver);
        
        // Assert
        verify(responseObserver).onNext(blockListCaptor.capture());
        verify(responseObserver).onCompleted();
        verifyNoMoreInteractions(responseObserver);

        BlockList blockList = blockListCaptor.getValue();
        assertThat(blockList).isNotNull();
        assertThat(blockList.getBlocksCount()).isEqualTo(0);
    }
}
