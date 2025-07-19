package de.flashyotter.blockchain_node.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.when;

import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import de.flashyotter.blockchain_node.dto.BlocksDto;
import de.flashyotter.blockchain_node.dto.GetBlocksDto;
import de.flashyotter.blockchain_node.p2p.Peer;
import de.flashyotter.blockchain_node.p2p.libp2p.Libp2pService;
import de.flashyotter.blockchain_node.config.NodeProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.fasterxml.jackson.databind.ObjectMapper;
import blockchain.core.serialization.JsonUtils;

import java.util.List;

public class SyncServiceTest {

    @Mock
    NodeService node;
    @Mock
    Libp2pService libp2p;
    SyncService svc;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        JsonUtils.use(mapper);
        when(node.getProps()).thenReturn(new NodeProperties());
        svc = new SyncService(node, libp2p);
    }

    @Test
    void followPeerRequestsUntilEmpty() {
        Peer peer = new Peer("h", 1);
        Block genesis = new Block(0, "g", List.of(new Transaction()), 0);
        Block b1 = new Block(1, "h1", List.of(new Transaction()), 0);
        Block b2 = new Block(2, "h2", List.of(new Transaction()), 0);

        when(node.latestBlock()).thenReturn(genesis, b2, b2);
        String j1 = JsonUtils.toJson(b1);
        String j2 = JsonUtils.toJson(b2);
        when(libp2p.requestBlocksReactive(eq(peer), any(GetBlocksDto.class), anyLong()))
                .thenReturn(reactor.core.publisher.Mono.just(new BlocksDto(List.of(j1, j2))))
                .thenReturn(reactor.core.publisher.Mono.just(new BlocksDto(List.of())));

        svc.followPeer(peer).collectList().block();

        verify(libp2p, times(2)).requestBlocksReactive(eq(peer), any(GetBlocksDto.class), anyLong());
        verify(node, times(2)).acceptExternalBlock(any(Block.class));
    }

    @Test
    void followPeerHandlesTimeout() {
        Peer peer = new Peer("h", 1);
        Block genesis = new Block(0, "g", List.of(new Transaction()), 0);
        when(node.latestBlock()).thenReturn(genesis);
        when(libp2p.requestBlocksReactive(eq(peer), any(GetBlocksDto.class), anyLong()))
                .thenReturn(reactor.core.publisher.Mono.error(new RuntimeException("timeout")));

        svc.followPeer(peer).collectList().block();

        verify(node, atLeastOnce()).latestBlock();
    }

    @Test
    void malformedBlocksAreIgnored() {
        Peer peer = new Peer("h", 1);
        Block genesis = new Block(0, "g", List.of(new Transaction()), 0);
        when(node.latestBlock()).thenReturn(genesis, genesis);
        when(libp2p.requestBlocksReactive(eq(peer), any(GetBlocksDto.class), anyLong()))
                .thenReturn(reactor.core.publisher.Mono.just(new BlocksDto(List.of("{bad}"))))
                .thenReturn(reactor.core.publisher.Mono.just(new BlocksDto(List.of())));

        svc.followPeer(peer).collectList().block();

        verify(node, atLeastOnce()).latestBlock();
    }
}
