package de.flashyotter.blockchain_node.service;

import org.springframework.stereotype.Service;

import de.flashyotter.blockchain_node.dto.NewBlockDto;
import de.flashyotter.blockchain_node.dto.NewTxDto;
import de.flashyotter.blockchain_node.dto.P2PMessageDto;
import de.flashyotter.blockchain_node.dto.PeerListDto;
import de.flashyotter.blockchain_node.p2p.Peer;
import de.flashyotter.blockchain_node.p2p.libp2p.Libp2pService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import de.flashyotter.blockchain_node.config.MetricsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Fan-out helper that sends JSON DTOs to every known peer. */
@Service
@RequiredArgsConstructor
@Slf4j
public class P2PBroadcastService implements P2PBroadcastPort {

    private final PeerRegistry   registry;
    private final Libp2pService  libp2p;
    private final MeterRegistry  metrics;

    /* ------------------------------------------------------------------ */
    /* interface implementation                                           */
    /* ------------------------------------------------------------------ */

    @Override
    public void broadcastTx(NewTxDto dto, Peer origin) {
        fanOut(dto, origin);
    }

    @Override
    public void broadcastBlock(NewBlockDto dto, Peer origin) {
        Timer.Sample sample = Timer.start(metrics);
        fanOut(dto, origin);
        long nanos = sample.stop(metrics.timer(MetricsConfig.BLOCK_BROADCAST_TIME));
        int count = (int) registry.all().stream()
                                  .filter(p -> origin == null || !p.equals(origin))
                                  .count();
        log.info("Broadcasted block to {} peers in {} ms", count, nanos / 1_000_000);
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
                    if (dto instanceof NewBlockDto nb) {
                        libp2p.sendBlock(p, nb);
                    } else if (dto instanceof NewTxDto nt) {
                        libp2p.sendTx(p, nt);
                    } else {
                        libp2p.send(p, dto);
                    }
                });
    }
}
