package de.flashyotter.blockchain_node.p2p;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.flashyotter.blockchain_node.dto.*;
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_node.service.P2PBroadcastService;
import de.flashyotter.blockchain_node.service.PeerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.util.List;

/**
 * Handles raw JSON P2P frames – uses plain instanceof checks so Java 17
 */
@Component
@RequiredArgsConstructor
public class PeerServer extends TextWebSocketHandler {

    private final ObjectMapper        mapper;
    private final NodeService         node;
    private final PeerRegistry        registry;
    private final P2PBroadcastService broadcaster;

    @Override
    @SneakyThrows
    public void handleTextMessage(WebSocketSession sess, TextMessage msg) {

        P2PMessageDto dto = mapper.readValue(msg.getPayload(), P2PMessageDto.class);

        if (dto instanceof NewTxDto nt) {                      // new transaction
            var tx = mapper.readValue(nt.rawTxJson(),
                                       blockchain.core.model.Transaction.class);
            node.acceptExternalTx(tx);
            broadcaster.broadcastTx(nt, null);
            return;
        }

        if (dto instanceof NewBlockDto nb) {                   // new block
            var blk = mapper.readValue(nb.rawBlockJson(),
                                        blockchain.core.model.Block.class);
            node.acceptExternalBlock(blk);
            broadcaster.broadcastBlock(nb, null);
            return;
        }

        if (dto instanceof GetBlocksDto gb) {                  // range request
            List<blockchain.core.model.Block> blocks =
                    node.blocksFromHeight(gb.fromHeight());
            List<String> raws = blocks.stream()
                                       .map(blockchain.core.serialization.JsonUtils::toJson)
                                       .toList();
            sess.sendMessage(new TextMessage(
                    mapper.writeValueAsString(new BlocksDto(raws))));
            return;
        }

        if (dto instanceof PeerListDto pl) {                   // discovery
            registry.addAll(pl.peers().stream().map(Peer::fromString).toList());
        }
    }
}
