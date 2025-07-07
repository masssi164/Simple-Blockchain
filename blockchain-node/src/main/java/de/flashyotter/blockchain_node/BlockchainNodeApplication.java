package de.flashyotter.blockchain_node;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class BlockchainNodeApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlockchainNodeApplication.class, args);
	}
}
