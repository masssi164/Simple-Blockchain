package de.flashyotter.blockchain_node;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import de.flashyotter.blockchain_node.config.NodeProperties;

@SpringBootApplication
@EnableConfigurationProperties(NodeProperties.class)
@EnableScheduling
public class BlockchainNodeApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlockchainNodeApplication.class, args);
	}

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
