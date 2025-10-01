package com.ecommercehub.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global filter for logging outgoing responses
 */
@Component
@Slf4j
public class ResponseLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            Long startTime = exchange.getAttribute("startTime");

            long duration = startTime != null ?
                    System.currentTimeMillis() - startTime : 0;

            // Mask sensitive headers
            Map<String, String> maskedResponseHeaders = response.getHeaders().toSingleValueMap().entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> {
                                String key = e.getKey();
                                if ("set-cookie".equalsIgnoreCase(key)) {
                                    return "***";
                                }
                                return e.getValue();
                            }
                    ));

            // Log response details
            log.info("=== Outgoing Response ===");
            log.info("Request ID: {}", exchange.getRequest().getHeaders().getFirst("X-Gateway-Request-Id"));
            log.info("Status Code: {}", response.getStatusCode());
            log.info("Response Headers: {}", maskedResponseHeaders);
            log.info("Duration: {} ms", duration);

            // Log performance metrics
            if (duration > 1000) {
                log.warn("Slow request detected - Duration: {} ms, URI: {}",
                        duration, exchange.getRequest().getURI());
            }
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE; // Execute last
    }
}