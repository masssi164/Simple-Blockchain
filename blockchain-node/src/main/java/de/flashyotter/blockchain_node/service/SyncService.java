package de.flashyotter.blockchain_node.service;

import blockchain.core.consensus.Chain;
import blockchain.core.model.Block;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.flashyotter.blockchain_node.dto.NewBlockDto;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * Streams NEW_BLOCK messages from a peer and feeds them into our Chain.
 * JSON parsing errors are converted to RuntimeException to satisfy
 * the reactive pipeline without cluttering it with checked exceptions.
 */
@Service
@RequiredArgsConstructor
public class SyncService {

    private final Chain                         chain;
    private final ObjectMapper                  mapper;
    private final ReactorNettyWebSocketClient   wsClient;

    /** Connects to {@code wsUrl} and continuously imports remote blocks. */
    public Mono<Void> followPeer(String wsUrl) {
        return wsClient.execute(
                URI.create(wsUrl),
                session -> session.receive()
                        .map(frame -> frame.getPayloadAsText())
                        .map(this::toNewBlockDto)
                        .map(this::toBlock)
                        .doOnNext(chain::addBlock)
                        .then());
    }

    /* ---------- helpers ---------- */

    @SneakyThrows
    private NewBlockDto toNewBlockDto(String json) {
        return mapper.readValue(json, NewBlockDto.class);
    }

    @SneakyThrows
    private Block toBlock(NewBlockDto dto) {
        return mapper.readValue(dto.rawBlockJson(), Block.class);
    }
}
