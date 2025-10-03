package de.flashyotter.blockchain_node.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import blockchain.core.model.Block;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled("Relies on network and timing, disable in CI")
class MiningFlowIT {

    @LocalServerPort int port;

    private final TestRestTemplate http = new TestRestTemplate();

    @Test
    void miningAdvancesHeightAndBalance() {

        String base = "http://localhost:" + port;

        int    startHeight = http.getForObject(
                base + "/api/chain/latest", Block.class).getHeight();

        double startBalance = balance(base);

        /* mine one block synchronously -------------------------------- */
        ResponseEntity<Block> mined = http.postForEntity(
                base + "/api/mining/mine", null, Block.class);
        assertEquals(201, mined.getStatusCode().value() / 100 * 100, "2xx expected");

        /* wait (max 10 s) until node has processed the block ---------- */
        Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {

            Block tip = http.getForObject(base + "/api/chain/latest", Block.class);

            assertEquals(startHeight + 1, tip.getHeight(), "height must advance");
            assertEquals(startBalance + 50.0, balance(base), 1e-9,
                         "coinbase reward credited");
        });
    }

    private double balance(String base) {
        UtxoView[] utxos = http.getForObject(base + "/api/utxo?address=testMinerAddress",
                UtxoView[].class);
        if (utxos == null) return 0.0;
        return java.util.Arrays.stream(utxos).mapToDouble(UtxoView::value).sum();
    }

    private record UtxoView(String id, double value, String recipientAddress) { }
}
