package com.ecommercehub.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;

/**
 * Rate Limiting Configuration using Redis
 * <p>
 * Rate limiting prevents abuse and ensures fair resource usage
 * Uses Token Bucket algorithm implemented in Redis
 */
@Configuration
@Slf4j
public class RateLimitConfig {

    /**
     * Redis Rate Limiter with Token Bucket Algorithm
     * <p>
     * Constructor parameters:
     * - replenishRate: How many requests per second are allowed (tokens added per second)
     * - burstCapacity: Maximum number of requests in a single burst (bucket size)
     * - requestedTokens: How many tokens each request costs (default: 1)
     */
    @Bean
    public RedisRateLimiter defaultRateLimiter() {
        return new RedisRateLimiter(
                10,   // replenishRate: 10 requests per second
                20,   // burstCapacity: up to 20 requests in burst
                1     // requestedTokens: 1 token per request
        );
    }

    /**
     * Stricter rate limiter for sensitive operations (payments, orders)
     */
    @Bean
    public RedisRateLimiter strictRateLimiter() {
        return new RedisRateLimiter(
                2,    // replenishRate: 2 requests per second
                5,    // burstCapacity: up to 5 requests in burst
                1     // requestedTokens: 1 token per request
        );
    }

    /**
     * Lenient rate limiter for read operations (product browsing)
     */
    @Bean
    public RedisRateLimiter lenientRateLimiter() {
        return new RedisRateLimiter(
                50,   // replenishRate: 50 requests per second
                100,  // burstCapacity: up to 100 requests in burst
                1     // requestedTokens: 1 token per request
        );
    }

    /**
     * Key resolver for user-based rate limiting.
     * Prefers an explicit user ID header (e.g., set by auth), falls back to client IP.
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");
            if (userId != null && !userId.isEmpty()) {
                return reactor.core.publisher.Mono.just(userId);
            }
            return reactor.core.publisher.Mono.just(
                    exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
            );
        };
    }

    /**
     * Key resolver for IP-based rate limiting.
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> reactor.core.publisher.Mono.just(
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
        );
    }

    /**
     * Custom Reactive Redis template for rate limiting
     * This enables proper serialization for rate limiting data
     * <p>
     * IMPORTANT: Must use ReactiveRedisConnectionFactory for reactive applications
     */
    @Bean
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {
        return new ReactiveRedisTemplate<>(
                connectionFactory,
                RedisSerializationContext.string()
        );
    }
}