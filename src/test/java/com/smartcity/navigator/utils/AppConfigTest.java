package com.smartcity.navigator.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AppConfigTest {

    @Test
    void loadsCoreApplicationPropertiesFromClasspath() {
        assertEquals("Smart City Route Navigator", AppConfig.getProperty("app.name"));
        assertEquals("1.0.0", AppConfig.getProperty("app.version"));
        assertEquals("Ritik Kumar", AppConfig.getProperty("app.author"));
        assertEquals("light", AppConfig.getProperty("app.theme"));
    }

    @Test
    void fallsBackToDefaultValueWhenPropertyIsMissing() {
        assertEquals("fallback", AppConfig.getProperty("app.missing", "fallback"));
    }

    @Test
    void exposesConfigValuesThroughConstants() {
        assertNotNull(Constants.APP_TITLE);
        assertNotNull(Constants.APP_VERSION);
    }
}
