package com.smartcity.navigator.algorithm;

import com.smartcity.navigator.graph.CityGraph;
import com.smartcity.navigator.graph.GraphBuilder;
import com.smartcity.navigator.model.PathResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies {@link DijkstraAlgorithm} stays correct (and reasonably fast)
 * on a graph far larger than the bundled 8-location sample city — a
 * synthetic N x N grid, connected by unit-weight roads between
 * horizontal and vertical neighbors.
 * <p>
 * A grid graph is a convenient stress-test shape because its shortest
 * path length between opposite corners is independently known without
 * running Dijkstra at all: it is simply the Manhattan distance,
 * {@code 2 * (N - 1)}, since every step costs exactly 1 and no diagonal
 * shortcuts exist. This lets the test assert against a ground truth
 * that doesn't depend on the algorithm under test.
 *
 * @author Smart City Route Navigator Team
 */
class DijkstraAlgorithmScaleTest {

    private static final int GRID_SIZE = 25; // 625 locations, 1200 roads

    @Test
    void findShortestPath_onLargeGrid_matchesKnownManhattanDistance() {
        CityGraph grid = buildGrid(GRID_SIZE);
        assertEquals(GRID_SIZE * GRID_SIZE, grid.locationCount());

        String topLeft = idOf(0, 0);
        String bottomRight = idOf(GRID_SIZE - 1, GRID_SIZE - 1);

        long start = System.nanoTime();
        PathResult result = new DijkstraAlgorithm(grid).findShortestPath(topLeft, bottomRight);
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;

        assertTrue(result.isPathFound());
        assertEquals(2.0 * (GRID_SIZE - 1), result.getTotalDistance(), 0.0001);
        assertEquals(2 * (GRID_SIZE - 1) + 1, result.getRoute().size()); // hops + 1 locations

        // Generous smoke-test bound (not a strict benchmark): O((V+E) log V)
        // on ~625 locations / ~1200 roads should complete near-instantly on
        // any reasonable machine. This just guards against an accidental
        // infinite loop or quadratic regression, not micro-performance.
        assertTrue(elapsedMillis < 2000,
                "Expected near-instant completion on a 625-location grid, took " + elapsedMillis + "ms");
    }

    /**
     * Builds an N x N grid graph: location ids {@code "r{row}c{col}"},
     * with a unit-weight road to the neighbor directly right and
     * directly below each cell (which, combined across all cells,
     * connects every adjacent pair exactly once).
     */
    private CityGraph buildGrid(int size) {
        GraphBuilder builder = new GraphBuilder();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                builder.withLocation(idOf(row, col), "R" + row + "C" + col, col * 20, row * 20);
            }
        }
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (col + 1 < size) {
                    builder.withRoad(idOf(row, col), idOf(row, col + 1), 1.0);
                }
                if (row + 1 < size) {
                    builder.withRoad(idOf(row, col), idOf(row + 1, col), 1.0);
                }
            }
        }
        return builder.build();
    }

    private String idOf(int row, int col) {
        return "r" + row + "c" + col;
    }
}
