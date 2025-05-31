package de.flashyotter.blockchain_node.controller;

import static blockchain.core.serialization.JsonUtils.toJson;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import blockchain.core.model.Block;
import de.flashyotter.blockchain_node.controler.MiningController;
import de.flashyotter.blockchain_node.service.NodeService;
import reactor.core.publisher.Mono;

@WebFluxTest(MiningController.class)
class MiningControllerTest {

    @Autowired
    private WebTestClient client;

    @MockitoBean
    private NodeService nodeSvc;

    @Test
    void mine() {
        Block b = new Block(0, "0".repeat(64),
            List.of(new blockchain.core.model.Transaction(null, 0)), 0);
        when(nodeSvc.mineNow()).thenReturn(Mono.just(b));

        client.post()
                .uri("/api/mining/mine")
                .exchange()
                .expectStatus().isOk()
                .expectBody().json(toJson(b));

        verify(nodeSvc, times(1)).mineNow();
    }
}
