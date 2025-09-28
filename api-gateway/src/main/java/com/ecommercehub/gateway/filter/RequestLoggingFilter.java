package com.ecommercehub.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global filter for logging incoming requests
 * Implements Ordered to control filter execution order
 */
@Component
@Slf4j
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Log request details
        log.info("=== Incoming Request ===");
        log.info("Request ID: {}", request.getHeaders().getFirst("X-Gateway-Request-Id"));
        log.info("Method: {}", request.getMethod());
        log.info("URI: {}", request.getURI());
        log.info("Remote Address: {}", request.getRemoteAddress());
        log.info("Headers: {}", request.getHeaders().toSingleValueMap());

        // Add request start time for performance monitoring
        exchange.getAttributes().put("startTime", System.currentTimeMillis());

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE; // Execute first
    }
}