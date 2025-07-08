package de.flashyotter.blockchain_node.grpc;

import de.flashyotter.blockchain_node.service.NodeService;
import net.devh.boot.grpc.server.service.GrpcService;
import io.grpc.stub.StreamObserver;

import de.flashyotter.blockchain_node.grpc.Empty;
import de.flashyotter.blockchain_node.grpc.Block;

@GrpcService
public class MiningGrpcService extends MiningGrpc.MiningImplBase {

    private final NodeService node;

    public MiningGrpcService(NodeService node) {
        this.node = node;
    }

    @Override
    public void mine(Empty request, StreamObserver<Block> responseObserver) {
        var blk = node.mineNow().block();
        responseObserver.onNext(GrpcMapper.toProto(blk));
        responseObserver.onCompleted();
    }
}
