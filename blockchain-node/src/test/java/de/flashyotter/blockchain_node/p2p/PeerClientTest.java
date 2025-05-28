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
import reactor.core.publisher.Mono;

class PeerClientTest {

    @Mock ObjectMapper mapper;
    @Mock ReactorNettyWebSocketClient wsClient;

    PeerClient client;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        client = new PeerClient(mapper, wsClient);
    }

    @Test
    void send_usesWebSocketClientWithCorrectUriAndPayload() throws Exception {
        // arrange
        Peer peer = new Peer("localhost", 8080);
        NewTxDto msg = new NewTxDto("{\"foo\":\"bar\"}");

        when(mapper.writeValueAsString(msg)).thenReturn("{\"type\":\"NewTxDto\",\"rawTxJson\":\"{\\\"foo\\\":\\\"bar\\\"}\"}");
        // stub execute to return an empty Mono
        when(wsClient.execute(eq(URI.create("ws://localhost:8080/ws")), any()))
            .thenReturn(Mono.empty());

        // act
        client.send(peer, msg);

        // assert
        verify(wsClient, timeout(1000)).execute(eq(URI.create("ws://localhost:8080/ws")), any());
        verify(mapper).writeValueAsString(msg);
    }
}
