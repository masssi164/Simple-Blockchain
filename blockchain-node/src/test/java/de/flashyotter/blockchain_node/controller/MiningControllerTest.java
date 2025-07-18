package de.flashyotter.blockchain_node.controller;

import static blockchain.core.serialization.JsonUtils.toJson;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import blockchain.core.model.Wallet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import blockchain.core.model.Block;
import de.flashyotter.blockchain_node.controller.MiningController;
import de.flashyotter.blockchain_node.service.NodeService;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = MiningController.class,
             excludeAutoConfiguration = {SecurityAutoConfiguration.class,
                                          ReactiveSecurityAutoConfiguration.class})
class MiningControllerTest {

    @Autowired
    private WebTestClient client;

    @MockBean
    private NodeService nodeSvc;

    @Test
    void mine() {
        Block b = new Block(0, "0".repeat(64),
            List.of(new blockchain.core.model.Transaction(new Wallet().getPublicKey(), 0)), 0);
        when(nodeSvc.mineNow()).thenReturn(Mono.just(b));

        client.post()
                .uri("/api/mining/mine")
                .exchange()
                .expectStatus().isOk()
                .expectBody().json(toJson(b));

        verify(nodeSvc, times(1)).mineNow();
    }
}
