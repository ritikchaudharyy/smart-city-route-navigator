package com.smartcity.navigator.graph;

/**
 * Checked exception thrown when a {@link CityGraph} cannot be loaded from
 * or saved to a data source — for example, a malformed file, a road
 * referencing an unknown location, or an unreadable/unwritable file path.
 * <p>
 * Kept as a single, specific checked exception type so the UI layer can
 * catch one thing and show a clear dialog message (per the "Show proper
 * dialog messages" requirement) instead of leaking {@code IOException} or
 * {@code NumberFormatException} details to the user.
 *
 * @author Smart City Route Navigator Team
 */
public class GraphLoadException extends Exception {

    public GraphLoadException(String message) {
        super(message);
    }

    public GraphLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
