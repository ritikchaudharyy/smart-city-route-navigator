package com.smartcity.navigator.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centralized logger for application events and startup diagnostics.
 * The implementation uses java.util.logging so the project remains dependency-free
 * while still offering a professional, configurable logging surface.
 */
public final class AppLogger {

    private static final Logger LOGGER = Logger.getLogger("com.smartcity.navigator");

    private AppLogger() {
        // Utility class.
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static void info(String message) {
        LOGGER.info(message);
    }

    public static void warning(String message) {
        LOGGER.warning(message);
    }

    public static void severe(String message) {
        LOGGER.log(Level.SEVERE, message);
    }

    public static void severe(String message, Throwable throwable) {
        LOGGER.log(Level.SEVERE, message, throwable);
    }
}
