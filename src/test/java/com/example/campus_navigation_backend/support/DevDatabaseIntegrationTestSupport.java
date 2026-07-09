package com.example.campus_navigation_backend.support;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Common setup for integration tests that run against real development database data.
 */
@Tag("dev-db")
@SpringBootTest
@ActiveProfiles("dev-db-test")
public abstract class DevDatabaseIntegrationTestSupport {

    private static final Properties LOCAL_PROPERTIES = loadLocalProperties();

    /**
     * Registers development database and scenario properties before the Spring context is loaded.
     *
     * @param registry dynamic property registry
     */
    @DynamicPropertySource
    static void registerDevDatabaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> optionalValue("DEV_DB_URL", "DB_URL", "jdbc:postgresql://localhost:5432/hanyang_map"));
        registry.add("spring.datasource.username", () -> optionalValue("DEV_DB_USERNAME", "DB_USERNAME", "postgres"));
        registry.add("spring.datasource.password", () -> optionalValue("DEV_DB_PASSWORD", "DB_PASSWORD", ""));

        registry.add("dev.test.start-lon", () -> requiredValue("DEV_TEST_START_LON"));
        registry.add("dev.test.start-lat", () -> requiredValue("DEV_TEST_START_LAT"));
        registry.add("dev.test.start-ele", () -> optionalValue("DEV_TEST_START_ELE", "0.0"));
        registry.add("dev.test.destination-building", () -> requiredValue("DEV_TEST_DESTINATION_BUILDING"));
    }

    private static String optionalValue(String primaryName, String fallbackName, String defaultValue) {
        String value = readValue(primaryName);
        if (!value.isBlank()) {
            return value;
        }

        value = readValue(fallbackName);
        if (!value.isBlank()) {
            return value;
        }

        return defaultValue;
    }

    private static String optionalValue(String name, String defaultValue) {
        String value = readValue(name);
        if (!value.isBlank()) {
            return value;
        }

        return defaultValue;
    }

    private static String requiredValue(String name) {
        String value = readValue(name);
        if (!value.isBlank()) {
            return value;
        }

        throw new IllegalStateException("Dev DB integration test requires " + name + " as an environment variable or JVM system property.");
    }

    private static String readValue(String name) {
        String systemProperty = System.getProperty(name);
        if (systemProperty != null) {
            return systemProperty.trim();
        }

        String environmentValue = System.getenv(name);
        if (environmentValue != null) {
            return environmentValue.trim();
        }

        String localPropertyValue = LOCAL_PROPERTIES.getProperty(name);
        if (localPropertyValue != null) {
            return localPropertyValue.trim();
        }

        return "";
    }

    private static Properties loadLocalProperties() {
        Path localPropertiesPath = Path.of("src", "test", "resources", "dev-db-test.local.properties");
        Properties properties = new Properties();

        if (!Files.exists(localPropertiesPath)) {
            return properties;
        }

        try (InputStream inputStream = Files.newInputStream(localPropertiesPath);
             InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            properties.load(reader);
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + localPropertiesPath, e);
        }
    }
}
