package de.flashyotter.blockchain_node.grpc;

import de.flashyotter.blockchain_node.service.NodeService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import de.flashyotter.blockchain_node.grpc.Empty;
import de.flashyotter.blockchain_node.grpc.Block;
import de.flashyotter.blockchain_node.grpc.PageRequest;
import de.flashyotter.blockchain_node.grpc.BlockList;

@GrpcService
public class ChainGrpcService extends ChainGrpc.ChainImplBase {

    private final NodeService node;

    public ChainGrpcService(NodeService node) {
        this.node = node;
    }

    @Override
    public void latest(Empty request, StreamObserver<Block> responseObserver) {
        try {
            var blk = node.latestBlock();
            responseObserver.onNext(GrpcMapper.toProto(blk));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void page(PageRequest request, StreamObserver<BlockList> responseObserver) {
        try {
            var blocks = node.blockPage(request.getPage(), request.getSize());
            var list = BlockList.newBuilder()
                    .addAllBlocks(blocks.stream().map(GrpcMapper::toProto).toList())
                    .build();
            responseObserver.onNext(list);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}
