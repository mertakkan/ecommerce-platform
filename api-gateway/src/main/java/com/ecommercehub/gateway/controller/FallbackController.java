package com.ecommercehub.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Fallback controller for circuit breaker responses
 * When a service is down, these endpoints return graceful degradation responses
 */
@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {

    @GetMapping("/user-service")
    public Mono<ResponseEntity<Map<String, Object>>> userServiceFallback() {
        log.warn("User service fallback triggered");
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "User service is temporarily unavailable",
                        "timestamp", LocalDateTime.now(),
                        "suggestion", "Please try again later"
                )));
    }

    @GetMapping("/product-service")
    public Mono<ResponseEntity<Map<String, Object>>> productServiceFallback() {
        log.warn("Product service fallback triggered");
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Product service is temporarily unavailable",
                        "timestamp", LocalDateTime.now(),
                        "suggestion", "Browse cached products or try again later"
                )));
    }

    @GetMapping("/order-service")
    public Mono<ResponseEntity<Map<String, Object>>> orderServiceFallback() {
        log.warn("Order service fallback triggered");
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Order service is temporarily unavailable",
                        "timestamp", LocalDateTime.now(),
                        "suggestion", "Your cart is saved. Please try placing the order later"
                )));
    }

    @GetMapping("/payment-service")
    public Mono<ResponseEntity<Map<String, Object>>> paymentServiceFallback() {
        log.warn("Payment service fallback triggered");
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Payment service is temporarily unavailable",
                        "timestamp", LocalDateTime.now(),
                        "suggestion", "Please try again in a few minutes"
                )));
    }

    // Add other service fallbacks...
}