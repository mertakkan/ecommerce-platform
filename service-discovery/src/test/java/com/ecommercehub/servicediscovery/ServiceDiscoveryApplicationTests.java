package com.ecommercehub.servicediscovery;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ServiceDiscoveryApplicationTests {

    @Test
    void contextLoads() {
        // This test verifies that the application context loads successfully
    }
}