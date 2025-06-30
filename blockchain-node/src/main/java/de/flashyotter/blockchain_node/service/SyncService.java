package de.flashyotter.blockchain_node.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import de.flashyotter.blockchain_node.dto.BlocksDto;
import de.flashyotter.blockchain_node.dto.GetBlocksDto;
import de.flashyotter.blockchain_node.dto.HandshakeDto;
import de.flashyotter.blockchain_node.dto.NewBlockDto;
import de.flashyotter.blockchain_node.dto.NewTxDto;
import de.flashyotter.blockchain_node.p2p.ConnectionManager;
import de.flashyotter.blockchain_node.p2p.Peer;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor @Slf4j
public class SyncService {

    private final NodeService       node;
    private final ObjectMapper      mapper;
    private final ConnectionManager manager;

    /** Dauerhafte Block-Synchro mit automatischem Re-Connect */
    public Flux<Void> followPeer(Peer peer) {

        ConnectionManager.Conn conn = manager.connectAndSink(peer);

        return conn.inbound()
            .concatMap(dto -> {
                if (dto instanceof HandshakeDto) {
                    requestBlocks(conn);
                }
                if (dto instanceof BlocksDto blks) {
                    for (String raw : blks.rawBlocks()) {
                        node.acceptExternalBlock(toBlock(raw));
                    }
                    if (!blks.rawBlocks().isEmpty()) {
                        requestBlocks(conn);
                    }
                }
                if (dto instanceof NewBlockDto nb) {
                    node.acceptExternalBlock(toBlock(nb));
                }
                if (dto instanceof NewTxDto nt) {
                    node.acceptExternalTx(toTx(nt));
                }
                return Mono.empty();
            });
    }

    /* helper */
    @SneakyThrows private Block         toBlock(NewBlockDto d){ return mapper.readValue(d.rawBlockJson(), Block.class); }
    @SneakyThrows private Block         toBlock(String raw)  { return mapper.readValue(raw, Block.class); }
    @SneakyThrows private Transaction   toTx(NewTxDto d){ return mapper.readValue(d.rawTxJson(), Transaction.class); }

    @SneakyThrows
    private void requestBlocks(ConnectionManager.Conn conn) {
        int height = node.latestBlock().getHeight();
        String json = mapper.writeValueAsString(new GetBlocksDto(height));
        conn.outbound().tryEmitNext(json);
    }
}
