package de.flashyotter.blockchain_node.p2p;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.flashyotter.blockchain_node.dto.NewTxDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

class PeerClientTest {

    @Mock ObjectMapper mapper;
    @Mock ConnectionManager manager;

    Sinks.Many<String> sink;

    PeerClient client;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sink = Sinks.many().unicast().onBackpressureBuffer();
        when(manager.connectAndSink(any())).thenReturn(new ConnectionManager.Conn(sink, Flux.never()));
        client = new PeerClient(mapper, manager);
    }

    @Test
    void send_usesWebSocketClientWithCorrectUriAndPayload() throws Exception {
        // arrange
        Peer peer = new Peer("localhost", 8080);
        NewTxDto msg = new NewTxDto("{\"foo\":\"bar\"}");

        when(mapper.writeValueAsString(msg)).thenReturn("{\"type\":\"NewTxDto\",\"rawTxJson\":\"{\\\"foo\\\":\\\"bar\\\"}\"}");

        // act
        client.send(peer, msg);

        // assert
        verify(manager).connectAndSink(peer);
        verify(mapper).writeValueAsString(msg);
        java.util.concurrent.atomic.AtomicReference<String> ref = new java.util.concurrent.atomic.AtomicReference<>();
        sink.asFlux().subscribe(ref::set);
        org.awaitility.Awaitility.await().until(() -> ref.get() != null);
        org.junit.jupiter.api.Assertions.assertEquals("{\"type\":\"NewTxDto\",\"rawTxJson\":\"{\\\"foo\\\":\\\"bar\\\"}\"}", ref.get());
    }
}
