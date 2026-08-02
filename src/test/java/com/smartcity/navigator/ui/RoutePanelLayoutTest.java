package com.smartcity.navigator.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Dimension;

import org.junit.jupiter.api.Test;

import com.smartcity.navigator.graph.CityGraph;
import com.smartcity.navigator.model.Location;
import com.smartcity.navigator.service.RouteService;
import com.smartcity.navigator.service.ai.GeminiService;

class RoutePanelLayoutTest {

    @Test
    void keepsTheCompleteAiAndManualControlAreaVisibleToItsScrollableParent() {
        CityGraph graph = new CityGraph();
        graph.addLocation(new Location("L1", "Home", 0, 0));
        graph.addLocation(new Location("L2", "Hospital", 100, 100));

        RoutePanel panel = new RoutePanel(null, new RouteService(graph), new GeminiService());
        Dimension preferredSize = panel.getPreferredSize();

        assertTrue(preferredSize.height > 400,
                "The route panel must reserve enough height for the AI and manual planner cards.");
        assertTrue(panel.getMaximumSize().height >= preferredSize.height,
                "The parent layout must not collapse the route panel below its full control area.");
    }
}
