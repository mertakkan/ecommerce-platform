package com.ecommercehub.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Circuit Breaker Configuration using Resilience4J
 * <p>
 * Circuit Breaker Pattern: Prevents cascading failures by monitoring
 * service calls and "opening" the circuit when failure threshold is reached
 * <p>
 * States:
 * - CLOSED: Normal operation, calls go through
 * - OPEN: Circuit is open, calls fail fast
 * - HALF_OPEN: Testing if service is back up
 */
@Configuration
@Slf4j
public class CircuitBreakerConfig {

    /**
     * Global circuit breaker configuration
     * This applies to all circuit breakers unless overridden
     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(
                        io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                                // Sliding window settings
                                .slidingWindowSize(10)                    // Monitor last 10 calls
                                .slidingWindowType(SlidingWindowType.COUNT_BASED)
                                .minimumNumberOfCalls(5)                  // Need at least 5 calls before evaluation

                                // Failure threshold
                                .failureRateThreshold(50.0f)              // Open circuit if 50% of calls fail
                                .slowCallRateThreshold(80.0f)             // Consider slow calls as failures
                                .slowCallDurationThreshold(Duration.ofSeconds(3))

                                // State transition settings
                                .waitDurationInOpenState(Duration.ofSeconds(30))  // Stay open for 30 seconds
                                .permittedNumberOfCallsInHalfOpenState(3)         // Allow 3 test calls in half-open

                                // What constitutes a failure
                                .recordExceptions(
                                        java.net.ConnectException.class,
                                        java.util.concurrent.TimeoutException.class,
                                        org.springframework.web.client.ResourceAccessException.class
                                )
                                .ignoreExceptions(
                                        java.lang.IllegalArgumentException.class  // Don't count client errors
                                )

                                .build())
                .timeLimiterConfig(
                        TimeLimiterConfig.custom()
                                .timeoutDuration(Duration.ofSeconds(5))   // Timeout after 5 seconds
                                .build())
                .build());
    }

    /**
     * Specific configuration for critical services (payments, orders)
     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> criticalServiceCustomizer() {
        return factory -> factory.configure(builder -> builder
                .circuitBreakerConfig(
                        io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                                .slidingWindowSize(20)                    // Larger window for more accuracy
                                .minimumNumberOfCalls(10)
                                .failureRateThreshold(30.0f)              // More sensitive to failures
                                .waitDurationInOpenState(Duration.ofMinutes(1))  // Longer wait time
                                .build())
                .timeLimiterConfig(
                        TimeLimiterConfig.custom()
                                .timeoutDuration(Duration.ofSeconds(3))   // Shorter timeout
                                .build())
                .build(), "payment-service-cb", "order-service-cb");
    }

    /**
     * Configuration for non-critical services (products, search)
     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> nonCriticalServiceCustomizer() {
        return factory -> factory.configure(builder -> builder
                .circuitBreakerConfig(
                        io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                                .slidingWindowSize(5)
                                .minimumNumberOfCalls(3)
                                .failureRateThreshold(70.0f)              // More tolerant of failures
                                .waitDurationInOpenState(Duration.ofSeconds(15))
                                .build())
                .timeLimiterConfig(
                        TimeLimiterConfig.custom()
                                .timeoutDuration(Duration.ofSeconds(10))  // Longer timeout
                                .build())
                .build(), "product-service-cb", "search-service-cb");
    }
}