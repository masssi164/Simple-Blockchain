package de.flashyotter.blockchain_node.p2p;

import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import de.flashyotter.blockchain_node.dto.*;
import de.flashyotter.blockchain_node.grpc.GrpcMapper;
import de.flashyotter.blockchain_node.p2p.P2PMessage;
import de.flashyotter.blockchain_node.p2p.Handshake;
import de.flashyotter.blockchain_node.p2p.NewBlock;
import de.flashyotter.blockchain_node.p2p.NewTx;
import de.flashyotter.blockchain_node.p2p.GetBlocks;
import de.flashyotter.blockchain_node.p2p.Blocks;
import de.flashyotter.blockchain_node.p2p.PeerList;
import de.flashyotter.blockchain_node.p2p.FindNode;
import de.flashyotter.blockchain_node.p2p.Nodes;

/** Utility converting P2P DTOs to their protobuf representation and back. */
public final class P2PProtoMapper {
    private P2PProtoMapper() {}

    public static P2PMessage toProto(P2PMessageDto dto) {
        var builder = P2PMessage.newBuilder();
        if (dto instanceof HandshakeDto hs) {
            builder.setHandshake(Handshake.newBuilder()
                    .setNodeId(hs.nodeId())
                    .setPeerId(hs.peerId())
                    .setProtocolVersion(hs.protocolVersion())
                    .setListenPort(hs.listenPort())
                    .setRestPort(hs.restPort())
                    .build());
        } else if (dto instanceof NewBlockDto nb) {
            Block b = blockchain.core.serialization.JsonUtils.blockFromJson(nb.rawBlockJson());
            builder.setNewBlock(NewBlock.newBuilder()
                    .setBlock(GrpcMapper.toProto(b)).build());
        } else if (dto instanceof NewTxDto nt) {
            Transaction tx = blockchain.core.serialization.JsonUtils.txFromJson(nt.rawTxJson());
            builder.setNewTx(NewTx.newBuilder()
                    .setTx(GrpcMapper.toProto(tx)).build());
        } else if (dto instanceof GetBlocksDto gb) {
            builder.setGetBlocks(GetBlocks.newBuilder()
                    .setFromHeight(gb.fromHeight()).build());
        } else if (dto instanceof BlocksDto bd) {
            var list = bd.rawBlocks().stream()
                    .map(blockchain.core.serialization.JsonUtils::blockFromJson)
                    .map(GrpcMapper::toProto).toList();
            builder.setBlocks(Blocks.newBuilder()
                    .addAllBlocks(list).build());
        } else if (dto instanceof PeerListDto pl) {
            builder.setPeerList(PeerList.newBuilder()
                    .addAllPeers(pl.peers()).build());
        } else if (dto instanceof FindNodeDto fn) {
            builder.setFindNode(FindNode.newBuilder()
                    .setNodeId(fn.nodeId()).build());
        } else if (dto instanceof NodesDto nd) {
            builder.setNodes(Nodes.newBuilder()
                    .addAllNodes(nd.peers()).build());
        }
        return builder.build();
    }

    public static P2PMessageDto fromProto(P2PMessage msg) {
        return switch (msg.getMsgCase()) {
            case HANDSHAKE -> new HandshakeDto(
                    msg.getHandshake().getNodeId(),
                    msg.getHandshake().getPeerId(),
                    msg.getHandshake().getProtocolVersion(),
                    msg.getHandshake().getListenPort(),
                    msg.getHandshake().getRestPort());
            case NEWBLOCK -> new NewBlockDto(
                    blockchain.core.serialization.JsonUtils.toJson(
                            GrpcMapper.fromProto(msg.getNewBlock().getBlock())));
            case NEWTX -> new NewTxDto(
                    blockchain.core.serialization.JsonUtils.toJson(
                            GrpcMapper.fromProto(msg.getNewTx().getTx())));
            case GETBLOCKS -> new GetBlocksDto(msg.getGetBlocks().getFromHeight());
            case BLOCKS -> new BlocksDto(msg.getBlocks().getBlocksList().stream()
                    .map(GrpcMapper::fromProto)
                    .map(blockchain.core.serialization.JsonUtils::toJson)
                    .toList());
            case PEERLIST -> new PeerListDto(msg.getPeerList().getPeersList());
            case FINDNODE -> new FindNodeDto(msg.getFindNode().getNodeId());
            case NODES -> new NodesDto(msg.getNodes().getNodesList());
            default -> null;
        };
    }
}
