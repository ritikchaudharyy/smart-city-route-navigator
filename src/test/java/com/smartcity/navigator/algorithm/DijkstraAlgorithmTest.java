package com.smartcity.navigator.algorithm;

import com.smartcity.navigator.graph.CityGraph;
import com.smartcity.navigator.graph.GraphBuilder;
import com.smartcity.navigator.graph.GraphLoadException;
import com.smartcity.navigator.graph.GraphLoader;
import com.smartcity.navigator.model.Location;
import com.smartcity.navigator.model.PathResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link DijkstraAlgorithm}: correctness of the shortest
 * path and distance on graphs with multiple candidate routes, guard
 * clauses for invalid input, disconnected-graph handling, and a
 * regression test against the project's bundled default city data.
 *
 * @author Smart City Route Navigator Team
 */
class DijkstraAlgorithmTest {

    @Test
    void findShortestPath_choosesShorterOfTwoRoutes() {
        // A -> B -> D costs 3 + 3 = 6; A -> C -> D costs 1 + 1 = 2.
        // The algorithm must pick the second, cheaper route even though
        // it isn't the first one explored.
        CityGraph graph = new GraphBuilder()
                .withLocation("A", "Start")
                .withLocation("B", "Long Way")
                .withLocation("C", "Short Way")
                .withLocation("D", "End")
                .withRoad("A", "B", 3)
                .withRoad("B", "D", 3)
                .withRoad("A", "C", 1)
                .withRoad("C", "D", 1)
                .build();

        PathResult result = new DijkstraAlgorithm(graph).findShortestPath("A", "D");

        assertTrue(result.isPathFound());
        assertEquals(2.0, result.getTotalDistance(), 0.0001);
        assertEquals(List.of("A", "C", "D"), idsOf(result));
    }

    @Test
    void findShortestPath_sameSourceAndDestination_returnsZeroDistancePath() {
        CityGraph graph = new GraphBuilder()
                .withLocation("A", "Start")
                .build();

        PathResult result = new DijkstraAlgorithm(graph).findShortestPath("A", "A");

        assertTrue(result.isPathFound());
        assertEquals(0.0, result.getTotalDistance(), 0.0001);
        assertEquals(List.of("A"), idsOf(result));
    }

    @Test
    void findShortestPath_disconnectedLocations_returnsFailure() {
        CityGraph graph = new GraphBuilder()
                .withLocation("A", "Island One")
                .withLocation("B", "Island Two")
                .build(); // no road connecting them

        PathResult result = new DijkstraAlgorithm(graph).findShortestPath("A", "B");

        assertFalse(result.isPathFound());
        assertTrue(result.getMessage().toLowerCase().contains("disconnected")
                || result.getMessage().toLowerCase().contains("no route"));
    }

    @Test
    void findShortestPath_unknownSource_returnsFailure() {
        CityGraph graph = new GraphBuilder().withLocation("A", "Start").build();
        PathResult result = new DijkstraAlgorithm(graph).findShortestPath("Z", "A");
        assertFalse(result.isPathFound());
    }

    @Test
    void findShortestPath_unknownDestination_returnsFailure() {
        CityGraph graph = new GraphBuilder().withLocation("A", "Start").build();
        PathResult result = new DijkstraAlgorithm(graph).findShortestPath("A", "Z");
        assertFalse(result.isPathFound());
    }

    @Test
    void constructor_rejectsNullGraph() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> new DijkstraAlgorithm(null));
    }

    @Test
    void findShortestPath_ignoresLongerAlternateRoute() {
        // A direct A-D road (10) should still lose to a cheaper multi-hop route (1+1+1=3).
        CityGraph graph = new GraphBuilder()
                .withLocation("A", "A").withLocation("B", "B")
                .withLocation("C", "C").withLocation("D", "D")
                .withRoad("A", "D", 10)
                .withRoad("A", "B", 1)
                .withRoad("B", "C", 1)
                .withRoad("C", "D", 1)
                .build();

        PathResult result = new DijkstraAlgorithm(graph).findShortestPath("A", "D");

        assertEquals(3.0, result.getTotalDistance(), 0.0001);
        assertEquals(List.of("A", "B", "C", "D"), idsOf(result));
    }

    /**
     * Regression test against the actual bundled dataset shipped in
     * {@code resources/data/default-city.dat}: guards against the sample
     * data ever being edited in a way that breaks the spec's own example
     * (Home -&gt; Market -&gt; Hospital -&gt; Mall, 14 km).
     */
    @Test
    void findShortestPath_defaultCity_matchesSpecExample() throws GraphLoadException {
        CityGraph defaultCity = GraphLoader.loadDefaultCity();
        PathResult result = new DijkstraAlgorithm(defaultCity).findShortestPath("L1", "L4");

        assertTrue(result.isPathFound());
        assertEquals(14.0, result.getTotalDistance(), 0.0001);
        assertEquals(List.of("Home", "Market", "Hospital", "Mall"), namesOf(result));
    }

    private List<String> idsOf(PathResult result) {
        return result.getRoute().stream().map(Location::getId).collect(Collectors.toList());
    }

    private List<String> namesOf(PathResult result) {
        return result.getRoute().stream().map(Location::getName).collect(Collectors.toList());
    }
}
