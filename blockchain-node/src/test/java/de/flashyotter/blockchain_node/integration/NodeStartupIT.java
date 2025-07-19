package de.flashyotter.blockchain_node.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

/** Simple integration test verifying that the Spring context starts. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "node.data-path=build/test-data/startup",
        "node.libp2p-port=0"
    })
class NodeStartupIT {

    @Autowired
    private ConfigurableApplicationContext ctx;

    @Test
    void contextStartsSuccessfully() {
        assertThat(ctx).isNotNull();
        assertThat(ctx.isActive()).isTrue();
    }
}
