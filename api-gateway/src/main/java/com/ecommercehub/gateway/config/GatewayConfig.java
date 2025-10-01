package com.ecommercehub.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
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
    public RouteLocator customRouteLocator(
            RouteLocatorBuilder builder,
            @Qualifier("defaultRateLimiter") RedisRateLimiter defaultRateLimiter,
            @Qualifier("strictRateLimiter") RedisRateLimiter strictRateLimiter,
            @Qualifier("lenientRateLimiter") RedisRateLimiter lenientRateLimiter,
            @Qualifier("userKeyResolver") KeyResolver userKeyResolver,
            @Qualifier("ipKeyResolver") KeyResolver ipKeyResolver
    ) {
        return builder.routes()

                // User Service Routes
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .and()
                        .method(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.OPTIONS)
                        .filters(f -> f
                                // Rate limiting: 100 requests per minute per user
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(defaultRateLimiter)
                                        .setKeyResolver(userKeyResolver))
                                // Circuit breaker with fallback
                                .circuitBreaker(config -> config
                                        .setName("user-service-cb")
                                        .setFallbackUri("forward:/fallback/user-service"))
                        )
                        .uri("lb://USER-SERVICE"))  // Load balance to USER-SERVICE instances

                // Product Service Routes
                .route("product-service", r -> r
                        .path("/api/products/**", "/api/categories/**")
                        .filters(f -> f
                                // Higher rate limit for product browsing
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(lenientRateLimiter)
                                        .setKeyResolver(ipKeyResolver))
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
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(defaultRateLimiter)
                                        .setKeyResolver(userKeyResolver))
                                .circuitBreaker(config -> config
                                        .setName("inventory-service-cb")
                                        .setFallbackUri("forward:/fallback/inventory-service"))
                        )
                        .uri("lb://INVENTORY-SERVICE"))

                // Cart Service Routes
                .route("cart-service", r -> r
                        .path("/api/cart/**")
                        .filters(f -> f
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(defaultRateLimiter)
                                        .setKeyResolver(userKeyResolver))
                                .circuitBreaker(config -> config
                                        .setName("cart-service-cb")
                                        .setFallbackUri("forward:/fallback/cart-service"))
                        )
                        .uri("lb://CART-SERVICE"))

                // Order Service Routes
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f
                                // Lower rate limit for orders (more critical)
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(strictRateLimiter)
                                        .setKeyResolver(userKeyResolver))
                                .circuitBreaker(config -> config
                                        .setName("order-service-cb")
                                        .setFallbackUri("forward:/fallback/order-service"))
                        )
                        .uri("lb://ORDER-SERVICE"))

                // Payment Service Routes
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .filters(f -> f
                                // Strict rate limiting for payments
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(strictRateLimiter)
                                        .setKeyResolver(userKeyResolver))
                                .circuitBreaker(config -> config
                                        .setName("payment-service-cb")
                                        .setFallbackUri("forward:/fallback/payment-service"))
                        )
                        .uri("lb://PAYMENT-SERVICE"))

                // Search Service Routes
                .route("search-service", r -> r
                        .path("/api/search/**")
                        .filters(f -> f
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(lenientRateLimiter)
                                        .setKeyResolver(ipKeyResolver))
                                .circuitBreaker(config -> config
                                        .setName("search-service-cb")
                                        .setFallbackUri("forward:/fallback/search-service"))
                        )
                        .uri("lb://SEARCH-SERVICE"))

                // Notification Service Routes (internal only)
                .route("notification-service", r -> r
                        .path("/internal/notifications/**")
                        .filters(f -> f
                                // Internal service - no rate limiting needed
                                .circuitBreaker(config -> config
                                        .setName("notification-service-cb")
                                        .setFallbackUri("forward:/fallback/notification-service"))
                        )
                        .uri("lb://NOTIFICATION-SERVICE"))

                .build();
    }
}