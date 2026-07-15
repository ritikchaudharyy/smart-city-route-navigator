package com.smartcity.navigator.utils;

import com.smartcity.navigator.graph.CityGraph;

/**
 * Small, reusable helper functions that don't belong to validation or
 * formatting specifically, used by the service layer and future UI
 * dialogs (e.g. an "Add Location" dialog needs a fresh, unused id).
 *
 * @author Smart City Route Navigator Team
 */
public final class Helpers {

    private Helpers() {
        // Utility class: not instantiable.
    }

    /**
     * Generates the next unused location id in the graph, of the form
     * {@code "L" + n} (e.g. {@code "L9"}), by counting upward from the
     * current location count until an unused id is found.
     *
     * @param graph the graph to check ids against
     * @return an id guaranteed not to already exist in {@code graph}
     */
    public static String generateNextLocationId(CityGraph graph) {
        int counter = graph.locationCount() + 1;
        String candidate;
        do {
            candidate = Constants.LOCATION_ID_PREFIX + counter;
            counter++;
        } while (graph.hasLocation(candidate));
        return candidate;
    }

    /**
     * @return the trimmed string, or {@code ""} if {@code value} is null
     */
    public static String nullSafeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Safely parses a string to a double, e.g. from a text field.
     *
     * @param value        the string to parse
     * @param defaultValue value returned if parsing fails
     * @return the parsed double, or {@code defaultValue} if {@code value} isn't a valid number
     */
    public static double safeParseDouble(String value, double defaultValue) {
        try {
            return Double.parseDouble(nullSafeTrim(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
