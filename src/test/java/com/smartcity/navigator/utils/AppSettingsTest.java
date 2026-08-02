package com.smartcity.navigator.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.prefs.Preferences;

import org.junit.jupiter.api.Test;

class AppSettingsTest {

    @Test
    void updatesAndPersistsTheUserPreferences() {
        AppSettings settings = AppSettings.getInstance();
        boolean originalDarkMode = settings.isDarkMode();
        boolean originalMetricUnits = settings.isMetricUnits();
        Preferences preferences = Preferences.userNodeForPackage(AppSettings.class);

        try {
            settings.setDarkMode(!originalDarkMode);
            settings.setMetricUnits(!originalMetricUnits);

            assertEquals(!originalDarkMode, settings.isDarkMode());
            assertEquals(!originalMetricUnits, settings.isMetricUnits());
            assertEquals(!originalDarkMode, preferences.getBoolean("darkMode", originalDarkMode));
            assertEquals(!originalMetricUnits, preferences.getBoolean("metricUnits", originalMetricUnits));
        } finally {
            settings.setDarkMode(originalDarkMode);
            settings.setMetricUnits(originalMetricUnits);
        }
    }
}
