package de.flashyotter.blockchain_node.integration;

import de.flashyotter.blockchain_node.BlockchainNodeApplication;
import de.flashyotter.blockchain_node.p2p.Peer;
import de.flashyotter.blockchain_node.service.PeerRegistry;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"node.data-path=build/test-data/ws-discovery",
                  "node.libp2p-port=0",
                  "grpc.server.port=19080"})
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@org.junit.jupiter.api.Disabled("Flaky in CI")
class WsPortDiscoveryIT {

    @LocalServerPort
    int portA;

    @Autowired
    PeerRegistry registry;

    private final TestRestTemplate http = new TestRestTemplate();
    private org.springframework.context.ConfigurableApplicationContext ctxB;

    @BeforeAll
    void startSecondNode() {
        ctxB = new org.springframework.boot.builder.SpringApplicationBuilder(BlockchainNodeApplication.class)
                .properties(
                    "server.port=0",
                    "grpc.server.port=19081",
                    "node.wallet-password=test",
                    "node.libp2p-port=0",
                    "node.data-path=build/test-data/ws-discovery-b",
                    "node.peers=localhost:" + portA)
                .run();

        Awaitility.await().atMost(Duration.ofSeconds(20))
                .until(() -> !registry.all().isEmpty());
    }

    @org.junit.jupiter.api.AfterAll
    void shutdown() {
        if (ctxB != null) ctxB.close();
    }

    @Test
    void restApiReachableViaHandshakePort() {
        Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            Peer peer = registry.all().iterator().next();
            String url = "http://" + peer.getHost() + ":" + peer.getRestPort() + "/api/chain/latest";
            int status = http.getForEntity(url, String.class).getStatusCode().value();
            assertEquals(200, status);
        });
    }
}
