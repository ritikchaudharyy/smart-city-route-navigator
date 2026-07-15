package com.smartcity.navigator.utils;

/**
 * Centralized, immutable application constants.
 * <p>
 * Keeping these values in one place (rather than scattered literals
 * throughout the UI and service layers) satisfies the "no hardcoded
 * values" code standard and makes future tweaks — window sizing, the
 * distance unit, id prefixes — a single-line change.
 *
 * @author Smart City Route Navigator Team
 */
public final class Constants {

    private Constants() {
        // Utility class: not instantiable.
    }

    /** Application display name, used in the title bar and About dialog. */
    public static final String APP_TITLE = "Smart City Route Navigator";

    /** Current application version, shown in the About dialog. */
    public static final String APP_VERSION = "1.0.0";

    /** Default main window width in pixels. */
    public static final int DEFAULT_WINDOW_WIDTH = 1100;

    /** Default main window height in pixels. */
    public static final int DEFAULT_WINDOW_HEIGHT = 720;

    /** Unit label appended to displayed distances. */
    public static final String DISTANCE_UNIT = "km";

    /** Prefix used when auto-generating new location ids (e.g. "L9"). */
    public static final String LOCATION_ID_PREFIX = "L";

    /** Splash screen minimum display time in milliseconds, so it's readable rather than a flicker. */
    public static final int SPLASH_SCREEN_DURATION_MS = 1500;
}
