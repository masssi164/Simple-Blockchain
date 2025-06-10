package de.flashyotter.blockchain_node.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import blockchain.core.model.Block;
import de.flashyotter.blockchain_node.BlockchainNodeApplication;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@org.junit.jupiter.api.Disabled("Requires multiple nodes running; flaky in CI")
class P2PConnectionIT {

    @LocalServerPort
    int portA;

    int portB;
    TestRestTemplate http = new TestRestTemplate();

    @BeforeAll
    void startSecondNode() {
        portB = randomFreePort();
        new Thread(() -> BlockchainNodeApplication.main(new String[]{
                "--server.port=" + portB,
                "--node.wallet-password=test",
                "--node.peers=localhost:" + portA
        })).start();

        Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
            http.getForEntity("http://localhost:" + portB + "/actuator/health", Void.class));
    }

    @Test
    void blockPropagationBetweenNodes() {
        String baseA = "http://localhost:" + portA;
        String baseB = "http://localhost:" + portB;

        http.postForEntity(baseA + "/api/mining/mine", null, Block.class);

        Awaitility.await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            int hA = http.getForObject(baseA + "/api/chain/latest", Block.class).getHeight();
            int hB = http.getForObject(baseB + "/api/chain/latest", Block.class).getHeight();
            assertEquals(hA, hB);
        });
    }

    private static int randomFreePort() {
        try (java.net.ServerSocket s = new java.net.ServerSocket(0)) {
            s.setReuseAddress(true);
            return s.getLocalPort();
        } catch (java.io.IOException e) {
            throw new RuntimeException("no free TCP port available", e);
        }
    }
}
