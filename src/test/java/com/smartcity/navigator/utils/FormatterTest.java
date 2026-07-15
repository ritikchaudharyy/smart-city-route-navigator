package com.smartcity.navigator.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class FormatterTest {

    @Test
    void formatLocationStatusUsesCountAndSingularPlural() {
        assertEquals("Ready — 3 locations loaded", Formatter.formatLocationStatus("Ready", 3));
        assertEquals("Refreshed — 1 location loaded", Formatter.formatLocationStatus("Refreshed", 1));
    }
}
