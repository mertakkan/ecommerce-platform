package com.ecommercehub.servicediscovery;

import com.netflix.eureka.EurekaServerContext;
import com.netflix.eureka.EurekaServerContextHolder;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metrics Configuration for Eureka Server
 * <p>
 * This exposes custom metrics that can be consumed by Prometheus/Grafana:
 * - eureka.services.count: Number of registered services
 * - eureka.instances.count: Total number of instances
 * - eureka.self.preservation.enabled: Whether self-preservation mode is active
 * <p>
 * These metrics are crucial for:
 * 1. Alerting when services go down
 * 2. Capacity planning
 * 3. Understanding system health
 */
@Slf4j
@Configuration
public class EurekaMetricsConfiguration {

    @Bean
    MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> {
            registry.config().commonTags("application", "eureka-server");

            // Register custom gauges for Eureka metrics
            Gauge.builder("eureka.services.count", this::getRegisteredServicesCount)
                    .description("Number of registered services in Eureka")
                    .register(registry);

            Gauge.builder("eureka.instances.count", this::getTotalInstancesCount)
                    .description("Total number of instances across all services")
                    .register(registry);

            Gauge.builder("eureka.self.preservation.enabled", this::isSelfPreservationEnabled)
                    .description("Whether Eureka is in self-preservation mode")
                    .register(registry);
        };
    }

    private double getRegisteredServicesCount() {
        try {
            EurekaServerContext context = EurekaServerContextHolder.getInstance().getServerContext();
            if (context != null && context.getRegistry() != null) {
                return context.getRegistry().getApplications().size();
            }
        } catch (Exception e) {
            log.debug("Could not get registered services count", e);
        }
        return 0;
    }

    private double getTotalInstancesCount() {
        try {
            EurekaServerContext context = EurekaServerContextHolder.getInstance().getServerContext();
            if (context != null && context.getRegistry() != null) {
                return context.getRegistry().getApplications()
                        .getRegisteredApplications()
                        .stream()
                        .mapToInt(app -> app.getInstances().size())
                        .sum();
            }
        } catch (Exception e) {
            log.debug("Could not get total instances count", e);
        }
        return 0;
    }

    private double isSelfPreservationEnabled() {
        try {
            EurekaServerContext context = EurekaServerContextHolder.getInstance().getServerContext();
            if (context != null && context.getRegistry() != null) {
                return context.getRegistry().isSelfPreservationModeEnabled() ? 1.0 : 0.0;
            }
        } catch (Exception e) {
            log.debug("Could not check self-preservation mode", e);
        }
        return 0;
    }
}