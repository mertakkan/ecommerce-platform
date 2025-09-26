package com.ecommercehub.servicediscovery;

import com.netflix.discovery.shared.Application;
import com.netflix.eureka.EurekaServerContext;
import com.netflix.eureka.EurekaServerContextHolder;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Custom Health Indicator for Eureka Server
 * <p>
 * This provides detailed health information about the Eureka Server including:
 * - Number of registered services
 * - Status of each registered service
 * - Registry statistics
 * <p>
 * This is crucial for monitoring in production environments where you need
 * to know not just if Eureka is running, but what services are registered.
 */
@Slf4j
@Component
public class EurekaHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try {
            // Get the Eureka server context
            EurekaServerContext context = EurekaServerContextHolder.getInstance().getServerContext();

            if (context == null) {
                return Health.down()
                        .withDetail("reason", "EurekaServerContext is not initialized")
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

            // Determine health status
            Status status = totalInstances > 0 ? Status.UP : Status.OUT_OF_SERVICE;

            return Health.status(status)
                    .withDetails(details)
                    .build();

        } catch (Exception e) {
            log.error("Error checking Eureka health", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}