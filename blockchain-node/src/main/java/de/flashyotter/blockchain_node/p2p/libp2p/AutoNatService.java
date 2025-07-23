package de.flashyotter.blockchain_node.p2p.libp2p;

import io.libp2p.core.Host;
import io.libp2p.core.multiformats.Multiaddr;
import io.libp2p.protocol.autonat.AutonatProtocol;

/** Simple helper for AutoNAT discovery. */
public class AutoNatService {
    private final Host host;

    public AutoNatService(Host host) {
        this.host = host;
    }

    /**
     * Performs a single AutoNAT dial and returns the observed address or null.
     */
    public String discover() {
        AutonatProtocol.AutoNatController ctrl = msg ->
                java.util.concurrent.CompletableFuture.completedFuture(
                        io.libp2p.protocol.autonat.pb.Autonat.Message.getDefaultInstance());
        try {
            var resp = ctrl.requestDial(host.getPeerId(), host.listenAddresses()).get();
            if (resp.hasAddr()) {
                return io.libp2p.core.multiformats.Multiaddr.deserialize(resp.getAddr().toByteArray()).toString();
            }
        } catch (Exception ignore) {
        }
        return null;
    }
}
