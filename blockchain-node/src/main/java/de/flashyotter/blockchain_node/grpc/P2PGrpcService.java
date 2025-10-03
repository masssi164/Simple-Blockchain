package de.flashyotter.blockchain_node.grpc;

import de.flashyotter.blockchain_node.grpc.p2p.P2PGrpcServiceGrpc;
import de.flashyotter.blockchain_node.grpc.p2p.P2PMessageRequest;
import de.flashyotter.blockchain_node.grpc.p2p.P2PMessageResponse;
import de.flashyotter.blockchain_node.p2p.P2PMessage;
import de.flashyotter.blockchain_node.service.P2PService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.ByteString;

/**
 * Implementation of the P2P gRPC service for handling peer-to-peer messages.
 * This service is used when nodes communicate over gRPC instead of direct TCP connections.
 */
@GrpcService
@RequiredArgsConstructor
@Slf4j
public class P2PGrpcService extends P2PGrpcServiceGrpc.P2PGrpcServiceImplBase {

    private final P2PService p2pService;

    @Override
    public void handleMessage(P2PMessageRequest request,
                              StreamObserver<P2PMessageResponse> responseObserver) {
        try {
            P2PMessage incoming = P2PMessage.parseFrom(request.getP2PMessageBytes());
            P2PMessage response = p2pService.handleMessage(incoming);

            P2PMessageResponse.Builder builder = P2PMessageResponse.newBuilder();
            if (response != null) {
                builder.setP2PMessageBytes(ByteString.copyFrom(response.toByteArray()));
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (InvalidProtocolBufferException e) {
            log.error("Failed to parse P2P message", e);
            responseObserver.onError(e);
        } catch (Exception e) {
            log.error("Error handling P2P message", e);
            responseObserver.onError(e);
        }
    }
}
