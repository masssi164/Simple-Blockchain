package de.flashyotter.blockchain_node;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.awaitility.Awaitility;
// Test, BeforeAll, TestInstance
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort; // Boot 3+
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import blockchain.core.model.Block;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled("Relies on network setup not available in CI")
class ClusterReorgIT {

    @LocalServerPort int portA;          // ← Port des 1. Nodes
    int portB;                           // is dynamically choosen

    @BeforeAll
    void spinSecondNode() {
        portB = randomFreePort();
        new Thread(() -> BlockchainNodeApplication.main(new String[]{
                "--server.port="          + portB,
                "--node.wallet-password=test",
                "--node.peers=localhost:" + portA
        })).start();

        Awaitility.await().atMost(Duration.ofSeconds(10))
                  .untilAsserted(() ->
                      WebClient.create("http://localhost:" + portB)
                               .get().uri("/actuator/health").retrieve()
                               .toBodilessEntity().block());
    }

    @Test
    void heavierBranchWins() {
        WebClient wcA = WebClient.builder()
                                 .baseUrl("http://localhost:" + portA).build();
        WebClient wcB = WebClient.builder()
                                 .baseUrl("http://localhost:" + portB).build();

        // B mined heavier branch (2 Blöcke)
        wcB.post().uri("/api/mining/mine").retrieve().bodyToMono(String.class).block();
        wcB.post().uri("/api/mining/mine").retrieve().bodyToMono(String.class).block();

        // A mined lighter branch (1 Block)
        wcA.post().uri("/api/mining/mine").retrieve().bodyToMono(String.class).block();

        // warten bis Sync fertig
        Awaitility.await().atMost(Duration.ofSeconds(20))
                  .until(() ->
                      wcA.get().uri("/api/chain/latest").retrieve()
                         .bodyToMono(Block.class).block().getHeight() == 2);

        // gleicher Tip erwartet
        String tipA = wcA.get().uri("/api/chain/latest").retrieve()
                         .bodyToMono(Block.class).block().getHashHex();
        String tipB = wcB.get().uri("/api/chain/latest").retrieve()
                         .bodyToMono(Block.class).block().getHashHex();
        assertEquals(tipB, tipA);
    }

    private static int randomFreePort() {
        try (java.net.ServerSocket s = new java.net.ServerSocket(0)) {
            s.setReuseAddress(true);
            return s.getLocalPort();
        } catch (java.io.IOException e) {
            throw new RuntimeException("No free TCP port found", e);
        }
    }

}
