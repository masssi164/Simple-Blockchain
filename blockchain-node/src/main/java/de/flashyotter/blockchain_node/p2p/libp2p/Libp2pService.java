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

    /** Request a batch of blocks from {@code peer} starting at {@code req.fromHeight()}. */
    public BlocksDto requestBlocks(Peer peer, GetBlocksDto req) {
        java.util.concurrent.CompletableFuture<BlocksDto> fut = new java.util.concurrent.CompletableFuture<>();
        try {
            String json = mapper.writeValueAsString(req);
            io.libp2p.core.multiformats.Multiaddr addr =
                    new io.libp2p.core.multiformats.Multiaddr(peer.multiAddr());
            java.util.concurrent.CompletableFuture<? extends io.libp2p.core.Stream> streamFuture;
            if (props.isLibp2pEncrypted() && peer.getId() != null) {
                io.libp2p.core.PeerId pid = io.libp2p.core.PeerId.fromBase58(peer.getId());
                streamFuture = host.newStream(PROTOCOL, pid, addr).getStream();
            } else {
                streamFuture = host.getNetwork().connect(addr)
                        .thenCompose(conn -> host.newStream(PROTOCOL, conn).getStream());
            }
            streamFuture.thenAccept(stream -> {
                    stream.pushHandler(new SimpleClientHandler() {
                        @Override
                        public void messageReceived(ChannelHandlerContext ctx, ByteBuf msg) {
                            try {
                                String body = msg.toString(java.nio.charset.StandardCharsets.UTF_8);
                                BlocksDto bd = mapper.readValue(body, BlocksDto.class);
                                fut.complete(bd);
                                stream.close();
                            } catch (Exception e) {
                                fut.completeExceptionally(e);
                            }
                        }
                    });
                    stream.writeAndFlush(json);
                }).join();
            return fut.get(5, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("libp2p request failed: {}", e.getMessage());
            return new BlocksDto(java.util.List.of());
        }
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
                } else if (dto instanceof GetBlocksDto gb) {
                    var blocks = node.blocksFromHeight(gb.fromHeight());
                    var list = blocks.stream()
                            .map(blockchain.core.serialization.JsonUtils::toJson)
                            .toList();
                    String reply = mapper.writeValueAsString(new BlocksDto(list));
                    ctx.writeAndFlush(io.netty.buffer.Unpooled.copiedBuffer(reply, java.nio.charset.StandardCharsets.UTF_8));
                } else if (dto instanceof BlocksDto bd) {
                    for (String raw : bd.rawBlocks()) {
                        blockchain.core.model.Block blk = mapper.readValue(raw, blockchain.core.model.Block.class);
                        node.acceptExternalBlock(blk);
                    }
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
            java.util.concurrent.CompletableFuture<? extends io.libp2p.core.Stream> fut;
            if (props.isLibp2pEncrypted() && peer.getId() != null) {
                io.libp2p.core.PeerId pid = io.libp2p.core.PeerId.fromBase58(peer.getId());
                fut = host.newStream(protocol, pid, addr).getStream();
            } else {
                fut = host.getNetwork().connect(addr)
                        .thenCompose(c -> host.newStream(protocol, c).getStream());
            }
            fut.thenAccept(s -> s.writeAndFlush(json)).join();
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
