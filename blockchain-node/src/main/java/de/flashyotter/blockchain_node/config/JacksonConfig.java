package de.flashyotter.blockchain_node.config;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    /** liefert das selbstgebaute Modul mit (De-)Serializer für PublicKey */
    @Bean
    KeyModule keyModule() {                       // ①
        return new KeyModule();
    }

    /** sorgt dafür, dass Spring-Boots ObjectMapper das Modul lädt        */
    @Bean
    Jackson2ObjectMapperBuilderCustomizer registerKeyModule(KeyModule mod){ // ②
        return builder -> builder.modules(mod);
    }
}
