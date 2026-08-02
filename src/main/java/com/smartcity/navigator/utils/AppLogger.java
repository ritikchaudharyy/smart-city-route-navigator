package com.smartcity.navigator.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global application logger handling information, warnings, and system errors.
 * This class is fully self-contained and handles all logging overloads.
 */
public class AppLogger {

    private static final Logger logger = Logger.getLogger("SmartCityNavigator");

    // Prevent instantiation
    private AppLogger() {}

    /**
     * Logs general informational messages.
     * @param message The message to log
     */
    public static void info(String message) {
        logger.log(Level.INFO, message);
    }

    /**
     * Logs system warning messages.
     * @param message The warning message to log
     */
    public static void warn(String message) {
        logger.log(Level.WARNING, message);
    }

    /**
     * Logs simple severe error messages.
     * @param message The error message to log
     */
    public static void error(String message) {
        logger.log(Level.SEVERE, message);
    }

    /**
     * Logs severe error messages with an associated exception/throwable.
     * Handles Exception, RuntimeException, InterruptedException, etc.
     * * @param message   The error context message
     * @param throwable The exception thrown
     */
    public static void error(String message, Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }
}