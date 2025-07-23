package de.flashyotter.blockchain_node.p2p;

import de.flashyotter.blockchain_node.BlockchainNodeApplication;
import de.flashyotter.blockchain_node.dto.NodeIdDto;
import org.junit.jupiter.api.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"node.libp2p-port=35554", "node.peers=/ip4/127.0.0.1/tcp/35555", "server.port=35553", "node.data-path=build/test-data/node1"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Libp2pPeerDiscoveryIT {
    private ConfigurableApplicationContext node2Ctx;
    private int node2Port = 35555;
    private String node2Id;

    @BeforeAll
    void startSecondNode() throws Exception {
        Map<String, Object> props = new HashMap<>();
        props.put("node.libp2p-port", node2Port);
        props.put("node.peers", "/ip4/127.0.0.1/tcp/35554");
        props.put("server.port", node2Port);
        props.put("node.data-path", "build/test-data/node2");
        SpringApplication app = new SpringApplication(BlockchainNodeApplication.class);
        app.setWebApplicationType(WebApplicationType.SERVLET);
        app.setDefaultProperties(props);
        node2Ctx = app.run();
        // Wait for node2 to start
        TimeUnit.SECONDS.sleep(8);
        // Get node2's id via REST
        RestTemplate rest = new RestTemplate();
        for (int i = 0; i < 10; i++) {
            try {
                ResponseEntity<NodeIdDto> resp = rest.getForEntity("http://localhost:" + node2Port + "/node/id", NodeIdDto.class);
                if (resp.getStatusCode().is2xxSuccessful()) {
                    node2Id = resp.getBody().nodeId();
                    break;
                }
            } catch (Exception ignored) {
                TimeUnit.SECONDS.sleep(1);
            }
        }
        assertThat(node2Id).isNotBlank();
    }

    @AfterAll
    void stopSecondNode() {
        if (node2Ctx != null) node2Ctx.close();
    }

    @Test
    void testNodesDiscoverEachOther() throws Exception {
        // Node 1 should see node2 as a peer
        RestTemplate rest = new RestTemplate();
        ResponseEntity<String[]> resp = rest.getForEntity("http://localhost:35553/node/peers", String[].class);
        assertThat(resp.getBody()).isNotNull();
        boolean found = false;
        for (String peer : resp.getBody()) {
            if (peer.contains(node2Id)) {
                found = true;
                break;
            }
        }
        assertThat(found).isTrue();
    }
}
