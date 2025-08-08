package de.flashyotter.blockchain_node.p2p.libp2p;

import io.libp2p.core.Host;
import io.libp2p.core.multiformats.Multiaddr;
import io.libp2p.protocol.autonat.AutonatProtocol;
import io.libp2p.protocol.autonat.pb.Autonat;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

/** Simple helper for AutoNAT discovery. */
@Slf4j
public class AutoNatService {
    private final Host host;
    private final AutonatProtocol.AutoNatController controller;

    public AutoNatService(Host host) {
        this(host, msg -> CompletableFuture.completedFuture(Autonat.Message.getDefaultInstance()));
    }

    /**
     * Package-private constructor allowing tests to inject a custom controller.
     */
    AutoNatService(Host host, AutonatProtocol.AutoNatController controller) {
        this.host = host;
        this.controller = controller;
    }

    /**
     * Performs a single AutoNAT dial and returns the observed address or null.
     */
    public String discover() {
        try {
            var resp = controller.requestDial(host.getPeerId(), host.listenAddresses()).get();
            if (resp.hasAddr()) {
                return Multiaddr.deserialize(resp.getAddr().toByteArray()).toString();
            }
        } catch (Exception e) {
            log.warn("AutoNAT discovery failed: {}", e.getMessage());
        }
        return null;
    }
}
