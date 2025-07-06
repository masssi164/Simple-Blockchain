package de.flashyotter.blockchain_node.p2p.libp2p;

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

    private final Host host;
    private final NodeProperties props;

    @PostConstruct
    public void init() {
        host.listenAddresses().forEach(a -> log.info("libp2p listening on {}", a));
    }

    public void broadcast(P2PMessageDto dto) {
        // TODO implement real transport
    }

    public void send(Peer peer, P2PMessageDto dto) {
        // TODO implement real transport
    }
}
