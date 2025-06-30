package de.flashyotter.blockchain_node.service;

import java.net.URI;
import java.time.Duration;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import blockchain.core.consensus.Chain;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import de.flashyotter.blockchain_node.dto.HandshakeDto;
import de.flashyotter.blockchain_node.dto.NewBlockDto;
import de.flashyotter.blockchain_node.dto.NewTxDto;
import de.flashyotter.blockchain_node.dto.P2PMessageDto;
import de.flashyotter.blockchain_node.dto.GetBlocksDto;
import de.flashyotter.blockchain_node.dto.BlocksDto;
import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.p2p.Peer;
import de.flashyotter.blockchain_node.service.PeerRegistry;
import de.flashyotter.blockchain_node.discovery.PeerDiscoveryService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Service
@RequiredArgsConstructor @Slf4j
public class SyncService {

    private final Chain                       chain;
    private final NodeService                 node;
    private final ObjectMapper                mapper;
    private final ReactorNettyWebSocketClient wsClient;
    private final NodeProperties              props;
    private final PeerRegistry               registry;
    private final de.flashyotter.blockchain_node.discovery.PeerDiscoveryService discovery;

    /** Dauerhafte Block-Synchro mit automatischem Re-Connect */
    public Flux<Void> followPeer(Peer peer) {
        String wsUrl = peer.wsUrl();

        return Flux.defer(() ->
                wsClient.execute(URI.create(wsUrl),
                                 s -> {
                                     // send our handshake first
                                     HandshakeDto hello = new HandshakeDto(
                                             props.getId(),
                                             "0.4.0");
                                     Mono<Void> snd = s.send(Mono.just(
                                             s.textMessage(toJson(hello))));

                                     Mono<Void> rcv = s.receive()
                                             .map(fr -> fr.getPayloadAsText())
                                             .map(this::toDto)
                                             .handle((dto, sink) -> {
                                                 if (dto instanceof HandshakeDto hs) {
                                                     registry.add(peer);
                                                     discovery.onMessage(hs, peer);
                                                     GetBlocksDto req = new GetBlocksDto(
                                                             node.latestBlock().getHeight());
                                                     s.send(Mono.just(s.textMessage(toJson(req)))).subscribe();
                                                 }
                                                 if (dto instanceof BlocksDto bl) {
                                                     bl.rawBlocks().stream()
                                                       .map(this::toBlock)
                                                       .forEach(node::acceptExternalBlock);
                                                 }
                                                 if (dto instanceof NewBlockDto nb) {
                                                     sink.next(toBlock(nb));
                                                 }
                                                 if (dto instanceof NewTxDto nt) {
                                                     node.acceptExternalTx(toTx(nt));
                                                 }
                                             })
                                             .cast(Block.class)
                                             .doOnNext(node::acceptExternalBlock)
                                             .then();

                                     return snd.then(rcv);
                                 }))
            .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(5)))
            .doOnError(e -> log.warn("sync({}) – {}", wsUrl, e.getMessage()));
    }

    /* helper */
    @SneakyThrows private String       toJson(Object o){ return mapper.writeValueAsString(o); }
    @SneakyThrows private P2PMessageDto toDto(String j){ return mapper.readValue(j, P2PMessageDto.class); }
    @SneakyThrows private Block         toBlock(NewBlockDto d){ return mapper.readValue(d.rawBlockJson(), Block.class); }
    @SneakyThrows private Block         toBlock(String json){ return mapper.readValue(json, Block.class); }
    @SneakyThrows private Transaction   toTx(NewTxDto d){ return mapper.readValue(d.rawTxJson(), Transaction.class); }
}
