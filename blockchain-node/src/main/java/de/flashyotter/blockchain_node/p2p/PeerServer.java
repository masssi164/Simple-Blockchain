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
 * Handles inbound WebSocket P2P messages.  Uses instanceof checks
 * to stay compatible with Java 17 (no pattern-matching switch).
 */
@Component
@RequiredArgsConstructor
public class PeerServer extends TextWebSocketHandler {

    private final ObjectMapper          mapper;
    private final NodeService           node;
    private final PeerRegistry          registry;
    private final P2PBroadcastService   broadcaster;

    @Override
    @SneakyThrows
    public void handleTextMessage(WebSocketSession sess, TextMessage msg) {

        P2PMessageDto dto = mapper.readValue(msg.getPayload(), P2PMessageDto.class);

        /* --- NEW_BLOCK ------------------------------------------------ */
        if (dto instanceof NewBlockDto nb) {
            var blk = mapper.readValue(nb.rawBlockJson(), blockchain.core.model.Block.class);
            node.acceptExternalBlock(blk);
            broadcaster.broadcastBlock(nb, null);
            return;
        }

        /* --- NEW_TX --------------------------------------------------- */
        if (dto instanceof NewTxDto nt) {
            var tx = mapper.readValue(nt.rawTxJson(), blockchain.core.model.Transaction.class);
            node.acceptExternalTx(tx);
            broadcaster.broadcastTx(nt, null);
            return;
        }

        /* --- PEER_LIST ------------------------------------------------ */
        if (dto instanceof PeerListDto pl) {
            pl.peers().forEach(s -> {
                var sp = s.split(":");
                registry.add(new Peer(sp[0], Integer.parseInt(sp[1])));
            });
            return;
        }

        /* --- GET_BLOCKS ---------------------------------------------- */
        if (dto instanceof GetBlocksDto gb) {
            List<blockchain.core.model.Block> blocks = node.blocksFromHeight(gb.fromHeight());
            List<String> raws = blocks.stream()
                    .map(blockchain.core.serialization.JsonUtils::toJson)
                    .toList();
            sess.sendMessage(new TextMessage(
                    mapper.writeValueAsString(new BlocksDto(raws))));
            return;
        }

        /* --- BLOCKS --------------------------------------------------- */
        if (dto instanceof BlocksDto blks) {
            for (String raw : blks.rawBlocks()) {
                var blk = mapper.readValue(raw, blockchain.core.model.Block.class);
                node.acceptExternalBlock(blk);
            }
        }
    }
}
