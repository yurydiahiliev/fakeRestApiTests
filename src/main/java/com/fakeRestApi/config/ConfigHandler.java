package com.fakeRestApi.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads and provides access to configuration values from the api.properties file.
 * Supports system property overrides for flexible runtime configuration.
 */
public class ConfigHandler {

    private static final Properties PROPERTIES = loadProperties();

    /** Private constructor to prevent instantiation */
    private ConfigHandler() {}

    /**
     * Loads configuration values from api.properties located in the resources folder.
     * @return loaded Properties object
     * @throws RuntimeException if the file cannot be found or read
     */
    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = ConfigHandler.class
                .getClassLoader()
                .getResourceAsStream("api.properties")) {

            if (input == null) {
                throw new IllegalStateException("Can not find api.properties in resources folder!");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load api.properties file", e);
        }
        return properties;
    }

    /**
     * Returns the base API URL from configuration.
     * @return base API URL string
     */
    public static String getBaseUrl() {
        return getProperty("base.api.url");
    }

    /**
     * Returns the configured log level.
     * Checks for a system property override, otherwise falls back to api.properties.
     * Defaults to INFO if not specified.
     * @return log level string
     */
    public static String getLogLevel() {
        return System.getProperty("log.level", PROPERTIES.getProperty("log.level", "INFO"));
    }

    /**
     * Retrieves a property value by key with optional system override.
     * @param key property key name
     * @return property value string
     */
    private static String getProperty(String key) {
        return System.getProperty(key, PROPERTIES.getProperty(key));
    }
}