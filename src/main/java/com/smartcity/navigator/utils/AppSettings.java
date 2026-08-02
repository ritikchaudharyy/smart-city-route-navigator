package com.smartcity.navigator.utils;

import java.util.prefs.Preferences;

/**
 * Application-wide user preferences: dark mode and preferred distance
 * unit. A single shared instance (simple singleton) is used so that
 * {@code SettingsDialog} can update preferences and every other
 * component ({@code Formatter}, {@code MapPanel}) can read the current
 * value without threading a settings object through every constructor.
 * <p>
 * This class intentionally holds no Swing/UI code — it is a plain data
 * holder, keeping {@code utils} free of any dependency on {@code ui}.
 *
 * @author Smart City Route Navigator Team
 */
public final class AppSettings {

    private static final AppSettings INSTANCE = new AppSettings();
    private static final String DARK_MODE_KEY = "darkMode";
    private static final String METRIC_UNITS_KEY = "metricUnits";

    private final Preferences preferences;

    private boolean darkMode;
    private boolean metricUnits;

    private AppSettings() {
        preferences = openPreferences();
        darkMode = readBoolean(DARK_MODE_KEY, false);
        metricUnits = readBoolean(METRIC_UNITS_KEY, true);
    }

    public static AppSettings getInstance() {
        return INSTANCE;
    }

    public synchronized boolean isDarkMode() {
        return darkMode;
    }

    public synchronized void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
        writeBoolean(DARK_MODE_KEY, darkMode);
    }

    /**
     * @return {@code true} for kilometers, {@code false} for miles
     */
    public synchronized boolean isMetricUnits() {
        return metricUnits;
    }

    public synchronized void setMetricUnits(boolean metricUnits) {
        this.metricUnits = metricUnits;
        writeBoolean(METRIC_UNITS_KEY, metricUnits);
    }

    private boolean readBoolean(String key, boolean defaultValue) {
        if (preferences == null) {
            return defaultValue;
        }
        try {
            return preferences.getBoolean(key, defaultValue);
        } catch (SecurityException exception) {
            AppLogger.warn("Application preferences are unavailable; using the default " + key + " setting.");
            return defaultValue;
        }
    }

    private void writeBoolean(String key, boolean value) {
        if (preferences == null) {
            return;
        }
        try {
            preferences.putBoolean(key, value);
        } catch (SecurityException exception) {
            AppLogger.warn("Application preferences could not be saved; the setting will apply only for this session.");
        }
    }

    private Preferences openPreferences() {
        try {
            return Preferences.userNodeForPackage(AppSettings.class);
        } catch (SecurityException exception) {
            AppLogger.warn("Application preferences are unavailable; settings will apply only for this session.");
            return null;
        }
    }
}
