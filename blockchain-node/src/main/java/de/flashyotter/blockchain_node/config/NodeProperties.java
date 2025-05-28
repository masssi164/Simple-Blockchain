package de.flashyotter.blockchain_node.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "node")
public class NodeProperties {
    private List<String> peers = List.of();
    
    /**
     * Password for encrypting/decrypting the PKCS12 keystore.
     */
    private String walletPassword;
}
