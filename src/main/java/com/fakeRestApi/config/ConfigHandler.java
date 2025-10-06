package com.fakeRestApi.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigHandler {

    private static final Properties PROPERTIES = loadProperties();

    private ConfigHandler() {}

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

    public static String getBaseUrl() {
        return getProperty("base.api.url");
    }

    public static String getLogLevel() {
        return System.getProperty("log.level",
                PROPERTIES.getProperty("log.level", "INFO"));
    }

    private static String getProperty(String key) {
        return System.getProperty(key, PROPERTIES.getProperty(key));
    }
}