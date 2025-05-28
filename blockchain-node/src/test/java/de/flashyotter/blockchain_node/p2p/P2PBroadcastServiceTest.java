// P2PBroadcastServiceTest.java
package de.flashyotter.blockchain_node.p2p;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.flashyotter.blockchain_node.dto.NewBlockDto;
import de.flashyotter.blockchain_node.dto.NewTxDto;
import de.flashyotter.blockchain_node.dto.PeerListDto;
import de.flashyotter.blockchain_node.service.P2PBroadcastService;
import de.flashyotter.blockchain_node.service.PeerRegistry;

class P2PBroadcastServiceTest {

    @Mock PeerRegistry registry;
    @Mock PeerClient   client;

    P2PBroadcastService svc;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        svc = new P2PBroadcastService(registry, client);
    }

    @Test
    void broadcastTxFanOut() {
        var p1 = new Peer("h1",1);
        var p2 = new Peer("h2",2);
        when(registry.all()).thenReturn(java.util.Set.of(p1,p2));
        NewTxDto dto = new NewTxDto("json");
        svc.broadcastTx(dto, p1);
        // p1 origin should be skipped, only p2
        verify(client).send(eq(p2), eq(dto));
        verify(client, never()).send(eq(p1), any());
    }

    @Test
    void broadcastBlockFanOutNoOrigin() {
        var p1 = new Peer("h1",1);
        when(registry.all()).thenReturn(java.util.Set.of(p1));
        NewBlockDto dto = new NewBlockDto("jb");
        svc.broadcastBlock(dto, null);
        verify(client).send(eq(p1), eq(dto));
    }

    @Test
    void broadcastPeerListSendsAll() {
        var p1 = new Peer("a",3);
        var p2 = new Peer("b",4);
        when(registry.all()).thenReturn(java.util.Set.of(p1,p2));
        svc.broadcastPeerList();
        // building a PeerListDto internally, so just verify send called twice
        verify(client, times(2)).send(any(Peer.class), any(PeerListDto.class));
    }
}
