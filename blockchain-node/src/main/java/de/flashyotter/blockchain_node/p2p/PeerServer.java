package de.flashyotter.blockchain_node.p2p;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.discovery.FindNodeDto;
import de.flashyotter.blockchain_node.discovery.NodesDto;
import de.flashyotter.blockchain_node.discovery.PeerDiscoveryService;
import de.flashyotter.blockchain_node.discovery.PingDto;
import de.flashyotter.blockchain_node.discovery.PongDto;
import de.flashyotter.blockchain_node.dto.*;
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_node.service.P2PBroadcastService;
import de.flashyotter.blockchain_node.service.PeerRegistry;
import de.flashyotter.blockchain_node.service.SyncService;
import blockchain.core.model.Block;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PeerServer implements WebSocketHandler {

    private final ObjectMapper        mapper;
    private final NodeService         node;
    private final PeerRegistry        registry;
    private final P2PBroadcastService broadcast;
    private final NodeProperties      props;
    private final PeerDiscoveryService discovery;
    private final ConnectionManager   connectionManager;
    private final SyncService         syncService;

    private static final String PROTOCOL_VER = "0.4.0";

    @Override
    @SneakyThrows
    public Mono<Void> handle(WebSocketSession session) {
        HandshakeDto hello = new HandshakeDto(props.getId(), PROTOCOL_VER, props.getPort());
        InetSocketAddress addr = session.getHandshakeInfo().getRemoteAddress();
        String host = addr.getHostString();
        Peer peer = new Peer(host, addr.getPort());
        java.util.concurrent.atomic.AtomicReference<Peer> actual = new java.util.concurrent.atomic.AtomicReference<>(peer);

        ConnectionManager.Conn conn = connectionManager.registerServerSession(peer, session);
        conn.outbound().tryEmitNext(mapper.writeValueAsString(hello));

        conn.inbound().subscribe(dto -> {
            if (dto instanceof HandshakeDto hs) {
                Peer real = new Peer(host, hs.listenPort());
                actual.set(real);
                connectionManager.remap(peer, real);
                boolean fresh = registry.add(real);
                broadcast.broadcastPeerList();
                discovery.onMessage(hs, real);
                if (fresh) {
                    syncService.followPeer(real).subscribe();
                }
                return;
            }

            if (dto instanceof PeerListDto pl) {
                List<Peer> peers = pl.peers().stream().map(Peer::fromString).toList();
                registry.addAll(peers);
                return;
            }

            if (dto instanceof GetBlocksDto req) {
                List<String> raws = node.blocksFromHeight(req.fromHeight()).stream()
                                     .map(b -> {
                                         try { return mapper.writeValueAsString(b); }
                                         catch (Exception e) { throw new RuntimeException(e); }
                                     })
                                     .toList();
                try {
                    String json = mapper.writeValueAsString(new BlocksDto(raws));
                    conn.outbound().tryEmitNext(json);
                } catch (Exception e) {
                    log.warn("❌  failed to send blocks: {}", e.getMessage());
                }
                return;
            }

            if (dto instanceof BlocksDto blks) {
                blks.rawBlocks().forEach(raw -> {
                    try {
                        Block b = mapper.readValue(raw, Block.class);
                        node.acceptExternalBlock(b);
                    } catch (Exception e) {
                        log.warn("❌  failed to process block: {}", e.getMessage());
                    }
                });
                return;
            }

            if (dto instanceof PingDto || dto instanceof PongDto ||
                dto instanceof FindNodeDto || dto instanceof NodesDto) {
                discovery.onMessage(dto, actual.get());
            }
        });

        return session.closeStatus().then();
    }
}
