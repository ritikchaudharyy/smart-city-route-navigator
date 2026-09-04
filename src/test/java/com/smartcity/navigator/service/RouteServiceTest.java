package com.smartcity.navigator.service;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.smartcity.navigator.graph.CityGraph;
import com.smartcity.navigator.graph.GraphBuilder;
import com.smartcity.navigator.graph.GraphLoadException;
import com.smartcity.navigator.model.PathResult;

/**
 * Unit tests for {@link RouteService}: validation delegation for route
 * requests, location/road mutation with validation, and graph
 * load/save round-tripping through a temporary file.
 *
 * @author Smart City Route Navigator Team
 */
class RouteServiceTest {

    private RouteService service;

    @BeforeEach
    void setUp() {
        CityGraph graph = new GraphBuilder()
                .withLocation("A", "Home", 0, 0)
                .withLocation("B", "Market", 10, 0)
                .withLocation("C", "Mall", 20, 0)
                .withRoad("A", "B", 5)
                .withRoad("B", "C", 5)
                .build();
        service = new RouteService(graph);
    }

    @Test
    void findRoute_validSelection_returnsShortestPath() {
        PathResult result = service.findRoute("A", "C");
        assertTrue(result.isPathFound());
        assertEquals(10.0, result.getTotalDistance(), 0.0001);
    }

    @Test
    void findRoute_blankSource_returnsValidationFailure() {
        PathResult result = service.findRoute("", "C");
        assertFalse(result.isPathFound());
        assertTrue(result.getMessage().toLowerCase().contains("source"));
    }

    @Test
    void findRoute_blankDestination_returnsValidationFailure() {
        PathResult result = service.findRoute("A", "");
        assertFalse(result.isPathFound());
        assertTrue(result.getMessage().toLowerCase().contains("destination"));
    }

    @Test
    void findRoute_sameSourceAndDestination_returnsValidationFailure() {
        PathResult result = service.findRoute("A", "A");
        assertFalse(result.isPathFound());
        assertTrue(result.getMessage().toLowerCase().contains("different"));
    }

    @Test
    void findRoute_nullSelections_returnsValidationFailure() {
        PathResult result = service.findRoute(null, null);
        assertFalse(result.isPathFound());
    }

    @Test
    void addLocation_makesItAvailableForRouting() {
        service.addLocation("D", "Airport", 30, 0);
        assertTrue(service.getGraph().hasLocation("D"));
        assertEquals(4, service.getAllLocations().size());
    }

    @Test
    void addLocation_duplicateId_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.addLocation("A", "Duplicate", 0, 0));
    }

    @Test
    void addLocation_trimsWhitespaceAroundIdAndName() {
        service.addLocation("  D  ", "  Airport  ", 30, 0);
        assertTrue(service.getGraph().hasLocation("D"));
        assertEquals("Airport", service.getGraph().getLocation("D").orElseThrow().getName());
    }

    @Test
    void addRoad_connectsExistingLocations() {
        service.addLocation("D", "Airport", 30, 0);
        service.addRoad("A", "D", 15);
        assertTrue(service.getGraph().hasRoad("A", "D"));
    }

    @Test
    void addRoad_nonPositiveWeight_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.addRoad("A", "B", -1));
    }

    @Test
    void removeLocation_removesItFromGraph() {
        boolean removed = service.removeLocation("B");
        assertTrue(removed);
        assertFalse(service.getGraph().hasLocation("B"));
    }

    @Test
    void removeRoad_disconnectsLocations() {
        boolean removed = service.removeRoad("A", "B");
        assertTrue(removed);
        assertFalse(service.getGraph().hasRoad("A", "B"));
    }

    @Test
    void saveThenLoadGraph_roundTripsCorrectly(@TempDir File tempDir) throws GraphLoadException {
        File file = new File(tempDir, "saved-city.dat");
        service.saveGraphToFile(file);

        RouteService loadedService = new RouteService(new GraphBuilder().withLocation("placeholder", "x").build());
        loadedService.loadGraphFromFile(file);

        assertEquals(3, loadedService.getAllLocations().size());
        assertTrue(loadedService.getGraph().hasLocation("A"));
        assertTrue(loadedService.getGraph().hasRoad("A", "B"));

        PathResult result = loadedService.findRoute("A", "C");
        assertTrue(result.isPathFound());
        assertEquals(10.0, result.getTotalDistance(), 0.0001);
    }

    @Test
    void resetToDefaultCity_loadsBundledData() throws GraphLoadException {
        service.resetToDefaultCity();
        assertFalse(service.isEmpty());
        assertTrue(service.getGraph().hasLocation("L1")); // Home, from default-city.dat
    }

    @Test
    void createNewCity_startsWithAnEmptyGraph() {
        service.createNewCity();
        assertTrue(service.isEmpty());
        assertEquals(0, service.getGraph().roadCount());
    }

    @Test
    void constructor_rejectsNullGraph() {
        assertThrows(IllegalArgumentException.class, () -> new RouteService((CityGraph) null));
    }
}
