package com.ecommercehub.servicediscovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Service Discovery Server Application
 * <p>
 * This is the central registry where all microservices register themselves.
 * It uses Netflix Eureka, which implements the client-server model where:
 * - This application is the Eureka Server (registry)
 * - Other microservices are Eureka Clients that register with this server
 * <p>
 * Key Concepts:
 * 1. Service Registration: Services register their network location
 * 2. Service Discovery: Services query to find other services
 * 3. Health Checking: Regular heartbeats ensure service availability
 * 4. Load Balancing: Multiple instances of a service can register
 */
@SpringBootApplication
@EnableEurekaServer  // This annotation enables the Eureka Server functionality
public class ServiceDiscoveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceDiscoveryApplication.class, args);
    }
}