package de.flashyotter.blockchain_node.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Java toolchain to ensure the correct Java version is used.
 */
@Configuration
public class ToolchainConfig {
    
    @Bean
    public Integer javaVersion() {
        return Runtime.version().feature(); // Get the current Java version
    }
    
    @Bean
    public Boolean isValidJavaVersion() {
        int version = Runtime.version().feature();
        return version >= 17; // Require Java 17 or higher
    }
}
