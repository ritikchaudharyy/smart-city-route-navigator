package com.smartcity.navigator.utils;

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

    private boolean darkMode = false;
    private boolean metricUnits = true;

    private AppSettings() {
        // Singleton: use getInstance().
    }

    public static AppSettings getInstance() {
        return INSTANCE;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }

    /**
     * @return {@code true} for kilometers, {@code false} for miles
     */
    public boolean isMetricUnits() {
        return metricUnits;
    }

    public void setMetricUnits(boolean metricUnits) {
        this.metricUnits = metricUnits;
    }
}
