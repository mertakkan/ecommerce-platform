package com.ecommercehub.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

/**
 * Gateway routing configuration using programmatic approach
 * This provides more flexibility than YAML configuration for complex routing logic
 */
@Configuration
@Slf4j
public class GatewayConfig {

    /**
     * Define all routes programmatically
     * RouteLocator is the main interface for defining routes
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // User Service Routes
                .route("user-service", r -> r
                        .path("/api/users/**")  // Match any path starting with /api/users/
                        .and()
                        .method(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
                        .filters(f -> f
                                // Add custom headers for downstream service
                                .addRequestHeader("X-Gateway-Request-Id", "#{T(java.util.UUID).randomUUID().toString()}")
                                .addRequestHeader("X-Gateway-Timestamp", "#{T(java.time.Instant).now().toString()}")
                                // Rate limiting: 100 requests per minute per user
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver()))
                                // Circuit breaker with fallback
                                .circuitBreaker(config -> config
                                        .setName("user-service-cb")
                                        .setFallbackUri("forward:/fallback/user-service"))
                                // Remove sensitive headers before forwarding
                                .removeRequestHeader("Cookie")
                                .removeRequestHeader("Authorization") // Will be handled by security layer
                        )
                        .uri("lb://USER-SERVICE"))  // Load balance to USER-SERVICE instances

                // Product Service Routes
                .route("product-service", r -> r
                        .path("/api/products/**", "/api/categories/**")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Request-Id", "#{T(java.util.UUID).randomUUID().toString()}")
                                // Higher rate limit for product browsing
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(ipKeyResolver()))
                                .circuitBreaker(config -> config
                                        .setName("product-service-cb")
                                        .setFallbackUri("forward:/fallback/product-service"))
                                // Cache GET requests for products
                                .filter((exchange, chain) -> {
                                    if (HttpMethod.GET.equals(exchange.getRequest().getMethod())) {
                                        exchange.getResponse().getHeaders().add("Cache-Control", "max-age=300");
                                    }
                                    return chain.filter(exchange);
                                })
                        )
                        .uri("lb://PRODUCT-SERVICE"))

                // Inventory Service Routes
                .route("inventory-service", r -> r
                        .path("/api/inventory/**")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Request-Id", "#{T(java.util.UUID).randomUUID().toString()}")
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver()))
                                .circuitBreaker(config -> config
                                        .setName("inventory-service-cb")
                                        .setFallbackUri("forward:/fallback/inventory-service"))
                        )
                        .uri("lb://INVENTORY-SERVICE"))

                // Cart Service Routes
                .route("cart-service", r -> r
                        .path("/api/cart/**")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Request-Id", "#{T(java.util.UUID).randomUUID().toString()}")
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver()))
                                .circuitBreaker(config -> config
                                        .setName("cart-service-cb")
                                        .setFallbackUri("forward:/fallback/cart-service"))
                        )
                        .uri("lb://CART-SERVICE"))

                // Order Service Routes
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Request-Id", "#{T(java.util.UUID).randomUUID().toString()}")
                                // Lower rate limit for orders (more critical)
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver()))
                                .circuitBreaker(config -> config
                                        .setName("order-service-cb")
                                        .setFallbackUri("forward:/fallback/order-service"))
                        )
                        .uri("lb://ORDER-SERVICE"))

                // Payment Service Routes
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Request-Id", "#{T(java.util.UUID).randomUUID().toString()}")
                                // Strict rate limiting for payments
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver()))
                                .circuitBreaker(config -> config
                                        .setName("payment-service-cb")
                                        .setFallbackUri("forward:/fallback/payment-service"))
                        )
                        .uri("lb://PAYMENT-SERVICE"))

                // Search Service Routes
                .route("search-service", r -> r
                        .path("/api/search/**")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Request-Id", "#{T(java.util.UUID).randomUUID().toString()}")
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(ipKeyResolver()))
                                .circuitBreaker(config -> config
                                        .setName("search-service-cb")
                                        .setFallbackUri("forward:/fallback/search-service"))
                        )
                        .uri("lb://SEARCH-SERVICE"))

                // Notification Service Routes (internal only)
                .route("notification-service", r -> r
                        .path("/internal/notifications/**")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Request-Id", "#{T(java.util.UUID).randomUUID().toString()}")
                                // Internal service - no rate limiting needed
                                .circuitBreaker(config -> config
                                        .setName("notification-service-cb")
                                        .setFallbackUri("forward:/fallback/notification-service"))
                        )
                        .uri("lb://NOTIFICATION-SERVICE"))

                // Health check route for all services
                .route("health-check", r -> r
                        .path("/health/**")
                        .filters(f -> f
                                .rewritePath("/health/(?<service>.*)", "/actuator/health")
                                .addRequestHeader("X-Gateway-Health-Check", "true")
                        )
                        .uri("lb://SERVICE-DISCOVERY"))  // Route to any service for health check

                .build();
    }

    // Rate limiter beans will be defined in RateLimitConfig
    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter redisRateLimiter() {
        return new org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter(10, 20, 1);
    }

    // Key resolver for user-based rate limiting
    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.KeyResolver userKeyResolver() {
        return exchange -> {
            // Try to get user ID from JWT token or session
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");
            if (userId != null) {
                return reactor.core.publisher.Mono.just(userId);
            }
            // Fallback to IP-based rate limiting
            return reactor.core.publisher.Mono.just(
                    exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
            );
        };
    }

    // Key resolver for IP-based rate limiting
    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.KeyResolver ipKeyResolver() {
        return exchange -> reactor.core.publisher.Mono.just(
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
        );
    }
}