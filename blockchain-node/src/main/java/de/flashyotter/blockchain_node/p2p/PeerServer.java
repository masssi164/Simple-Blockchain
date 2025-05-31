// blockchain-node/src/main/java/de/flashyotter/blockchain_node/p2p/PeerServer.java
package de.flashyotter.blockchain_node.p2p;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.flashyotter.blockchain_node.dto.*;
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_node.service.P2PBroadcastService;
import de.flashyotter.blockchain_node.service.PeerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;

@Component @RequiredArgsConstructor @Slf4j
public class PeerServer extends TextWebSocketHandler {

    private final ObjectMapper        mapper;
    private final NodeService         node;
    private final PeerRegistry        registry;
    private final P2PBroadcastService broadcast;

    private static final String PROTOCOL_VER = "0.4.0";

    @Override
    @SneakyThrows
    public void afterConnectionEstablished(WebSocketSession session) {
        // send our handshake immediately
        HandshakeDto hello = new HandshakeDto(
                java.util.UUID.randomUUID().toString(), PROTOCOL_VER);
        session.sendMessage(new TextMessage(mapper.writeValueAsString(hello)));
    }

    @Override
    @SneakyThrows
    public void handleTextMessage(WebSocketSession sess, TextMessage msg) {

        P2PMessageDto dto = mapper.readValue(msg.getPayload(), P2PMessageDto.class);

        /* ------------------------------------------------ handshake ---- */
        if (dto instanceof HandshakeDto hs) {
            if (!PROTOCOL_VER.split("\\.")[0]
                             .equals(hs.protocolVersion().split("\\.")[0])) {
                log.warn("⚠️  incompatible peer {}; disconnecting", hs);
                sess.close();
                return;
            }
            // send current peer-list after a successful handshake
            broadcast.broadcastPeerList();
            return;                                     // nothing further
        }

        /* ------------------------------------------------ transactions - */
        if (dto instanceof NewTxDto nt) {
            var tx = mapper.readValue(nt.rawTxJson(),
                                       blockchain.core.model.Transaction.class);
            node.acceptExternalTx(tx);
            broadcast.broadcastTx(nt, null);
            return;
        }

        /* ------------------------------------------------ blocks -------- */
        if (dto instanceof NewBlockDto nb) {
            var blk = mapper.readValue(nb.rawBlockJson(),
                                        blockchain.core.model.Block.class);
            node.acceptExternalBlock(blk);
            broadcast.broadcastBlock(nb, null);
            return;
        }

        /* ------------------------------------------------ range sync ---- */
        if (dto instanceof GetBlocksDto gb) {
            List<blockchain.core.model.Block> blocks =
                    node.blocksFromHeight(gb.fromHeight());
            List<String> raws = blocks.stream()
                                      .map(blockchain.core.serialization.JsonUtils::toJson)
                                      .toList();
            sess.sendMessage(new TextMessage(
                    mapper.writeValueAsString(new BlocksDto(raws))));
            return;
        }

        /* ------------------------------------------------ discovery ----- */
        if (dto instanceof PeerListDto pl) {
            registry.addAll(pl.peers().stream().map(Peer::fromString).toList());
            return;
        }
        if (dto instanceof HandshakeDto hs) {
            log.info("🤝  Handshake from {} (prot={})",
                     hs.nodeId(), hs.protocolVersion());
            return;
        }
    }
}
