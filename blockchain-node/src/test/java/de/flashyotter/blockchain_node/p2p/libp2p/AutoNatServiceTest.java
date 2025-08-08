package de.flashyotter.blockchain_node.p2p.libp2p;

import de.flashyotter.blockchain_node.p2p.libp2p.AutoNatService;
import io.libp2p.core.Host;
import io.libp2p.core.PeerId;
import io.libp2p.core.multiformats.Multiaddr;
import io.libp2p.protocol.autonat.AutonatProtocol;
import io.libp2p.protocol.autonat.pb.Autonat;
import com.google.protobuf.ByteString;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AutoNatServiceTest {
    @Test
    void returnsObservedAddressFromController() throws Exception {
        Host host = mock(Host.class);
        when(host.getPeerId()).thenReturn(PeerId.random());
        Multiaddr listen = new Multiaddr("/ip4/1.1.1.1/tcp/4001");
        when(host.listenAddresses()).thenReturn(List.of(listen));

        Multiaddr observed = new Multiaddr("/ip4/9.9.9.9/tcp/4001");
        AutonatProtocol.AutoNatController ctrl = msg -> CompletableFuture.completedFuture(
                Autonat.Message.newBuilder()
                        .setType(Autonat.Message.MessageType.DIAL_RESPONSE)
                        .setDialResponse(Autonat.Message.DialResponse.newBuilder()
                                .setAddr(ByteString.copyFrom(observed.serialize()))
                                .build())
                        .build());

        AutoNatService svc = new AutoNatService(host, ctrl);
        assertEquals(observed.toString(), svc.discover());
    }
}
