package de.flashyotter.blockchain_node.p2p.libp2p;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.dto.*;
import de.flashyotter.blockchain_node.p2p.Peer;
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_node.service.KademliaService;
import io.libp2p.core.Host;
import io.libp2p.etc.SimpleClientHandler;
import io.libp2p.etc.SimpleClientHandlerKt;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class Libp2pService {

    private static final java.util.List<String> PROTOCOL       = java.util.List.of("/simple-blockchain/1.0.0");
    private static final java.util.List<String> PROTOCOL_BLOCK = java.util.List.of("/simple-blockchain/block/1.0.0");
    private static final java.util.List<String> PROTOCOL_TX    = java.util.List.of("/simple-blockchain/tx/1.0.0");

    private final Host           host;
    private final NodeProperties props;
    private final ObjectMapper   mapper;
    private final NodeService    node;
    private final KademliaService kademlia;

    @PostConstruct
    public void init() {
        host.listenAddresses().forEach(a -> log.info("libp2p listening on {}", a));

        host.addProtocolHandler(SimpleClientHandlerKt.createSimpleBinding(
                PROTOCOL.get(0), ControlHandler::new));
        host.addProtocolHandler(SimpleClientHandlerKt.createSimpleBinding(
                PROTOCOL_BLOCK.get(0), BlockHandler::new));
        host.addProtocolHandler(SimpleClientHandlerKt.createSimpleBinding(
                PROTOCOL_TX.get(0), TxHandler::new));
    }

    public void broadcast(java.util.Collection<Peer> peers, P2PMessageDto dto) {
        peers.forEach(p -> send(p, dto));
    }

    public void send(Peer peer, P2PMessageDto dto) {
        send(peer, PROTOCOL, dto);
    }

    public void sendBlock(Peer peer, NewBlockDto dto) {
        send(peer, PROTOCOL_BLOCK, dto);
    }

    public void sendTx(Peer peer, NewTxDto dto) {
        send(peer, PROTOCOL_TX, dto);
    }

    public void broadcastBlocks(java.util.Collection<Peer> peers, NewBlockDto dto) {
        peers.forEach(p -> sendBlock(p, dto));
    }

    public void broadcastTxs(java.util.Collection<Peer> peers, NewTxDto dto) {
        peers.forEach(p -> sendTx(p, dto));
    }

    /** Handles peer discovery messages (FIND_NODE/NODES). */
    class ControlHandler extends SimpleClientHandler {
        @Override
        public void messageReceived(ChannelHandlerContext ctx, ByteBuf msg) {
            try {
                String json = msg.toString(java.nio.charset.StandardCharsets.UTF_8);
                P2PMessageDto dto = mapper.readValue(json, P2PMessageDto.class);

                if (dto instanceof FindNodeDto find) {
                    var nearest = kademlia.closest(find.nodeId(), 16)
                            .stream().map(Peer::toString).toList();
                    String reply = mapper.writeValueAsString(new NodesDto(nearest));
                    ctx.writeAndFlush(io.netty.buffer.Unpooled.copiedBuffer(reply, java.nio.charset.StandardCharsets.UTF_8));
                } else if (dto instanceof NodesDto nodes) {
                    kademlia.merge(nodes);
                } else if (dto instanceof PeerListDto pl) {
                    kademlia.merge(new NodesDto(pl.peers()));
                }
            } catch (Exception e) {
                log.warn("libp2p inbound failed: {}", e.getMessage());
            }
        }
    }

    private void send(Peer peer, java.util.List<String> protocol, Object dto) {
        try {
            String json = mapper.writeValueAsString(dto);
            io.libp2p.core.multiformats.Multiaddr addr =
                    new io.libp2p.core.multiformats.Multiaddr(peer.multiAddr());
            host.getNetwork().connect(addr)
                .thenCompose(conn -> host.newStream(protocol, conn).getStream())
                .thenAccept(s -> s.writeAndFlush(json)).join();
        } catch (Exception e) {
            log.warn("libp2p send failed: {}", e.getMessage());
        }
    }

    private class BlockHandler extends JsonHandler<NewBlockDto> {
        BlockHandler() { super(NewBlockDto.class); }

        @Override
        protected void handle(NewBlockDto dto) throws Exception {
            blockchain.core.model.Block blk = mapper.readValue(dto.rawBlockJson(), blockchain.core.model.Block.class);
            node.acceptExternalBlock(blk);
        }
    }

    private class TxHandler extends JsonHandler<NewTxDto> {
        TxHandler() { super(NewTxDto.class); }

        @Override
        protected void handle(NewTxDto dto) throws Exception {
            blockchain.core.model.Transaction tx = mapper.readValue(dto.rawTxJson(), blockchain.core.model.Transaction.class);
            node.acceptExternalTx(tx);
        }
    }

    private abstract class JsonHandler<T> extends SimpleClientHandler {
        private final Class<T> type;

        JsonHandler(Class<T> type) {
            this.type = type;
        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx, ByteBuf msg) {
            try {
                String json = msg.toString(java.nio.charset.StandardCharsets.UTF_8);
                T dto = mapper.readValue(json, type);
                handle(dto);
            } catch (Exception e) {
                log.warn("libp2p inbound failed: {}", e.getMessage());
            }
        }

        protected abstract void handle(T dto) throws Exception;
    }
}
