package de.flashyotter.blockchain_node.p2p.proto;

import de.flashyotter.blockchain_node.dto.*;
import de.flashyotter.blockchain_node.p2p.proto.P2P.Envelope;
import de.flashyotter.blockchain_node.p2p.proto.P2P.Handshake;
import de.flashyotter.blockchain_node.p2p.proto.P2P.NewBlock;
import de.flashyotter.blockchain_node.p2p.proto.P2P.NewTx;
import de.flashyotter.blockchain_node.p2p.proto.P2P.GetBlocks;
import de.flashyotter.blockchain_node.p2p.proto.P2P.Blocks;
import de.flashyotter.blockchain_node.p2p.proto.P2P.PeerList;
import de.flashyotter.blockchain_node.p2p.proto.P2P.FindNode;
import de.flashyotter.blockchain_node.p2p.proto.P2P.Nodes;

public class ProtoUtils {
    public static Envelope toProto(P2PMessageDto dto, String jwt) {
        Envelope.Builder env = Envelope.newBuilder();
        if (jwt != null) env.setJwt(jwt);
        if (dto instanceof HandshakeDto hs) {
            env.setHandshake(Handshake.newBuilder()
                    .setNodeId(hs.nodeId())
                    .setProtocolVersion(hs.protocolVersion())
                    .setListenPort(hs.listenPort())
                    .setPublicAddr(hs.publicAddr() == null ? "" : hs.publicAddr())
                    .build());
        } else if (dto instanceof NewBlockDto nb) {
            env.setNewBlock(NewBlock.newBuilder()
                    .setRawBlock(com.google.protobuf.ByteString.copyFrom(nb.rawBlockJson().getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                    .build());
        } else if (dto instanceof NewTxDto nt) {
            env.setNewTx(NewTx.newBuilder()
                    .setRawTx(com.google.protobuf.ByteString.copyFrom(nt.rawTxJson().getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                    .build());
        } else if (dto instanceof GetBlocksDto gb) {
            env.setGetBlocks(GetBlocks.newBuilder().setFromHeight(gb.fromHeight()).build());
        } else if (dto instanceof BlocksDto bd) {
            Blocks.Builder b = Blocks.newBuilder();
            for (String s : bd.rawBlocks())
                b.addRawBlocks(com.google.protobuf.ByteString.copyFrom(s.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            env.setBlocks(b.build());
        } else if (dto instanceof PeerListDto pl) {
            env.setPeerList(PeerList.newBuilder().addAllPeers(pl.peers()).build());
        } else if (dto instanceof FindNodeDto fn) {
            env.setFindNode(FindNode.newBuilder().setNodeId(fn.nodeId()).build());
        } else if (dto instanceof NodesDto nd) {
            env.setNodes(Nodes.newBuilder().addAllPeers(nd.peers()).build());
        }
        return env.build();
    }

    public static P2PMessageDto fromProto(Envelope env) {
        switch (env.getMsgCase()) {
            case HANDSHAKE -> {
                var hs = env.getHandshake();
                return new HandshakeDto(hs.getNodeId(), hs.getProtocolVersion(), hs.getListenPort(), hs.getPublicAddr());
            }
            case NEWBLOCK -> {
                var nb = env.getNewBlock();
                return new NewBlockDto(new String(nb.getRawBlock().toByteArray(), java.nio.charset.StandardCharsets.UTF_8));
            }
            case NEWTX -> {
                var nt = env.getNewTx();
                return new NewTxDto(new String(nt.getRawTx().toByteArray(), java.nio.charset.StandardCharsets.UTF_8));
            }
            case GETBLOCKS -> {
                var gb = env.getGetBlocks();
                return new GetBlocksDto(gb.getFromHeight());
            }
            case BLOCKS -> {
                var bl = env.getBlocks();
                java.util.List<String> list = new java.util.ArrayList<>();
                for (com.google.protobuf.ByteString bs : bl.getRawBlocksList())
                    list.add(new String(bs.toByteArray(), java.nio.charset.StandardCharsets.UTF_8));
                return new BlocksDto(list);
            }
            case PEERLIST -> {
                return new PeerListDto(env.getPeerList().getPeersList());
            }
            case FINDNODE -> {
                return new FindNodeDto(env.getFindNode().getNodeId());
            }
            case NODES -> {
                return new NodesDto(env.getNodes().getPeersList());
            }
            case MSG_NOT_SET -> {
                return null;
            }
        }
        return null;
    }
}
