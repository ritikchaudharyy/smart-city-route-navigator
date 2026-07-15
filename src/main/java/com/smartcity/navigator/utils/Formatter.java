package com.smartcity.navigator.utils;

import java.util.Locale;

import com.smartcity.navigator.model.PathResult;

/**
 * Formats domain values ({@link PathResult}, raw distances) into
 * display-ready strings for the UI, keeping presentation formatting out
 * of both the algorithm and the UI's action listeners.
 *
 * @author Smart City Route Navigator Team
 */
public final class Formatter {

    private Formatter() {
        // Utility class: not instantiable.
    }

    /** 1 kilometer in miles, used when the user prefers imperial units. */
    private static final double KM_TO_MILES = 0.621371;

    /**
     * Formats a distance (always supplied in kilometers, the graph's
     * native unit) to two decimal places with a unit suffix, converting
     * to miles first if the user has selected that preference in
     * {@code SettingsDialog}. Infinite distances (no path found) render
     * as {@code "N/A"}.
     */
    public static String formatDistance(double distanceKm) {
        if (Double.isInfinite(distanceKm) || Double.isNaN(distanceKm)) {
            return "N/A";
        }
        if (AppSettings.getInstance().isMetricUnits()) {
            return String.format(Locale.US, "%.2f %s", distanceKm, Constants.DISTANCE_UNIT);
        }
        return String.format(Locale.US, "%.2f mi", distanceKm * KM_TO_MILES);
    }

    /**
     * Builds the full multi-line summary shown in the result panel: the
     * arrow-separated route followed by the total distance, or the
     * failure message if no path was found.
     */
    public static String formatRouteSummary(PathResult result) {
        if (result == null) {
            return "";
        }
        if (!result.isPathFound()) {
            return result.getMessage();
        }
        return result.getFormattedRoute() + System.lineSeparator()
                + "Total Distance: " + formatDistance(result.getTotalDistance());
    }

    /**
     * Formats a short status-bar message summarizing the result in one line.
     */
    public static String formatStatusMessage(PathResult result) {
        if (result == null) {
            return "Ready";
        }
        return result.isPathFound()
                ? "Route found — " + formatDistance(result.getTotalDistance())
                : "No route found: " + result.getMessage();
    }

    /**
     * Formats a status-bar message for load/refresh operations using the
     * correct singular/plural form for the location count.
     */
    public static String formatLocationStatus(String prefix, int locationCount) {
        String noun = locationCount == 1 ? "location" : "locations";
        return prefix + " — " + locationCount + " " + noun + " loaded";
    }
}
