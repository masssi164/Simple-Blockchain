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
    /** Track peers currently being synced to avoid duplicate loops. */
    private final java.util.Set<Peer> syncing =
            java.util.concurrent.ConcurrentHashMap.newKeySet();
    private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER =
            new com.fasterxml.jackson.databind.ObjectMapper()
                    .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .findAndRegisterModules();

    /**
     * Fetch blocks from {@code peer} until no newer blocks are returned.
     * Each batch is requested via {@link GetBlocksDto} and processed as a
     * {@link BlocksDto} reply.
     */
    public Flux<Void> followPeer(Peer peer) {
        return Flux.defer(() -> {
            if (!syncing.add(peer)) {
                return Flux.empty();
            }
            return fetchLoop(peer, node.latestBlock().getHeight())
                    .doFinally(sig -> syncing.remove(peer));
        });
    }

    private Flux<Void> fetchLoop(Peer peer, int fromHeight) {
        return libp2p.requestBlocksReactive(peer, new GetBlocksDto(fromHeight), node.getProps().getSyncTimeoutMs())
                .flatMapMany(dto -> {
                    if (dto.rawBlocks().isEmpty()) {
                        return Flux.empty();
                    }
                    return Flux.fromIterable(dto.rawBlocks())
                            .doOnNext(raw -> {
                                try {
                                    blockchain.core.model.Block blk = MAPPER.readValue(raw, blockchain.core.model.Block.class);
                                    node.acceptExternalBlock(blk);
                                } catch (Exception e) {
                                    log.warn("failed to parse block from peer: {}", e.getMessage());
                                }
                            })
                            .thenMany(fetchLoop(peer, node.latestBlock().getHeight()));
                })
                .onErrorResume(e -> Flux.empty());
    }

}
