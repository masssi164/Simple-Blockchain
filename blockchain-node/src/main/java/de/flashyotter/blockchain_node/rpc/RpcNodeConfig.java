package de.flashyotter.blockchain_node.rpc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_rpc.RpcHandler;

@Configuration
@Slf4j
public class RpcNodeConfig {
    @Bean
    RpcHandler nodeRpcHandler(NodeService node) {
        log.info("Registering SimpleRpcHandler");
        return new SimpleRpcHandler(node);
    }
}
