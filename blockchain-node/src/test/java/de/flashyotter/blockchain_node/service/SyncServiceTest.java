package de.flashyotter.blockchain_node.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.flashyotter.blockchain_node.dto.BlocksDto;
import de.flashyotter.blockchain_node.dto.GetBlocksDto;
import de.flashyotter.blockchain_node.dto.HandshakeDto;
import de.flashyotter.blockchain_node.p2p.ConnectionManager;
import de.flashyotter.blockchain_node.p2p.Peer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Sinks;

import blockchain.core.model.Block;

class SyncServiceTest {

    @Mock NodeService node;
    @Mock ObjectMapper mapper;
    @Mock ConnectionManager manager;

    Sinks.Many<String> out;
    Sinks.Many<de.flashyotter.blockchain_node.dto.P2PMessageDto> inSink;

    SyncService svc;
    Peer peer = new Peer("h", 1);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        out = Sinks.many().unicast().onBackpressureBuffer();
        inSink = Sinks.many().unicast().onBackpressureBuffer();
        when(manager.connectAndSink(any())).thenReturn(new ConnectionManager.Conn(out, inSink, inSink.asFlux()));
        svc = new SyncService(node, mapper, manager);
    }

    @Test
    void handshakeTriggersBlockRequest() throws Exception {
        Block tip = mock(Block.class);
        when(tip.getHeight()).thenReturn(5);
        when(node.latestBlock()).thenReturn(tip);
        when(mapper.writeValueAsString(any(GetBlocksDto.class)))
                .thenAnswer(inv -> {
                    GetBlocksDto d = inv.getArgument(0);
                    return "get-" + d.fromHeight();
                });

        List<String> sent = new CopyOnWriteArrayList<>();
        out.asFlux().subscribe(sent::add);
        svc.followPeer(peer).subscribe();

        inSink.tryEmitNext(new HandshakeDto("a","0"));

        Awaitility.await().until(() -> !sent.isEmpty());
        assertEquals("get-5", sent.get(0));
    }

    @Test
    void blocksTriggerFurtherRequests() throws Exception {
        Block b0 = mock(Block.class);
        when(b0.getHeight()).thenReturn(5);
        AtomicReference<Block> tip = new AtomicReference<>(b0);
        when(node.latestBlock()).thenAnswer(inv -> tip.get());
        doAnswer(inv -> { tip.set(inv.getArgument(0)); return null; }).when(node).acceptExternalBlock(any());

        Block b1 = mock(Block.class); when(b1.getHeight()).thenReturn(6);
        Block b2 = mock(Block.class); when(b2.getHeight()).thenReturn(7);
        when(mapper.readValue("b1", Block.class)).thenReturn(b1);
        when(mapper.readValue("b2", Block.class)).thenReturn(b2);
        when(mapper.writeValueAsString(any(GetBlocksDto.class)))
                .thenAnswer(inv -> "get-" + ((GetBlocksDto)inv.getArgument(0)).fromHeight());

        List<String> sent = new CopyOnWriteArrayList<>();
        out.asFlux().subscribe(sent::add);
        svc.followPeer(peer).subscribe();

        inSink.tryEmitNext(new HandshakeDto("a","0"));
        Awaitility.await().until(() -> !sent.isEmpty());
        inSink.tryEmitNext(new BlocksDto(List.of("b1","b2")));

        Awaitility.await().until(() -> sent.size() == 2);

        InOrder order = inOrder(node);
        order.verify(node, times(1)).acceptExternalBlock(b1);
        order.verify(node, times(1)).acceptExternalBlock(b2);

        assertEquals(List.of("get-5","get-7"), sent);
    }
}
