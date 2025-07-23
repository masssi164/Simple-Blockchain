package de.flashyotter.blockchain_node.service;

import com.google.protobuf.ByteString;
import de.flashyotter.blockchain_node.p2p.*;
import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.p2p.P2PProtoMapper;
import de.flashyotter.blockchain_node.dto.*;
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
            P2PMessageDto dto = P2PProtoMapper.fromProto(message);
            if (dto instanceof NewBlockDto nb) {
                Block blk = JsonUtils.blockFromJson(nb.rawBlockJson());
                nodeService.acceptExternalBlock(blk);
                return P2PProtoMapper.toProto(new NewBlockDto(nb.rawBlockJson()));
            } else if (dto instanceof NewTxDto nt) {
                Transaction tx = JsonUtils.txFromJson(nt.rawTxJson());
                nodeService.acceptExternalTx(tx);
                return P2PProtoMapper.toProto(new NewTxDto(nt.rawTxJson()));
            } else if (dto instanceof HandshakeDto hs) {
                if (hs.nodeId() == null || hs.nodeId().isBlank()) {
                    return P2PProtoMapper.toProto(new HandshakeDto("ERROR: Invalid node ID format", null, "", 0, 0));
                }
                return P2PProtoMapper.toProto(new HandshakeDto(
                        props.getId(),
                        hs.peerId(),
                        hs.protocolVersion(),
                        props.getLibp2pPort(),
                        props.getPort()
                ));
            } else if (dto instanceof GetBlocksDto gb) {
                List<Block> blocks = nodeService.blocksFromHeight(gb.fromHeight());
                List<String> raw = blocks.stream().map(JsonUtils::toJson).toList();
                return P2PProtoMapper.toProto(new BlocksDto(raw));
            } else if (dto instanceof FindNodeDto) {
                return P2PProtoMapper.toProto(new NodesDto(props.getPeers()));
            } else {
                return P2PProtoMapper.toProto(new HandshakeDto("ERROR: Unknown message type", null, "", 0, 0));
            }
        } catch (Exception e) {
            log.error("Error handling P2P message", e);
            return P2PProtoMapper.toProto(new HandshakeDto("ERROR: " + e.getMessage(), null, "", 0, 0));
        }
    }
}
