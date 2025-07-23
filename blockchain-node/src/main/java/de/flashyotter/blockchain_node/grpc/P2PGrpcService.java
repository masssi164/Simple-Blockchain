package de.flashyotter.blockchain_node.grpc;

import com.google.protobuf.ByteString;
import de.flashyotter.blockchain_node.p2p.P2PMessage;
import de.flashyotter.blockchain_node.p2p.P2PProtoMapper;
import de.flashyotter.blockchain_node.dto.P2PMessageDto;
import de.flashyotter.blockchain_node.service.P2PService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * Implementation of the P2P gRPC service for handling peer-to-peer messages.
 * This service is used when nodes communicate over gRPC instead of direct TCP/WebSocket connections.
 */
@GrpcService
@RequiredArgsConstructor
@Slf4j
public class P2PGrpcService {

    private final P2PService p2pService;
    
    /**
     * Handles a P2P message received over gRPC
     * 
     * @param messageBytes Raw serialized P2P message
     * @return Response message as bytes if any, null otherwise
     */
    public byte[] handleGrpcMessage(byte[] messageBytes) {
        try {
            P2PMessage incomingMessage;
            try {
                // Parse incoming P2P message from bytes
                incomingMessage = P2PMessage.parseFrom(messageBytes);
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                log.error("Failed to parse P2P message", e);
                return null;
            }

            // Handle the message and get response
            P2PMessage response = p2pService.handleMessage(incomingMessage);
            
            if (response != null) {
                return response.toByteArray();
            }
            return null;
        } catch (Exception e) {
            log.error("Error handling P2P message", e);
            return null;
        }
    }
}
