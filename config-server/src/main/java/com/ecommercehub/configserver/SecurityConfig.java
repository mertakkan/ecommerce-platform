package com.ecommercehub.configserver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for Config Server
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        // Allow health check without authentication
                        .requestMatchers("/actuator/health").permitAll()

                        // Protect all encryption-related endpoints
                        .requestMatchers("/encrypt/**", "/decrypt/**").authenticated()
                        .requestMatchers("/admin/**").authenticated()

                        // Allow config endpoints without auth (services need configs)
                        .requestMatchers("/**").permitAll()
                )
                .httpBasic(basic -> basic.realmName("Config Server"))
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}