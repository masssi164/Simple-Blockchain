package de.flashyotter.blockchain_node.grpc;

import com.google.protobuf.ByteString;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import blockchain.core.serialization.JsonUtils;
import de.flashyotter.blockchain_node.dto.*;
import de.flashyotter.blockchain_node.p2p.*;
import lombok.NonNull;
import java.util.List;
import java.util.ArrayList;

/** 
 * Utility converting blocks and transactions to protobuf P2P messages and back.
 */
public final class P2PProtoMapper {
    private P2PProtoMapper() {}

    /** Convert DTO to protobuf message */
    public static P2PMessage toProto(@NonNull P2PMessageDto dto) {
        P2PMessage.Builder builder = P2PMessage.newBuilder();
        if (dto instanceof NewBlockDto blockDto) {
            builder.setNewBlock(NewBlock.newBuilder().setBlock(GrpcMapper.toProto(JsonUtils.blockFromJson(blockDto.rawBlockJson()))));
        } else if (dto instanceof NewTxDto txDto) {
            builder.setNewTx(NewTx.newBuilder().setTx(GrpcMapper.toProto(JsonUtils.txFromJson(txDto.rawTxJson()))));
        } else if (dto instanceof NodesDto nodesDto) {
            builder.setPeerList(PeerList.newBuilder().addAllPeers(nodesDto.peers()));
        } else if (dto instanceof BlocksDto blocksDto) {
            List<de.flashyotter.blockchain_node.grpc.Block> blockList = blocksDto.rawBlocks().stream()
                .map(JsonUtils::blockFromJson)
                .map(GrpcMapper::toProto)
                .toList();
            builder.setBlocks(Blocks.newBuilder().addAllBlocks(blockList));
        } else if (dto instanceof GetBlocksDto getBlocksDto) {
            builder.setGetBlocks(GetBlocks.newBuilder().setFromHeight(getBlocksDto.fromHeight()));
        } else if (dto instanceof FindNodeDto) {
            builder.setFindNode(FindNode.newBuilder());
        } else if (dto instanceof HandshakeDto handshakeDto) {
            builder.setHandshake(Handshake.newBuilder().setNodeId(handshakeDto.nodeId() != null ? handshakeDto.nodeId() : ""));
        } else {
            throw new IllegalArgumentException("Unsupported DTO type: " + dto.getClass());
        }

        if (dto.jwt() != null) {
            builder.setJwt(dto.jwt());
        }
        return builder.build();
    }

    /** Convert protobuf message to DTO */
    public static P2PMessageDto fromProto(@NonNull P2PMessage msg) {
        switch (msg.getMsgCase()) {
            case NEWBLOCK:
                return new NewBlockDto(
                    JsonUtils.toJson(GrpcMapper.fromProto(msg.getNewBlock().getBlock())),
                    msg.getJwt()
                );
            case NEWTX:
                return new NewTxDto(
                    JsonUtils.toJson(GrpcMapper.fromProto(msg.getNewTx().getTx())),
                    msg.getJwt()
                );
            case PEERLIST:
                NodesDto nodesDto = new NodesDto(
                    List.copyOf(msg.getPeerList().getPeersList())
                );
                return nodesDto;
            case BLOCKS:
                List<String> blockJsons = msg.getBlocks().getBlocksList().stream()
                    .map(GrpcMapper::fromProto)
                    .map(JsonUtils::toJson)
                    .toList();
                BlocksDto blocksDto = new BlocksDto(List.copyOf(blockJsons));
                return blocksDto;
            case GETBLOCKS:
                GetBlocksDto getBlocksDto = new GetBlocksDto(msg.getGetBlocks().getFromHeight());
                return getBlocksDto;
            case FINDNODE:
                return new FindNodeDto(msg.getJwt());
            case HANDSHAKE:
                return new HandshakeDto(
                    msg.getHandshake().getNodeId(), null, "1.0", 0, 0, msg.getJwt()
                );
            case MSG_NOT_SET:
            default:
                throw new IllegalStateException("Unexpected message type: " + msg.getMsgCase());
        }
    }
}
