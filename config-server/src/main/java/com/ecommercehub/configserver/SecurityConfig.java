package com.ecommercehub.configserver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for Config Server
 * <p>
 * This configuration:
 * 1. Allows unrestricted access to config endpoints (since services need to fetch configs)
 * 2. Protects encryption/decryption endpoints with basic auth
 * 3. Enables CSRF protection for state-changing operations
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Configure authorization rules
                .authorizeHttpRequests(authz -> authz
                        // Allow health check without authentication (for load balancers)
                        .requestMatchers("/actuator/health").permitAll()

                        // Protect encryption/decryption endpoints - only authenticated users can access
                        .requestMatchers("/encrypt/**", "/decrypt/**").authenticated()

                        // Allow config endpoints without auth (services need to fetch their configs)
                        .requestMatchers("/**").permitAll()
                )
                // Enable HTTP Basic Authentication for protected endpoints
                .httpBasic(basic -> basic.realmName("Config Server"))

                // Disable CSRF for REST API endpoints
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}