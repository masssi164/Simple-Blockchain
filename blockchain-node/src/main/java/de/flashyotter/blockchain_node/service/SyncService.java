package de.flashyotter.blockchain_node.service;

import org.springframework.stereotype.Service;

import de.flashyotter.blockchain_node.p2p.libp2p.Libp2pService;
import de.flashyotter.blockchain_node.p2p.Peer;
import de.flashyotter.blockchain_node.dto.BlocksDto;
import de.flashyotter.blockchain_node.dto.GetBlocksDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor @Slf4j
public class SyncService {

    private final NodeService  node;
    private final Libp2pService libp2p;
    private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER = new com.fasterxml.jackson.databind.ObjectMapper()
            .registerModule(new com.fasterxml.jackson.module.paramnames.ParameterNamesModule())
            .addMixIn(blockchain.core.model.Block.class, BlockMixIn.class)
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .findAndRegisterModules();

    private abstract static class BlockMixIn {
        @com.fasterxml.jackson.annotation.JsonCreator
        BlockMixIn(@com.fasterxml.jackson.annotation.JsonProperty("height") int height,
                   @com.fasterxml.jackson.annotation.JsonProperty("prevHashHex") String prev,
                   @com.fasterxml.jackson.annotation.JsonProperty("txs") java.util.List<blockchain.core.model.Transaction> txs,
                   @com.fasterxml.jackson.annotation.JsonProperty("compactBits") int bits,
                   @com.fasterxml.jackson.annotation.JsonProperty("fixedTimeMillis") long time,
                   @com.fasterxml.jackson.annotation.JsonProperty("fixedNonce") int nonce) {}
    }

    /**
     * Fetch blocks from {@code peer} until no newer blocks are returned.
     * Each batch is requested via {@link GetBlocksDto} and processed as a
     * {@link BlocksDto} reply.
     */
    public Flux<Void> followPeer(Peer peer) {
        return Flux.create(sink -> {
            int height = node.latestBlock().getHeight();
            while (true) {
                BlocksDto resp = libp2p.requestBlocks(peer, new GetBlocksDto(height));
                if (resp.rawBlocks().isEmpty()) {
                    sink.complete();
                    break;
                }
                resp.rawBlocks().forEach(raw -> {
                    try {
                        blockchain.core.model.Block blk = MAPPER.readValue(raw, blockchain.core.model.Block.class);
                        node.acceptExternalBlock(blk);
                    } catch (Exception e) {
                        log.warn("failed to parse block from peer: {}", e.getMessage());
                    }
                });
                height = node.latestBlock().getHeight();
            }
        });
    }

}
