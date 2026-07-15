package com.smartcity.navigator.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads application-level configuration values from classpath resources.
 * This keeps the UI and service layers from depending on hardcoded literals
 * for common metadata and defaults.
 */
public final class AppConfig {

    private static final String CONFIG_RESOURCE = "/app.properties";
    private static final Properties PROPERTIES = loadProperties();

    private AppConfig() {
        // Utility class.
    }

    public static String getProperty(String key) {
        return PROPERTIES.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return PROPERTIES.getProperty(key, defaultValue);
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = AppConfig.class.getResourceAsStream(CONFIG_RESOURCE)) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException ignored) {
            // Fall back to empty properties if the resource cannot be read.
        }
        return properties;
    }
}
