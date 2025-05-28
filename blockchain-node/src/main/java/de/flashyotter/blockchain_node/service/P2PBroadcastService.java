package de.flashyotter.blockchain_node.service;

import org.springframework.stereotype.Service;

import de.flashyotter.blockchain_node.dto.NewBlockDto;
import de.flashyotter.blockchain_node.dto.NewTxDto;
import de.flashyotter.blockchain_node.dto.P2PMessageDto;
import de.flashyotter.blockchain_node.dto.PeerListDto;
import de.flashyotter.blockchain_node.p2p.Peer;
import de.flashyotter.blockchain_node.p2p.PeerClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Fan-out helper that sends JSON DTOs to every known peer. */
@Service
@RequiredArgsConstructor
@Slf4j
public class P2PBroadcastService implements P2PBroadcastPort {

    private final PeerRegistry registry;
    private final PeerClient   client;

    /* ------------------------------------------------------------------ */
    /* interface implementation                                           */
    /* ------------------------------------------------------------------ */

    @Override
    public void broadcastTx(NewTxDto dto, Peer origin) {
        fanOut(dto, origin);
    }

    @Override
    public void broadcastBlock(NewBlockDto dto, Peer origin) {
        fanOut(dto, origin);
    }

    @Override
    public void broadcastPeerList() {
        PeerListDto list = new PeerListDto(
                registry.all().stream().map(Peer::toString).toList());
        fanOut(list, null);
    }

    /* ------------------------------------------------------------------ */
    /* internal helper                                                    */
    /* ------------------------------------------------------------------ */

    private void fanOut(P2PMessageDto dto, Peer origin) {
        registry.all().stream()
                .filter(p -> origin == null || !p.equals(origin)) // avoid echo
                .forEach(p -> {
                    try { client.send(p, dto); }
                    catch (Exception e) {
                        log.warn("❌  send to {} failed: {}", p, e.getMessage());
                    }
                });
    }
}
