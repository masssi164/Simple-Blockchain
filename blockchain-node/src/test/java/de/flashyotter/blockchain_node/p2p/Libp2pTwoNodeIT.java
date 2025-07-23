package de.flashyotter.blockchain_node.p2p;

import de.flashyotter.blockchain_node.BlockchainNodeApplication;
import de.flashyotter.blockchain_node.config.NodeProperties;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = BlockchainNodeApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Libp2pTwoNodeIT {
    @LocalServerPort
    int port1;
    static int port2;
    static Process node2;
    static String node2Id;

    @DynamicPropertySource
    static void node1Props(DynamicPropertyRegistry registry) {
        // Node 1 listens on port1, node2 on port2
        port2 = 35555;
        registry.add("node.libp2p-port", () -> 35554);
        registry.add("node.peers", () -> "/ip4/127.0.0.1/tcp/" + port2);
        registry.add("server.port", () -> 0);
    }

    @BeforeAll
    static void startSecondNode() throws Exception {
        // Start a second node as a separate process with peer set to node1
        ProcessBuilder pb = new ProcessBuilder(
                "java", "-jar", "build/libs/blockchain-node-0.0.1-SNAPSHOT.jar",
                "--node.libp2p-port=" + port2,
                "--node.peers=/ip4/127.0.0.1/tcp/35554",
                "--server.port=0"
        );
        pb.inheritIO();
        node2 = pb.start();
        // Wait for node2 to start and expose its REST API
        TimeUnit.SECONDS.sleep(10);
        // Get node2's id via REST
        RestTemplate rest = new RestTemplate();
        for (int i = 0; i < 10; i++) {
            try {
                ResponseEntity<String> resp = rest.getForEntity("http://localhost:" + port2 + "/api/node/id", String.class);
                if (resp.getStatusCode().is2xxSuccessful()) {
                    node2Id = resp.getBody();
                    break;
                }
            } catch (Exception ignored) {
                TimeUnit.SECONDS.sleep(1);
            }
        }
        assertThat(node2Id).isNotBlank();
    }

    @AfterAll
    static void stopSecondNode() {
        if (node2 != null) node2.destroy();
    }

    @Test
    void testNodesDiscoverEachOther() throws Exception {
        // Node 1 should see node2 as a peer
        RestTemplate rest = new RestTemplate();
        ResponseEntity<String[]> resp = rest.getForEntity("http://localhost:" + port1 + "/api/node/peers", String[].class);
        assertThat(resp.getBody()).contains(node2Id);
    }
}
