package com.ecommercehub.configserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.config.server.encryption.TextEncryptorLocator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Administrative controller for Config Server utilities
 * <p>
 * This controller provides convenient endpoints for administrators to:
 * 1. Bulk encrypt configuration values
 * 2. Test encryption/decryption functionality
 * 3. Check encryption service health
 * <p>
 * Note: This is separate from the built-in /encrypt and /decrypt endpoints
 * that Spring Cloud Config provides automatically.
 */
@RestController
@RequestMapping("/admin/config")
public class ConfigAdminController {

    private final TextEncryptorLocator textEncryptorLocator;

    /**
     * Constructor injection for better testability
     */
    public ConfigAdminController(TextEncryptorLocator textEncryptorLocator) {
        this.textEncryptorLocator = textEncryptorLocator;
    }

    /**
     * Bulk encrypt multiple values at once
     * This is useful when setting up new environment configurations
     * <p>
     * Usage: POST http://localhost:8888/admin/config/bulk-encrypt
     * Body: {
     * "database.password": "myDbPassword",
     * "jwt.secret": "myJwtSecret",
     * "api.key": "myApiKey"
     * }
     * <p>
     * Response: {
     * "database.password": "AQA7P9FfRz8QJ...",
     * "jwt.secret": "BQB8Q0GgSz9RK...",
     * "api.key": "CQC9R1HhTz0SL..."
     * }
     */
    @PostMapping("/bulk-encrypt")
    public Map<String, String> bulkEncrypt(@RequestBody Map<String, String> plainValues) {
        Map<String, String> encryptedValues = new HashMap<>();

        plainValues.forEach((key, plainValue) -> {
            try {
                String encrypted = textEncryptorLocator.locate(null).encrypt(plainValue);
                encryptedValues.put(key, encrypted);
            } catch (Exception e) {
                encryptedValues.put(key, "ERROR: " + e.getMessage());
            }
        });

        return encryptedValues;
    }

    /**
     * Generate a ready-to-use configuration snippet
     * <p>
     * Usage: POST http://localhost:8888/admin/config/generate-config
     * Body: {
     * "service": "user-service",
     * "values": {
     * "spring.datasource.password": "myDbPassword",
     * "security.jwt.secret": "myJwtSecret"
     * }
     * }
     */
    @PostMapping("/generate-config")
    public Map<String, Object> generateConfigSnippet(@RequestBody ConfigGenerationRequest request) {
        Map<String, Object> result = new HashMap<>();
        Map<String, String> configSnippet = new HashMap<>();

        request.getValues().forEach((key, plainValue) -> {
            try {
                String encrypted = textEncryptorLocator.locate(null).encrypt(plainValue);
                configSnippet.put(key, "{cipher}" + encrypted);
            } catch (Exception e) {
                configSnippet.put(key, "ERROR: " + e.getMessage());
            }
        });

        result.put("service", request.getService());
        result.put("config", configSnippet);
        result.put("usage", "Copy the config values to your " + request.getService() + ".yml file");

        return result;
    }

    /**
     * Test the encryption/decryption round-trip
     * This helps verify that the encryption service is working correctly
     */
    @GetMapping("/test-encryption")
    public Map<String, Object> testEncryption() {
        Map<String, Object> result = new HashMap<>();

        try {
            String testValue = "test-encryption-" + System.currentTimeMillis();
            String encrypted = textEncryptorLocator.locate(null).encrypt(testValue);
            String decrypted = textEncryptorLocator.locate(null).decrypt(encrypted);

            result.put("status", decrypted.equals(testValue) ? "SUCCESS" : "FAILED");
            result.put("original", testValue);
            result.put("encrypted", encrypted);
            result.put("decrypted", decrypted);
            result.put("roundTripSuccess", decrypted.equals(testValue));

        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * Get information about the encryption configuration
     */
    @GetMapping("/encryption-info")
    public Map<String, Object> getEncryptionInfo() {
        Map<String, Object> info = new HashMap<>();

        try {
            // Test if encryption is working
            String test = textEncryptorLocator.locate(null).encrypt("test");
            info.put("encryptionEnabled", true);
            info.put("encryptorType", textEncryptorLocator.locate(null).getClass().getSimpleName());

        } catch (Exception e) {
            info.put("encryptionEnabled", false);
            info.put("error", e.getMessage());
        }

        info.put("builtInEndpoints", Map.of(
                "encrypt", "POST /encrypt (requires basic auth)",
                "decrypt", "POST /decrypt (requires basic auth)"
        ));

        return info;
    }

    /**
     * Request class for configuration generation
     */
    public static class ConfigGenerationRequest {
        private String service;
        private Map<String, String> values;

        // Constructors
        public ConfigGenerationRequest() {
        }

        public ConfigGenerationRequest(String service, Map<String, String> values) {
            this.service = service;
            this.values = values;
        }

        // Getters and setters
        public String getService() {
            return service;
        }

        public void setService(String service) {
            this.service = service;
        }

        public Map<String, String> getValues() {
            return values;
        }

        public void setValues(Map<String, String> values) {
            this.values = values;
        }
    }
}