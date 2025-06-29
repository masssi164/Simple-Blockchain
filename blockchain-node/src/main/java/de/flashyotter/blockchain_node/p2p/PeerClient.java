package de.flashyotter.blockchain_node.p2p;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.flashyotter.blockchain_node.dto.P2PMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

@Component @RequiredArgsConstructor @Slf4j
public class PeerClient {

    private final ObjectMapper                mapper;
    private final ConnectionManager           connections;

    @SneakyThrows
    public void send(Peer peer, P2PMessageDto msg) {

        String json = mapper.writeValueAsString(msg);

        Sinks.EmitResult result =
                connections.connectAndSink(peer)
                           .outbound()
                           .tryEmitNext(json);
        if (result.isFailure()) {
            log.warn("❌  send to {} failed: {}", peer, result);
        }
    }
}
