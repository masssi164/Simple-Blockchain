// blockchain-node/src/test/java/de/flashyotter/blockchain_node/network/TwoNodeFundsFlowIT.java
package de.flashyotter.blockchain_node.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.time.Duration;
import java.util.Arrays;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import blockchain.core.model.Block;
import de.flashyotter.blockchain_node.BlockchainNodeApplication;
import de.flashyotter.blockchain_node.dto.SendFundsDto;
import de.flashyotter.blockchain_node.dto.WalletInfoDto;

/**
 * Network-edge integration test with *many* pending TXs:
 *
 *  • node-A mines the genesis successor (height 1 → +50 coins)
 *  • A emits THREE payments (10.0, 4.5, 1.5) to B – all stay in the mem-pool
 *  • A mines a 2nd block → all 3 TXs confirm simultaneously
 *  • Assertions
 *       – B balance = 10 + 4.5 + 1.5 = 16
 *       – A balance = 50 (initial) + 50 (coinbase #2) − 16 = 84
 *       – overspend still yields HTTP 409
 *
 * The test exercises:
 *  • mem-pool accepting chained spends against *pending* change outputs
 *  • multi-TX block assembly & propagation
 *  • UTXO rebuild on both nodes
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TwoNodeFundsFlowIT {

    /* ---------- node-A is managed by the SpringBootTest context ------ */
    @LocalServerPort int portA;

    /* ---------- node-B is started manually inside this JVM ----------- */
    private int portB;
    private final TestRestTemplate http = new TestRestTemplate();

    /* ------------------------------------------------------------------ */
    /* Spin-up of node-B and peer-linking to A                            */
    /* ------------------------------------------------------------------ */
    @BeforeAll
    void spinUpSecondNode() {
        portB = randomFreePort();

        new Thread(() -> BlockchainNodeApplication.main(new String[]{
                "--server.port="          + portB,
                "--node.wallet-password=test",
                "--node.peers=localhost:" + portA
        })).start();

        Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
            http.getForEntity("http://localhost:" + portB + "/actuator/health",
                              Void.class));
    }

    /* ------------------------------------------------------------------ */
    /* The actual end-to-end test                                         */
    /* ------------------------------------------------------------------ */
    @Test
    @DisplayName("multi-TX block across two nodes + overspend error")
    void manyTxsInOneBlock() {

        String baseA = "http://localhost:" + portA;
        String baseB = "http://localhost:" + portB;

        /* — 1 : A mines height-1 block → gets first 50-coin reward —— */
        http.postForEntity(baseA + "/api/mining/mine", null, Block.class);

        /* Wait until B is on height 1 as well -------------------------- */
        Awaitility.await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            int hA = http.getForObject(baseA + "/api/chain/latest", Block.class).getHeight();
            int hB = http.getForObject(baseB + "/api/chain/latest", Block.class).getHeight();
            assertEquals(hA, hB, "chains in sync @ height 1");
        });

        /* Wallet snapshots BEFORE we queue TXs ------------------------- */
        WalletInfoDto infoA0 = http.getForObject(baseA + "/api/wallet", WalletInfoDto.class);
        WalletInfoDto infoB0 = http.getForObject(baseB + "/api/wallet", WalletInfoDto.class);

        /* — 2 : queue THREE payments (remain unconfirmed) -------------- */
        double[] amounts = { 10.0, 4.5, 1.5 };
        Arrays.stream(amounts).forEach(val -> http.exchange(
                RequestEntity.post(baseA + "/api/wallet/send")
                             .contentType(APPLICATION_JSON)
                             .body(new SendFundsDto(infoB0.address(), val)),
                String.class));

        /* — 3 : mine ONE block – should pack all pending TXs ----------- */
        http.postForEntity(baseA + "/api/mining/mine", null, Block.class);

        /* Expected balances after confirmation ------------------------- */
        double totalSent       = Arrays.stream(amounts).sum();  // 16.0
        double expectedBFinal  = infoB0.confirmedBalance() + totalSent;
        double expectedAFinal  = infoA0.confirmedBalance() - totalSent + 50.0;

        /* — 4 : wait until B processed the block, then assert ---------- */
        Awaitility.await().atMost(Duration.ofSeconds(25)).untilAsserted(() -> {
            WalletInfoDto infoA = http.getForObject(baseA + "/api/wallet", WalletInfoDto.class);
            WalletInfoDto infoB = http.getForObject(baseB + "/api/wallet", WalletInfoDto.class);

            assertEquals(expectedAFinal, infoA.confirmedBalance(), 1e-6, "A final balance");
            assertEquals(expectedBFinal, infoB.confirmedBalance(), 1e-6, "B final balance");
        });

        /* — 5 : B still cannot overspend ------------------------------- */
        SendFundsDto tooMuch = new SendFundsDto(infoA0.address(), 1_000.0);
        ResponseEntity<String> rsp = http.exchange(
                RequestEntity.post(baseB + "/api/wallet/send")
                             .contentType(APPLICATION_JSON)
                             .body(tooMuch),
                String.class);
        assertEquals(409, rsp.getStatusCode().value(), "overspend → 409 Conflict");
    }

    /* ------------------------------------------------------------------ */
    /* Helper: allocate an unused TCP port                                */
    /* ------------------------------------------------------------------ */
    private static int randomFreePort() {
        try (java.net.ServerSocket s = new java.net.ServerSocket(0)) {
            s.setReuseAddress(true);
            return s.getLocalPort();
        } catch (java.io.IOException e) {
            throw new RuntimeException("no free TCP port available", e);
        }
    }
}
