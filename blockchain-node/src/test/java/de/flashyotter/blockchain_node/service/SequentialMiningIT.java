package de.flashyotter.blockchain_node.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.stream.IntStream;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import blockchain.core.model.Block;
import de.flashyotter.blockchain_node.dto.WalletInfoDto;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SequentialMiningIT {

    @LocalServerPort int port;
    private final TestRestTemplate http = new TestRestTemplate();

    @Test
    void fiveBlocksIncreaseHeightAndBalance() {

        String base = "http://localhost:" + port;

        int    startHeight  = http.getForObject(base + "/api/chain/latest",
                                                Block.class).getHeight();
        double startBalance = http.getForObject(base + "/api/wallet",
                                                WalletInfoDto.class).confirmedBalance();

        /* mine five blocks -------------------------------------------------- */
        IntStream.range(0, 5).forEach(i ->
                http.postForEntity(base + "/api/mining/mine", null, Block.class));

        /* wait until the 5th block is processed ---------------------------- */
        Awaitility.await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {

            Block tip   = http.getForObject(base + "/api/chain/latest", Block.class);
            WalletInfoDto wallet =
                    http.getForObject(base + "/api/wallet", WalletInfoDto.class);

            assertEquals(startHeight  + 5, tip.getHeight(),          "height ↑ 5");
            assertEquals(startBalance + 5 * 50.0,
                         wallet.confirmedBalance(), 1e-9,             "balance +250");
        });
    }
}
