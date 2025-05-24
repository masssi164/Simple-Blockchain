package de.flashyotter.blockchain_node.service;

import de.flashyotter.blockchain_node.dto.*;
import de.flashyotter.blockchain_node.p2p.Peer;
import de.flashyotter.blockchain_node.p2p.PeerClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Sends JSON-encoded P2P messages to every known peer except an optional origin.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class P2PBroadcastService {

    private final PeerRegistry registry;
    private final PeerClient   client;

    /* ---- high-level helpers ----------------------------------------- */

    public void broadcastTx(NewTxDto dto, Peer origin) {
        fanOut(dto, origin);
    }

    public void broadcastBlock(NewBlockDto dto, Peer origin) {
        fanOut(dto, origin);
    }

    public void broadcastPeerList() {
        var dto = new PeerListDto(
                registry.all().stream().map(p -> p.getHost() + ':' + p.getPort()).toList());
        fanOut(dto, null);
    }

    /* ---- internal ---------------------------------------------------- */

    private void fanOut(P2PMessageDto dto, Peer origin) {
        registry.all().stream()
                .filter(p -> origin == null || !p.equals(origin))   // skip echo
                .forEach(p -> {
                    try { client.send(p, dto); }
                    catch (Exception e) { log.warn("send to {} failed: {}", p, e.getMessage()); }
                });
    }
}
