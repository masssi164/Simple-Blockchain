package de.flashyotter.blockchain_node.p2p.libp2p;

import io.libp2p.core.Host;
import io.libp2p.core.multiformats.Multiaddr;
import io.libp2p.protocol.autonat.AutonatProtocol;
import lombok.extern.slf4j.Slf4j;

/** Simple helper for AutoNAT discovery. */
@Slf4j
public class AutoNatService {
    private final Host host;

    public AutoNatService(Host host) {
        this.host = host;
    }

    /**
     * Performs up to three AutoNAT dials and returns the observed address or {@code null}.
     */
    public String discover() {
        AutonatProtocol.AutoNatController ctrl = msg ->
                java.util.concurrent.CompletableFuture.completedFuture(
                        io.libp2p.protocol.autonat.pb.Autonat.Message.getDefaultInstance());
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                var resp = ctrl.requestDial(host.getPeerId(), host.listenAddresses()).get();
                if (resp.hasAddr()) {
                    return Multiaddr.deserialize(resp.getAddr().toByteArray()).toString();
                }
            } catch (Exception e) {
                log.warn("AutoNAT dial attempt {} failed: {}", attempt, e.getMessage());
            }
        }
        return null;
    }
}
