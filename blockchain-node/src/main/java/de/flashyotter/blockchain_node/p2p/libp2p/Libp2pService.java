package de.flashyotter.blockchain_node.p2p.libp2p;

import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.dto.*;
import de.flashyotter.blockchain_node.p2p.Peer;
import de.flashyotter.blockchain_node.p2p.P2PProto;
import de.flashyotter.blockchain_node.p2p.P2PProtoMapper;
import de.flashyotter.blockchain_node.p2p.P2PMessage;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_node.service.KademliaService;
import io.libp2p.core.Host;
import io.libp2p.etc.SimpleClientHandler;
import io.libp2p.etc.SimpleClientHandlerKt;
import io.libp2p.protocol.autonat.AutonatProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.nio.ByteBuffer;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class Libp2pService {

    private static final java.util.List<String> PROTOCOL       = java.util.List.of("/simple-blockchain/1.0.0");
    private static final java.util.List<String> PROTOCOL_BLOCK = java.util.List.of("/simple-blockchain/block/1.0.0");
    private static final java.util.List<String> PROTOCOL_TX    = java.util.List.of("/simple-blockchain/tx/1.0.0");
    private static final String PROTOCOL_VERSION = "1.0.0";

    private final PeerRateLimiter rateLimiter = new PeerRateLimiter(50, 50);

    private final Host host;
    private final NodeProperties props;
    private final NodeService node;
    private final KademliaService kademlia;

    public Libp2pService(Host host, NodeProperties props,
                         @org.springframework.context.annotation.Lazy NodeService node,
                         KademliaService kademlia) {
        this.host = host;
        this.props = props;
        this.node = node;
        this.kademlia = kademlia;
    }

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
            try {
                Peer first = Peer.parse(props.getPeers().get(0));
                discoverPublicAddr(first);
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

    /** Base58-encoded libp2p peer ID of this node. */
    public String peerId() {
        return host.getPeerId().toBase58();
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

    /** Number of known peers */
    public int peerCount() { return kademlia.peerCount(); }

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
            P2PMessage msg = P2PProtoMapper.toProto(req);
            if (props.getJwtSecret() != null && props.getJwtSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8).length >= 32) {
                String jwt = Jwts.builder()
                        .signWith(Keys.hmacShaKeyFor(
                                props.getJwtSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                        .compact();
                msg = msg.toBuilder().setJwt(jwt).build();
            }
            byte[] payload = msg.toByteArray();
            ByteBuffer buf = ByteBuffer.allocate(4 + payload.length).order(java.nio.ByteOrder.LITTLE_ENDIAN);
            buf.putInt(payload.length).put(payload).flip();
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
                                if (msg.readableBytes() < 4) return;
                                msg.markReaderIndex();
                                int len = msg.readIntLE();
                                if (msg.readableBytes() < len) {
                                    msg.resetReaderIndex();
                                    return;
                                }
                                if (len > 1_000_000) {
                                    log.warn("libp2p inbound failed: length {} exceeds limit", len);
                                    ctx.close();
                                    return;
                                }
                                byte[] data = new byte[len];
                                msg.readBytes(data);
                                P2PMessage pm = P2PMessage.parseFrom(data);
                                if (!pm.getJwt().isBlank() && props.getJwtSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8).length >= 32) {
                                    try {
                                        Jwts.parserBuilder()
                                                .setSigningKey(Keys.hmacShaKeyFor(
                                                        props.getJwtSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                                                .build()
                                                .parseClaimsJws(pm.getJwt());
                                    } catch (Exception ex) {
                                        ctx.close();
                                        return;
                                    }
                                }
                                BlocksDto bd = (BlocksDto) P2PProtoMapper.fromProto(pm);
                                fut.complete(bd);
                                stream.close();
                            } catch (Exception e) {
                                fut.completeExceptionally(e);
                            }
                        }
                    });
                    stream.writeAndFlush(io.netty.buffer.Unpooled.wrappedBuffer(buf.array()));
                }).join();
            // Allow more time for peers on CI runners which may be slow
            return fut.get(10, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("libp2p request failed", e);
            return new BlocksDto(java.util.List.of());
        }
    }

    /** Reactive variant of {@link #requestBlocks(Peer, GetBlocksDto)} with retry. */
    public reactor.core.publisher.Mono<BlocksDto> requestBlocksReactive(Peer peer, GetBlocksDto req, long timeoutMs) {
        return reactor.core.publisher.Mono.fromCallable(() -> requestBlocks(peer, req))
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
                .timeout(java.time.Duration.ofMillis(timeoutMs))
                .retryWhen(reactor.util.retry.Retry.backoff(3, java.time.Duration.ofMillis(200))
                        .maxBackoff(java.time.Duration.ofSeconds(5)))
                .onErrorResume(ex -> reactor.core.publisher.Mono.just(new BlocksDto(java.util.List.of())));
    }

    /** Handles peer discovery messages (FIND_NODE/NODES). */
    class ControlHandler extends SimpleClientHandler {
        @Override
        public void messageReceived(ChannelHandlerContext ctx, ByteBuf msg) {
            String pid = ctx.channel() != null && ctx.channel().remoteAddress() != null ?
                    ctx.channel().remoteAddress().toString() : "unknown";
            if (!rateLimiter.allow(pid)) {
                ctx.close();
                return;
            }
            try {
                if (msg.readableBytes() < 4) return;
                msg.markReaderIndex();
                int len = msg.readIntLE();
                if (msg.readableBytes() < len) {
                    msg.resetReaderIndex();
                    return;
                }
                if (len > 1_000_000) {
                    log.warn("libp2p inbound failed: length {} exceeds limit", len);
                    ctx.close();
                    return;
                }
                byte[] data = new byte[len];
                msg.readBytes(data);
                P2PMessage pm = P2PMessage.parseFrom(data);
                if (!pm.getJwt().isBlank() && props.getJwtSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8).length >= 32) {
                    try {
                        Jwts.parserBuilder()
                                .setSigningKey(Keys.hmacShaKeyFor(
                                        props.getJwtSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                                .build()
                                .parseClaimsJws(pm.getJwt());
                    } catch (Exception ex) {
                        ctx.close();
                        return;
                    }
                }
                P2PMessageDto dto = P2PProtoMapper.fromProto(pm);

                if (dto instanceof HandshakeDto hs) {
                    if (!PROTOCOL_VERSION.equals(hs.protocolVersion())) {
                        ctx.close();
                        return;
                    }
                    if (ctx.channel().remoteAddress() instanceof java.net.InetSocketAddress isa) {
                        String host = isa.getAddress().getHostAddress();
                        kademlia.store(new Peer(host, hs.listenPort(), hs.peerId()));
                    }
                } else if (dto instanceof FindNodeDto find) {
                    var nearest = kademlia.closest(find.nodeId(), 16)
                            .stream().map(Peer::toString).toList();
                    P2PMessage out = P2PProtoMapper.toProto(new NodesDto(nearest));
                    if (props.getJwtSecret() != null && props.getJwtSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8).length >= 32) {
                        String jwt = Jwts.builder()
                                .signWith(Keys.hmacShaKeyFor(
                                        props.getJwtSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                                .compact();
                        out = out.toBuilder().setJwt(jwt).build();
                    }
                    byte[] p = out.toByteArray();
                    ByteBuffer b = ByteBuffer.allocate(4 + p.length).order(java.nio.ByteOrder.LITTLE_ENDIAN);
                    b.putInt(p.length).put(p).flip();
                    ctx.writeAndFlush(io.netty.buffer.Unpooled.wrappedBuffer(b.array()));
                } else if (dto instanceof NodesDto nodes) {
                    kademlia.merge(nodes);
                } else if (dto instanceof PeerListDto pl) {
                    kademlia.merge(new NodesDto(pl.peers()));
                } else if (dto instanceof GetBlocksDto gb) {
                    var blocks = node.blocksFromHeight(gb.fromHeight());
                    var list = blocks.stream().map(blockchain.core.serialization.JsonUtils::toJson).toList();
                    P2PMessage out = P2PProtoMapper.toProto(new BlocksDto(list));
                    if (props.getJwtSecret() != null && props.getJwtSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8).length >= 32) {
                        String jwt = Jwts.builder()
                                .signWith(Keys.hmacShaKeyFor(
                                        props.getJwtSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                                .compact();
                        out = out.toBuilder().setJwt(jwt).build();
                    }
                    byte[] p = out.toByteArray();
                    ByteBuffer b = ByteBuffer.allocate(4 + p.length).order(java.nio.ByteOrder.LITTLE_ENDIAN);
                    b.putInt(p.length).put(p).flip();
                    ctx.writeAndFlush(io.netty.buffer.Unpooled.wrappedBuffer(b.array()));
                } else if (dto instanceof BlocksDto bd) {
                    for (String raw : bd.rawBlocks()) {
                        blockchain.core.model.Block blk = blockchain.core.serialization.JsonUtils.blockFromJson(raw);
                        node.acceptExternalBlock(blk);
                    }
                }
            } catch (Exception e) {
                log.warn("libp2p inbound failed: {}", e.getMessage());
            }
        }
    }

    private void send(Peer peer, java.util.List<String> protocol, P2PMessageDto dto) {
        try {
            P2PMessage msg = P2PProtoMapper.toProto(dto);
            if (props.getJwtSecret() != null && props.getJwtSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8).length >= 32) {
                String jwt = Jwts.builder()
                        .signWith(Keys.hmacShaKeyFor(
                                props.getJwtSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                        .compact();
                msg = msg.toBuilder().setJwt(jwt).build();
            }
            byte[] payload = msg.toByteArray();
            ByteBuffer buf = ByteBuffer.allocate(4 + payload.length).order(java.nio.ByteOrder.LITTLE_ENDIAN);
            buf.putInt(payload.length).put(payload).flip();
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
            byte[] data = buf.array();
            fut.thenAccept(s -> s.writeAndFlush(io.netty.buffer.Unpooled.wrappedBuffer(data))).join();
        } catch (Exception e) {
            log.warn("libp2p send failed", e);
        }
    }

    private class BlockHandler extends JsonHandler<NewBlockDto> {
        BlockHandler() { super(NewBlockDto.class); }

        @Override
        protected void handle(NewBlockDto dto) throws Exception {
            blockchain.core.model.Block blk = blockchain.core.serialization.JsonUtils.blockFromJson(dto.rawBlockJson());
            node.acceptExternalBlock(blk);
        }
    }

    private class TxHandler extends JsonHandler<NewTxDto> {
        TxHandler() { super(NewTxDto.class); }

        @Override
        protected void handle(NewTxDto dto) throws Exception {
            blockchain.core.model.Transaction tx = blockchain.core.serialization.JsonUtils.txFromJson(dto.rawTxJson());
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
            String pid = ctx.channel() != null && ctx.channel().remoteAddress() != null ?
                    ctx.channel().remoteAddress().toString() : "unknown";
            if (!rateLimiter.allow(pid)) {
                ctx.close();
                return;
            }
            try {
                if (msg.readableBytes() < 4) return;
                msg.markReaderIndex();
                int len = msg.readIntLE();
                if (msg.readableBytes() < len) {
                    msg.resetReaderIndex();
                    return;
                }
                if (len > 1_000_000) {
                    log.warn("libp2p inbound failed: length {} exceeds limit", len);
                    ctx.close();
                    return;
                }
                byte[] data = new byte[len];
                msg.readBytes(data);
                P2PMessage pm = P2PMessage.parseFrom(data);
                if (!pm.getJwt().isBlank() && props.getJwtSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8).length >= 32) {
                    try {
                        Jwts.parserBuilder()
                                .setSigningKey(Keys.hmacShaKeyFor(
                                        props.getJwtSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                                .build()
                                .parseClaimsJws(pm.getJwt());
                    } catch (Exception ex) {
                        ctx.close();
                        return;
                    }
                }
                P2PMessageDto base = P2PProtoMapper.fromProto(pm);
                @SuppressWarnings("unchecked")
                T dto = (T) base;
                handle(dto);
            } catch (Exception e) {
                log.warn("libp2p inbound failed: {}", e.getMessage());
            }
        }

        protected abstract void handle(T dto) throws Exception;
    }

    /** Per-peer token bucket rate limiter. */
    static class PeerRateLimiter {
        private final int capacity;
        private final double refillPerSec;
        private final java.util.Map<String, TokenBucket> buckets =
                new java.util.concurrent.ConcurrentHashMap<>();

        PeerRateLimiter(int capacity, double refillPerSec) {
            this.capacity = capacity;
            this.refillPerSec = refillPerSec;
        }

        boolean allow(String peerId) {
            return buckets.computeIfAbsent(peerId,
                    k -> new TokenBucket(capacity, refillPerSec)).allow();
        }

        private static class TokenBucket {
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
}
