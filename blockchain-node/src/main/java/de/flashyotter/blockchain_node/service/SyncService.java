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
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

@Service
@RequiredArgsConstructor @Slf4j
public class SyncService {

    private final Chain                       chain;
    private final ObjectMapper                mapper;
    private final ReactorNettyWebSocketClient wsClient;

    /** Dauerhafte Block-Synchro mit automatischem Re-Connect */
    public Flux<Void> followPeer(String wsUrl) {

        return Flux.defer(() ->
                wsClient.execute(URI.create(wsUrl),
                                 s -> s.receive()
                                       .map(fr -> fr.getPayloadAsText())
                                       .map(this::toNewBlockDto)
                                       .map(this::toBlock)
                                       .doOnNext(chain::addBlock)
                                       .then()))
            .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(5)))
            .doOnError(e -> log.warn("sync({}) – {}", wsUrl, e.getMessage()));
    }

    /* helper */
    @SneakyThrows private NewBlockDto toNewBlockDto(String j){ return mapper.readValue(j,NewBlockDto.class);}
    @SneakyThrows private Block       toBlock(NewBlockDto d){ return mapper.readValue(d.rawBlockJson(), Block.class);}
}
