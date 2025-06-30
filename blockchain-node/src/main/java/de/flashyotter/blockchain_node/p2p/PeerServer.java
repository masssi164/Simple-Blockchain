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
        HandshakeDto hello = new HandshakeDto(props.getId(), PROTOCOL_VER);
        InetSocketAddress addr = session.getHandshakeInfo().getRemoteAddress();
        Peer peer = new Peer(addr.getHostString(), addr.getPort());

        ConnectionManager.Conn conn = connectionManager.registerServerSession(peer, session);

        conn.inbound().subscribe(dto -> {
            if (dto instanceof HandshakeDto) {
                boolean fresh = registry.add(peer);
                broadcast.broadcastPeerList();
                discovery.onMessage(dto, peer);
                if (fresh) {
                    syncService.followPeer(peer).subscribe();
                }
                return;
            }

            if (dto instanceof PeerListDto pl) {
                List<Peer> peers = pl.peers().stream().map(Peer::fromString).toList();
                registry.addAll(peers);
                return;
            }

            if (dto instanceof PingDto || dto instanceof PongDto ||
                dto instanceof FindNodeDto || dto instanceof NodesDto) {
                discovery.onMessage(dto, peer);
            }
        });

        return session.send(Mono.just(session.textMessage(mapper.writeValueAsString(hello))))
                      .then(session.closeStatus())
                      .then();
    }
}
