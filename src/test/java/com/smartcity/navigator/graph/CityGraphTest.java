package com.smartcity.navigator.graph;

import com.smartcity.navigator.model.Edge;
import com.smartcity.navigator.model.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link CityGraph}: location/road CRUD, undirected
 * traversal, dynamic insertion and deletion, and edge deduplication.
 *
 * @author Smart City Route Navigator Team
 */
class CityGraphTest {

    private CityGraph graph;

    @BeforeEach
    void setUp() {
        graph = new CityGraph();
        graph.addLocation(new Location("A", "Alpha"));
        graph.addLocation(new Location("B", "Beta"));
        graph.addLocation(new Location("C", "Gamma"));
    }

    @Test
    void addLocation_makesItRetrievable() {
        assertTrue(graph.hasLocation("A"));
        assertEquals("Alpha", graph.getLocation("A").orElseThrow().getName());
        assertEquals(3, graph.locationCount());
    }

    @Test
    void addRoad_isTraversableFromBothEndpoints() {
        graph.addRoad("A", "B", 5.0);

        assertTrue(graph.hasRoad("A", "B"));
        assertTrue(graph.hasRoad("B", "A")); // undirected: reachable from either side
        assertEquals(1, graph.getNeighbors("A").size());
        assertEquals(1, graph.getNeighbors("B").size());
    }

    @Test
    void addRoad_toUnknownLocation_throws() {
        assertThrows(IllegalArgumentException.class, () -> graph.addRoad("A", "Z", 5.0));
    }

    @Test
    void addRoad_duplicateRoad_throws() {
        graph.addRoad("A", "B", 5.0);
        assertThrows(IllegalArgumentException.class, () -> graph.addRoad("A", "B", 7.0));
        // Reversed order is the same undirected road and must also be rejected.
        assertThrows(IllegalArgumentException.class, () -> graph.addRoad("B", "A", 7.0));
    }

    @Test
    void addRoad_selfLoop_throws() {
        assertThrows(IllegalArgumentException.class, () -> graph.addRoad("A", "A", 5.0));
    }

    @Test
    void addRoad_nonPositiveWeight_throws() {
        assertThrows(IllegalArgumentException.class, () -> graph.addRoad("A", "B", 0.0));
        assertThrows(IllegalArgumentException.class, () -> graph.addRoad("A", "B", -3.0));
        assertThrows(IllegalArgumentException.class, () -> graph.addRoad("A", "B", Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> graph.addRoad("A", "B", Double.POSITIVE_INFINITY));
    }

    @Test
    void addLocation_nonFiniteCoordinates_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> graph.addLocation(new Location("D", "Delta", Double.NaN, 20.0)));
        assertThrows(IllegalArgumentException.class,
                () -> graph.addLocation(new Location("D", "Delta", 20.0, Double.NEGATIVE_INFINITY)));
    }

    @Test
    void removeRoad_disconnectsBothEndpoints() {
        graph.addRoad("A", "B", 5.0);
        boolean removed = graph.removeRoad("A", "B");

        assertTrue(removed);
        assertFalse(graph.hasRoad("A", "B"));
        assertTrue(graph.getNeighbors("A").isEmpty());
        assertTrue(graph.getNeighbors("B").isEmpty());
    }

    @Test
    void removeRoad_nonExistentRoad_returnsFalse() {
        assertFalse(graph.removeRoad("A", "B"));
    }

    @Test
    void removeLocation_alsoRemovesItsRoads() {
        graph.addRoad("A", "B", 5.0);
        graph.addRoad("B", "C", 3.0);

        boolean removed = graph.removeLocation("B");

        assertTrue(removed);
        assertFalse(graph.hasLocation("B"));
        // Roads that referenced B must be gone from A's and C's adjacency lists too.
        assertTrue(graph.getNeighbors("A").isEmpty());
        assertTrue(graph.getNeighbors("C").isEmpty());
        assertEquals(2, graph.locationCount());
    }

    @Test
    void removeLocation_nonExistent_returnsFalse() {
        assertFalse(graph.removeLocation("Z"));
    }

    @Test
    void getAllEdges_deduplicatesUndirectedRoad() {
        graph.addRoad("A", "B", 5.0);
        graph.addRoad("B", "C", 3.0);

        // Even though each road is referenced from two adjacency lists,
        // getAllEdges() must report each physical road exactly once.
        assertEquals(2, graph.getAllEdges().size());
        assertEquals(2, graph.roadCount());
    }

    @Test
    void getNeighbors_unknownLocation_throws() {
        assertThrows(IllegalArgumentException.class, () -> graph.getNeighbors("Z"));
    }

    @Test
    void isEmptyAndClear_behaveCorrectly() {
        assertFalse(graph.isEmpty());
        graph.clear();
        assertTrue(graph.isEmpty());
        assertEquals(0, graph.locationCount());
        assertEquals(0, graph.roadCount());
    }

    @Test
    void edgeGetNeighbor_resolvesTheOtherEndpoint() {
        Edge edge = new Edge("A", "B", 5.0);
        assertEquals("B", edge.getNeighbor("A"));
        assertEquals("A", edge.getNeighbor("B"));
        assertThrows(IllegalArgumentException.class, () -> edge.getNeighbor("Z"));
    }

    /**
     * {@code getAllEdges()} caches its result for performance (see the
     * Phase 9 optimization notes in {@code docs/OPTIMIZATION.md}). This
     * test proves that cache is correctly invalidated on every mutating
     * operation, so callers (like {@code MapPanel}, repainted on every
     * zoom/resize) never see stale edge data.
     */
    @Test
    void getAllEdges_neverReturnsStaleDataAfterMutations() {
        assertTrue(graph.getAllEdges().isEmpty());

        graph.addRoad("A", "B", 5.0);
        assertEquals(1, graph.getAllEdges().size()); // cache built here

        graph.addRoad("B", "C", 3.0);
        assertEquals(2, graph.getAllEdges().size()); // must reflect the new road, not the stale cache

        graph.removeRoad("A", "B");
        assertEquals(1, graph.getAllEdges().size()); // must reflect the removal

        graph.addLocation(new Location("D", "Delta"));
        graph.addRoad("C", "D", 2.0);
        graph.removeLocation("C"); // cascades: removes both B-C and C-D roads
        assertTrue(graph.getAllEdges().isEmpty());
    }
}
