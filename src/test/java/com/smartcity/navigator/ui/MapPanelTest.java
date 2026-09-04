package com.smartcity.navigator.ui;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.smartcity.navigator.graph.CityGraph;
import com.smartcity.navigator.model.Edge;
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

    @Test
    void hitTestFindsNodesAndRoadsAfterViewportFitting() {
        CityGraph graph = new CityGraph();
        graph.addLocation(new Location("A", "Home", 40, 90));
        graph.addLocation(new Location("B", "Market", 260, 90));
        graph.addRoad("A", "B", 4);

        MapPanel mapPanel = new MapPanel();
        mapPanel.setSize(640, 420);
        mapPanel.setGraph(graph);

        MapPanel.Hit nodeHit = mapPanel.hitTest(113, 210);
        assertEquals("A", nodeHit.location.getId());

        MapPanel.Hit edgeHit = mapPanel.hitTest(320, 210);
        assertEquals(new Edge("A", "B", 4), edgeHit.edge);
    }
}
