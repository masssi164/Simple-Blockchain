package de.flashyotter.blockchain_node.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
          // 1) Disable CSRF using the Lambda-based DSL
          .csrf(csrf -> csrf.disable())

          // 2) Permit every HTTP request without authentication
          .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }
}
