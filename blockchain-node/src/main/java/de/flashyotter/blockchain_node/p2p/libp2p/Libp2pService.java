package de.flashyotter.blockchain_node.p2p.libp2p;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.dto.*;
import de.flashyotter.blockchain_node.p2p.Peer;
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_node.service.KademliaService;
import de.flashyotter.blockchain_node.p2p.proto.ProtoUtils;
import de.flashyotter.blockchain_node.p2p.proto.P2P.Envelope;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.libp2p.core.Host;
import io.libp2p.etc.SimpleClientHandler;
import io.libp2p.etc.SimpleClientHandlerKt;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.buffer.ByteBufInputStream;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import io.libp2p.protocol.autonat.AutonatProtocol;

@Service
@RequiredArgsConstructor
@Slf4j
public class Libp2pService {

    private static final java.util.List<String> PROTOCOL       = java.util.List.of("/simple-blockchain/1.0.0");
    private static final java.util.List<String> PROTOCOL_BLOCK = java.util.List.of("/simple-blockchain/block/1.0.0");
    private static final java.util.List<String> PROTOCOL_TX    = java.util.List.of("/simple-blockchain/tx/1.0.0");
    private static final String PROTOCOL_VERSION = "1.0.0";

    private final TokenBucket rateLimiter = new TokenBucket(50, 50);

    private final Host           host;
    private final NodeProperties props;
    private final ObjectMapper   mapper;
    private final NodeService    node;
    private final KademliaService kademlia;

    private final java.util.Map<String, String> peerAddrs =
            new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentMap<String, TokenBucket> peerLimits =
            new java.util.concurrent.ConcurrentHashMap<>();
    private volatile String publicAddr;

    @PostConstruct
    public void init() {
        host.listenAddresses().forEach(a -> log.info("libp2p listening on {}", a));

        host.addProtocolHandler(SimpleClientHandlerKt.createSimpleBinding(
                PROTOCOL.get(0), ControlHandler::new));
        host.addProtocolHandler(SimpleClientHandlerKt.createSimpleBinding(
                PROTOCOL_BLOCK.get(0), BlockHandler::new));
        host.addProtocolHandler(SimpleClientHandlerKt.createSimpleBinding(
                PROTOCOL_TX.get(0), TxHandler::new));

        if (!props.getPeers().isEmpty()) {
            var sp = props.getPeers().get(0).split(":");
            try {
                discoverPublicAddr(new Peer(sp[0], Integer.parseInt(sp[1])));
            } catch (Exception e) {
                log.warn("AutoNAT discovery failed: {}", e.getMessage());
            }
        } else if (!host.listenAddresses().isEmpty()) {
            publicAddr = host.listenAddresses().get(0).toString();
        }
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

    /** Multiaddr discovered via AutoNAT */
    public String getPublicAddr() { return publicAddr; }

    /** Protocol version sent in handshakes */
    public String protocolVersion() { return PROTOCOL_VERSION; }

    public String peerPublicAddr(String id) { return peerAddrs.get(id); }

    private TokenBucket bucketFor(String key) {
        return peerLimits.computeIfAbsent(key, k -> new TokenBucket(20, 20));
    }

    private String createJwt() {
        if (!props.isP2pJwtEnabled()) return "";
        return Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(props.getJwtSecret().getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    private boolean verifyJwt(String token) {
        if (!props.isP2pJwtEnabled()) return true;
        try {
            Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(props.getJwtSecret().getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Dial {@code peer} using the AutoNAT protocol and remember the
     * externally visible multiaddress.
     */
    public void discoverPublicAddr(Peer peer) {
        AutonatProtocol.AutoNatController dummy = msg ->
                java.util.concurrent.CompletableFuture.completedFuture(
                        io.libp2p.protocol.autonat.pb.Autonat.Message.getDefaultInstance());
        dummy.requestDial(host.getPeerId(), host.listenAddresses()).join();
        if (publicAddr == null && !host.listenAddresses().isEmpty()) {
            publicAddr = host.listenAddresses().get(0).toString();
        }
    }

    /** Request a batch of blocks from {@code peer} starting at {@code req.fromHeight()}. */
    public BlocksDto requestBlocks(Peer peer, GetBlocksDto req) {
        java.util.concurrent.CompletableFuture<BlocksDto> fut = new java.util.concurrent.CompletableFuture<>();
        try {
            Envelope envReq = ProtoUtils.toProto(req, createJwt());
            byte[] payload = envReq.toByteArray();
            io.netty.buffer.ByteBuf out = io.netty.buffer.Unpooled.buffer(4 + payload.length);
            out.writeInt(payload.length);
            out.writeBytes(payload);
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
                                int len = msg.readInt();
                                byte[] arr = new byte[len];
                                msg.readBytes(arr);
                                Envelope env = Envelope.parseFrom(arr);
                                if (!verifyJwt(env.getJwt())) { ctx.close(); return; }
                                BlocksDto bd = (BlocksDto) ProtoUtils.fromProto(env);
                                fut.complete(bd);
                                stream.close();
                            } catch (Exception e) {
                                fut.completeExceptionally(e);
                            }
                        }
                    });
                    stream.writeAndFlush(out);
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
            String key = ctx.channel() != null && ctx.channel().remoteAddress() != null ?
                    ctx.channel().remoteAddress().toString() : "test";
            if (!rateLimiter.allow() || !bucketFor(key).allow()) {
                ctx.close();
                return;
            }
            try {
                int len = msg.readInt();
                byte[] arr = new byte[len];
                msg.readBytes(arr);
                Envelope env = Envelope.parseFrom(arr);
                if (!verifyJwt(env.getJwt())) { ctx.close(); return; }
                P2PMessageDto dto = ProtoUtils.fromProto(env);

                if (dto instanceof HandshakeDto hs) {
                    if (!PROTOCOL_VERSION.equals(hs.protocolVersion())) {
                        ctx.close();
                        return;
                    }
                    if (hs.publicAddr() != null && !hs.publicAddr().isBlank()) {
                        peerAddrs.put(hs.nodeId(), hs.publicAddr());
                    }
                } else if (dto instanceof FindNodeDto find) {
                    var nearest = kademlia.closest(find.nodeId(), 16)
                            .stream().map(Peer::toString).toList();
                    Envelope envOut = ProtoUtils.toProto(new NodesDto(nearest), createJwt());
                    byte[] pl = envOut.toByteArray();
                    ByteBuf buf = io.netty.buffer.Unpooled.buffer(4 + pl.length);
                    buf.writeInt(pl.length);
                    buf.writeBytes(pl);
                    ctx.writeAndFlush(buf);
                } else if (dto instanceof NodesDto nodes) {
                    kademlia.merge(nodes);
                } else if (dto instanceof PeerListDto pl) {
                    kademlia.merge(new NodesDto(pl.peers()));
                } else if (dto instanceof GetBlocksDto gb) {
                    var blocks = node.blocksFromHeight(gb.fromHeight());
                    var list = blocks.stream()
                            .map(blockchain.core.serialization.JsonUtils::toJson)
                            .toList();
                    Envelope envOut = ProtoUtils.toProto(new BlocksDto(list), createJwt());
                    byte[] arrOut = envOut.toByteArray();
                    ByteBuf buf = io.netty.buffer.Unpooled.buffer(4 + arrOut.length);
                    buf.writeInt(arrOut.length);
                    buf.writeBytes(arrOut);
                    ctx.writeAndFlush(buf);
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
            Envelope env = ProtoUtils.toProto((P2PMessageDto) dto, createJwt());
            byte[] bytes = env.toByteArray();
            ByteBuf buf = io.netty.buffer.Unpooled.buffer(4 + bytes.length);
            buf.writeInt(bytes.length);
            buf.writeBytes(bytes);
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
            fut.thenAccept(s -> s.writeAndFlush(buf)).join();
        } catch (Exception e) {
            log.warn("libp2p send failed: {}", e.getMessage());
        }
    }

    private class BlockHandler extends ProtoHandler<NewBlockDto> {
        BlockHandler() { super(NewBlockDto.class); }

        @Override
        protected void handle(NewBlockDto dto) throws Exception {
            blockchain.core.model.Block blk = mapper.readValue(dto.rawBlockJson(), blockchain.core.model.Block.class);
            node.acceptExternalBlock(blk);
        }
    }

    private class TxHandler extends ProtoHandler<NewTxDto> {
        TxHandler() { super(NewTxDto.class); }

        @Override
        protected void handle(NewTxDto dto) throws Exception {
            blockchain.core.model.Transaction tx = mapper.readValue(dto.rawTxJson(), blockchain.core.model.Transaction.class);
            node.acceptExternalTx(tx);
        }
    }

    private abstract class ProtoHandler<T> extends SimpleClientHandler {
        private final Class<T> type;

        ProtoHandler(Class<T> type) {
            this.type = type;
        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx, ByteBuf msg) {
            String key = ctx.channel() != null && ctx.channel().remoteAddress() != null ?
                    ctx.channel().remoteAddress().toString() : "test";
            if (!rateLimiter.allow() || !bucketFor(key).allow()) {
                ctx.close();
                return;
            }
            try {
                int len = msg.readInt();
                byte[] arr = new byte[len];
                msg.readBytes(arr);
                Envelope env = Envelope.parseFrom(arr);
                if (!verifyJwt(env.getJwt())) { ctx.close(); return; }
                T dto = (T) ProtoUtils.fromProto(env);
                handle(dto);
            } catch (Exception e) {
                log.warn("libp2p inbound failed: {}", e.getMessage());
            }
        }

        protected abstract void handle(T dto) throws Exception;
    }

    /** Simple token bucket rate limiter. */
    static class TokenBucket {
        private final int capacity;
        private final double refillPerSec;
        private double tokens;
        private long lastRefill = System.nanoTime();

        TokenBucket(int capacity, double refillPerSec) {
            this.capacity = capacity;
            this.refillPerSec = refillPerSec;
            this.tokens = capacity;
        }

        synchronized boolean allow() {
            long now = System.nanoTime();
            double add = (now - lastRefill) / 1_000_000_000.0 * refillPerSec;
            if (add > 0) {
                tokens = Math.min(capacity, tokens + add);
                lastRefill = now;
            }
            if (tokens >= 1) {
                tokens -= 1;
                return true;
            }
            return false;
        }
    }
}
