package com.smartcity.navigator.ui;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.smartcity.navigator.graph.CityGraph;
import com.smartcity.navigator.model.Location;

class MapPanelTest {

    @Test
    void rendersAndHighlightsAGraphInAnOffscreenCanvas() {
        CityGraph graph = new CityGraph();
        Location home = new Location("L1", "Home", 40, 90);
        Location hospital = new Location("L2", "Hospital", 260, 130);
        Location airport = new Location("L3", "Airport", 180, 280);
        graph.addLocation(home);
        graph.addLocation(hospital);
        graph.addLocation(airport);
        graph.addRoad("L1", "L2", 4);
        graph.addRoad("L2", "L3", 6);

        MapPanel mapPanel = new MapPanel();
        mapPanel.setSize(640, 420);
        mapPanel.setGraph(graph);
        mapPanel.highlightRoute(List.of(home, hospital, airport));

        BufferedImage image = new BufferedImage(640, 420, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            assertDoesNotThrow(() -> mapPanel.paint(graphics));
        } finally {
            graphics.dispose();
        }
    }
}
