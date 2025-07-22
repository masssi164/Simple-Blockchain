package de.flashyotter.blockchain_node.service;

import com.google.protobuf.ByteString;
import de.flashyotter.blockchain_node.p2p.*;
import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.grpc.GrpcMapper;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import blockchain.core.serialization.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for handling P2P communication.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class P2PService {

    private final NodeService nodeService;
    private final NodeProperties props;

    public P2PMessage handleMessage(P2PMessage message) {
        try {
            switch (message.getMsgCase()) {
                case NEWBLOCK:
                    Block block = GrpcMapper.fromProto(message.getNewBlock().getBlock());
                    nodeService.acceptExternalBlock(block);
                    // Create a message with the new block
                    return P2PMessage.newBuilder()
                        .setNewBlock(NewBlock.newBuilder()
                            .setBlock(GrpcMapper.toProto(block))
                            .build())
                        .build();
                    
                case NEWTX:
                    Transaction tx = GrpcMapper.fromProto(message.getNewTx().getTx());
                    nodeService.acceptExternalTx(tx);
                    // Create a message with the new transaction
                    return P2PMessage.newBuilder()
                        .setNewTx(NewTx.newBuilder()
                            .setTx(GrpcMapper.toProto(tx))
                            .build())
                        .build();
                    
                case HANDSHAKE:
                    Handshake hs = message.getHandshake();
                    // Just validate node ID format for now
                    boolean valid = hs.getNodeId() != null && !hs.getNodeId().isEmpty();
                    if (!valid) {
                        // Return an error message
                        return P2PMessage.newBuilder()
                            .setHandshake(Handshake.newBuilder()
                                .setNodeId("ERROR: Invalid node ID format")
                                .build())
                            .build();
                    }
                    // Create a handshake response
                    return P2PMessage.newBuilder()
                        .setHandshake(Handshake.newBuilder()
                            .setNodeId(props.getBaseUrl())
                            .build())
                        .build();
                    
                case GETBLOCKS:
                    GetBlocks gb = message.getGetBlocks();
                    List<Block> blocks = nodeService.blocksFromHeight(gb.getFromHeight());
                    var builder = Blocks.newBuilder();
                    for (Block blk : blocks) {
                        builder.addBlocks(de.flashyotter.blockchain_node.grpc.GrpcMapper.toProto(blk));
                    }
                    return P2PMessage.newBuilder().setBlocks(builder).build();
                    
                case FINDNODE:
                    return P2PMessage.newBuilder()
                        .setNodes(Nodes.newBuilder()
                            .addAllNodes(props.getPeers())
                            .build())
                        .build();
                    
                default:
                    // Return error for unknown message types
                    return P2PMessage.newBuilder()
                        .setHandshake(Handshake.newBuilder()
                            .setNodeId("ERROR: Unknown message type")
                            .build())
                        .build();
            }
        } catch (Exception e) {
            log.error("Error handling P2P message", e);
            // Return error message for exceptions
            return P2PMessage.newBuilder()
                .setHandshake(Handshake.newBuilder()
                    .setNodeId("ERROR: " + e.getMessage())
                    .build())
                .build();
        }
    }
}
