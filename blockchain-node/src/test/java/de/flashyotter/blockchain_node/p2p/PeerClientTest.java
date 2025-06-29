package de.flashyotter.blockchain_node.p2p;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.flashyotter.blockchain_node.dto.NewTxDto;
import de.flashyotter.blockchain_node.config.NodeProperties;
import reactor.core.publisher.Mono;

class PeerClientTest {

    @Mock ObjectMapper mapper;
    @Mock ReactorNettyWebSocketClient wsClient;

    PeerClient client;
    NodeProperties props;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        props = new NodeProperties();
        props.setId("me");
        client = new PeerClient(mapper, wsClient, props);
    }

    @Test
    void connectionReusedForMultipleSends() throws Exception {
        Peer peer = new Peer("localhost", 8080);
        NewTxDto msg = new NewTxDto("{\"foo\":\"bar\"}");

        when(mapper.writeValueAsString(any())).thenReturn("json");
        when(wsClient.execute(eq(URI.create("ws://localhost:8080/ws")), any()))
            .thenReturn(Mono.never());

        client.send(peer, msg);
        client.send(peer, msg);

        verify(wsClient, timeout(1000).times(1))
            .execute(eq(URI.create("ws://localhost:8080/ws")), any());
    }
}
