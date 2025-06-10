package de.flashyotter.blockchain_node.wallet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.test.context.ActiveProfiles;

import blockchain.core.crypto.AddressUtils;
import blockchain.core.model.Wallet;
import de.flashyotter.blockchain_node.dto.SendFundsDto;
import de.flashyotter.blockchain_node.dto.WalletInfoDto;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled("Requires running mining which is slow in CI")
class SpendFundsFlowIT {

    @LocalServerPort int port;
    private final TestRestTemplate http = new TestRestTemplate();

    @Test
    void balanceDropsAfterSpend() {

        String base = "http://localhost:" + port;

        /* ── 1: mine one block so the wallet owns 50 coins ─────────────── */
        http.postForEntity(base + "/api/mining/mine", null, Void.class);

        WalletInfoDto before = http.getForObject(base + "/api/wallet",
                                                 WalletInfoDto.class);
        double start = before.confirmedBalance();

        /* ── 2: build a random recipient address ───────────────────────── */
        String recipientAddr =
                AddressUtils.publicKeyToAddress(new Wallet().getPublicKey());

        /* ── 3: create & submit TX for 17.5 coins ──────────────────────── */
        SendFundsDto dto = new SendFundsDto(recipientAddr, 17.5);

        http.exchange(RequestEntity.post(base + "/api/wallet/send")
                                   .contentType(MediaType.APPLICATION_JSON)
                                   .body(dto),
                      String.class);

        /* ── 4: mine next block so the TX confirms ─────────────────────── */
        http.postForEntity(base + "/api/mining/mine", null, Void.class);

        /* ── 5: wallet balance must be 50 − 17.5 = 32.5 ───────────────── */
        Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            WalletInfoDto after =
                    http.getForObject(base + "/api/wallet", WalletInfoDto.class);
            assertEquals(start - 17.5, after.confirmedBalance(), 1e-6);
        });
    }
}
