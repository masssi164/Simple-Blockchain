package de.flashyotter.blockchain_node.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import de.flashyotter.blockchain_node.dto.BlocksDto;
import de.flashyotter.blockchain_node.dto.GetBlocksDto;
import de.flashyotter.blockchain_node.p2p.Peer;
import de.flashyotter.blockchain_node.p2p.libp2p.Libp2pService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.fasterxml.jackson.databind.ObjectMapper;
import blockchain.core.serialization.JsonUtils;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import blockchain.core.model.Transaction;

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
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(blockchain.core.model.Block.class, BlockMixIn.class);
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JsonUtils.use(mapper);
        svc = new SyncService(node, libp2p);
    }

    private abstract static class BlockMixIn {
        @JsonCreator
        BlockMixIn(@JsonProperty("height") int height,
                   @JsonProperty("prevHashHex") String prev,
                   @JsonProperty("txs") java.util.List<Transaction> txs,
                   @JsonProperty("compactBits") int bits,
                   @JsonProperty("fixedTimeMillis") long time,
                   @JsonProperty("fixedNonce") int nonce) {}
    }

    @Test
    void followPeerRequestsUntilEmpty() {
        Peer peer = new Peer("h", 1);
        Block genesis = new Block(0, "g", List.of(new Transaction()), 0);
        Block b1 = new Block(1, "h1", List.of(new Transaction()), 0);
        Block b2 = new Block(2, "h2", List.of(new Transaction()), 0);

        when(node.latestBlock()).thenReturn(genesis, b2, b2);
        String j1 = "{\"height\":1,\"prevHashHex\":\"h1\",\"txs\":[],\"compactBits\":0,\"fixedTimeMillis\":0,\"fixedNonce\":0}";
        String j2 = "{\"height\":2,\"prevHashHex\":\"h2\",\"txs\":[],\"compactBits\":0,\"fixedTimeMillis\":0,\"fixedNonce\":0}";
        when(libp2p.requestBlocks(eq(peer), any(GetBlocksDto.class)))
                .thenReturn(new BlocksDto(List.of(j1, j2)), new BlocksDto(List.of()));

        svc.followPeer(peer).collectList().block();

        ArgumentCaptor<GetBlocksDto> captor = ArgumentCaptor.forClass(GetBlocksDto.class);
        verify(libp2p, times(4)).requestBlocks(eq(peer), captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(
                java.util.List.of(0, 2, 2, 2),
                captor.getAllValues().stream().map(GetBlocksDto::fromHeight).toList()
        );
        verify(node, times(2)).acceptExternalBlock(any(Block.class));
    }
}
