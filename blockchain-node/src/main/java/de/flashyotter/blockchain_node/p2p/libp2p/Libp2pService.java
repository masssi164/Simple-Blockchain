package de.flashyotter.blockchain_node.p2p.libp2p;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.dto.P2PMessageDto;
import de.flashyotter.blockchain_node.p2p.Peer;
import io.libp2p.core.Host;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class Libp2pService {

    private static final java.util.List<String> PROTOCOL = java.util.List.of("/simple-blockchain/1.0.0");

    private final Host           host;
    private final NodeProperties props;
    private final ObjectMapper   mapper;

    @PostConstruct
    public void init() {
        host.listenAddresses().forEach(a -> log.info("libp2p listening on {}", a));
    }

    public void broadcast(java.util.Collection<Peer> peers, P2PMessageDto dto) {
        peers.forEach(p -> send(p, dto));
    }

    public void send(Peer peer, P2PMessageDto dto) {
        try {
            String json = mapper.writeValueAsString(dto);
            io.libp2p.core.multiformats.Multiaddr addr =
                    new io.libp2p.core.multiformats.Multiaddr("/ip4/" + peer.getHost() + "/tcp/" + peer.getPort());
            host.getNetwork().connect(addr)
                .thenCompose(conn -> host.newStream(PROTOCOL, conn).getStream())
                .thenAccept(s -> s.writeAndFlush(json)).join();
        } catch (Exception e) {
            log.warn("libp2p send failed: {}", e.getMessage());
        }
    }
}
