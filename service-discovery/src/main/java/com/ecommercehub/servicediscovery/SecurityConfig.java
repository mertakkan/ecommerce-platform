package com.ecommercehub.servicediscovery;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;

/**
 * Security Configuration for Eureka Server
 * <p>
 * This configuration serves multiple purposes:
 * 1. Protects the Eureka dashboard from unauthorized access
 * 2. Secures the Eureka REST endpoints
 * 3. Allows health checks without authentication (for monitoring tools)
 * <p>
 * In production, you would:
 * - Use stronger passwords from environment variables
 * - Implement OAuth2 or certificate-based authentication
 * - Use HTTPS for all communications
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configure HTTP security for the Eureka Server
     * <p>
     * Important considerations:
     * - CSRF is disabled because microservices use token-based auth
     * - Basic auth is used for simplicity (in production, use OAuth2)
     * - Health endpoints are public for monitoring tools
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for service-to-service communication
                .csrf(csrf -> csrf.ignoringRequestMatchers("/eureka/**"))

                // Configure authorization rules
                .authorizeHttpRequests(authz -> authz
                        // Allow health checks without authentication
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()

                        // Allow Eureka service endpoints (for service registration/discovery)
                        // These endpoints are used by microservices to register themselves
                        .requestMatchers("/eureka/**").authenticated()

                        // Protect all other endpoints (including the dashboard)
                        .anyRequest().authenticated()
                )

                // Enable HTTP Basic authentication
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    /**
     * In-memory user store for development
     * In production, integrate with your identity provider
     */
    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("admin123"))
                .roles("ADMIN")
                .build();

        UserDetails service = User.builder()
                .username("service")
                .password(passwordEncoder().encode("service123"))
                .roles("SERVICE")
                .build();

        return new InMemoryUserDetailsManager(admin, service);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}