package com.ecommercehub.servicediscovery;

import com.netflix.discovery.shared.Application;
import com.netflix.eureka.EurekaServerContext;
import com.netflix.eureka.EurekaServerContextHolder;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Custom Health Indicator for Eureka Server Registry Details
 * <p>
 * This provides detailed health information about the Eureka Server including:
 * - Number of registered services
 * - Status of each registered service
 * - Registry statistics
 * <p>
 * Note: This is separate from Spring Cloud's built-in eurekaHealthIndicator
 * which focuses on Eureka client functionality.
 */
@Slf4j
@Component("eurekaServerHealthIndicator")  // Explicit bean name to avoid conflict
public class EurekaServerHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try {
            // Get the Eureka server context
            EurekaServerContext context = EurekaServerContextHolder.getInstance().getServerContext();

            if (context == null) {
                return Health.up()  // Changed from down to up during startup
                        .withDetail("status", "initializing")
                        .withDetail("reason", "EurekaServerContext is not yet initialized")
                        .build();
            }

            PeerAwareInstanceRegistry registry = context.getRegistry();

            // Collect registry statistics
            Map<String, Object> details = new HashMap<>();

            // Get all registered applications
            List<Application> apps = registry.getApplications().getRegisteredApplications();

            // Count total instances
            int totalInstances = apps.stream()
                    .mapToInt(app -> app.getInstances().size())
                    .sum();

            // Create a map of service names to instance counts
            Map<String, Integer> serviceInstanceCounts = apps.stream()
                    .collect(Collectors.toMap(
                            Application::getName,
                            app -> app.getInstances().size()
                    ));

            // Add details to health check
            details.put("registeredApplications", apps.size());
            details.put("totalInstances", totalInstances);
            details.put("services", serviceInstanceCounts);

            // Add registry metrics
            details.put("numberOfRenewsPerMin", registry.getNumOfRenewsPerMinThreshold());
            details.put("isSelfPreservationModeEnabled", registry.isSelfPreservationModeEnabled());
            details.put("status", "running");

            // For an empty registry (normal at startup), still consider it UP
            return Health.up()
                    .withDetails(details)
                    .build();

        } catch (Exception e) {
            log.error("Error checking Eureka server health", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}