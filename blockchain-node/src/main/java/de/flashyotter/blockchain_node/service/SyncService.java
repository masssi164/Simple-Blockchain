package de.flashyotter.blockchain_node.service;

import java.net.URI;
import java.time.Duration;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import blockchain.core.consensus.Chain;
import blockchain.core.model.Block;
import de.flashyotter.blockchain_node.dto.NewBlockDto;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * Streams NEW_BLOCK messages from a peer and feeds them into our Chain.
 * JSON parsing errors are converted to RuntimeException to satisfy
 * the reactive pipeline without cluttering it with checked exceptions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {

    private final Chain                         chain;
    private final ObjectMapper                  mapper;
    private final ReactorNettyWebSocketClient   wsClient;

    /** Connects to {@code wsUrl} and continuously imports remote blocks. */
    public Mono<Void> followPeer(String wsUrl) {

    return wsClient.execute(URI.create(wsUrl), sess -> sess.receive()
            .map(f -> f.getPayloadAsText())
            .map(this::toNewBlockDto)
            .map(this::toBlock)
            .doOnNext(chain::addBlock)
            .then())
        .retryWhen(                             // endlos reconnect-Loop
            Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(5)))
        .doOnError(e -> log.warn("sync({}) – {}", wsUrl, e.toString()));
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
