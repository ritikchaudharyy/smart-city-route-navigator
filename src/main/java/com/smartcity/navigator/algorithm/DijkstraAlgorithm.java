package com.smartcity.navigator.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.smartcity.navigator.graph.CityGraph;
import com.smartcity.navigator.model.Edge;
import com.smartcity.navigator.model.Location;
import com.smartcity.navigator.model.Node;
import com.smartcity.navigator.model.PathResult;

/**
 * Computes the shortest route between two locations in a {@link CityGraph}
 * using Dijkstra's Algorithm.
 * <p>
 * <b>Algorithm overview</b> — Dijkstra's algorithm greedily grows a set of
 * "finalized" locations whose shortest distance from the source is known
 * for certain. At each step it finalizes the closest not-yet-finalized
 * location, then relaxes (attempts to shorten) the distance to each of
 * its neighbors. Because every edge weight is positive (enforced by
 * {@link Edge}), once a location is finalized its distance can never be
 * improved later, which is what makes the greedy approach correct.
 * <p>
 * <b>Complexity</b> — {@code O((V + E) log V)}, where V is the number of
 * locations and E is the number of roads: each location is popped from
 * the priority queue once ({@code log V} per pop/push), and each edge
 * triggers at most one relaxation (push).
 * <p>
 * <b>Data structures</b> — distances, the visited/finalized set, and
 * parent pointers are stored in {@link Map}s keyed by location id
 * (conceptually the "Distance Array", "Visited Array", and "Parent
 * Array" required by the spec) rather than raw arrays, since location
 * ids are {@code String}s rather than contiguous integer indices. This
 * preserves the same effective O(1) average access cost via hashing.
 * <p>
 * <b>Priority queue note</b> — this implementation uses the project's
 * own {@link MinPriorityQueue}, a from-scratch binary min-heap, rather
 * than {@code java.util.PriorityQueue}, keeping the DSA fully
 * hand-built and inspectable. That heap has no "decrease-key"
 * operation, so instead of decreasing a location's priority in place,
 * this implementation pushes a brand-new {@link Node} every time a
 * shorter distance is found, and uses <i>lazy deletion</i>: when a node
 * is popped, it is skipped if that location was already finalized. This
 * is the standard, well-documented technique for implementing Dijkstra
 * with a binary heap that lacks decrease-key, and it does not affect
 * the algorithm's correctness or asymptotic complexity.
 *
 * @author Smart City Route Navigator Team
 */
public class DijkstraAlgorithm {

    private final CityGraph graph;

    /**
     * Creates a Dijkstra runner bound to a specific graph.
     *
     * @param graph the city graph to search over, must not be null
     */
    public DijkstraAlgorithm(CityGraph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph must not be null");
        }
        this.graph = graph;
    }

    /**
     * Finds the shortest route from {@code sourceId} to {@code destinationId}.
     *
     * @param sourceId      id of the starting location
     * @param destinationId id of the target location
     * @return a successful {@link PathResult} with the route and total
     *         distance if one exists, or a failed {@link PathResult}
     *         explaining why (unknown location, or no path exists)
     */
    public PathResult findShortestPath(String sourceId, String destinationId) {
        // --- Step 0: Guard clauses ------------------------------------------
        // Defensive checks so this class is safe to call directly (e.g. from
        // unit tests) even without going through the UI/service validation layer.
        if (!graph.hasLocation(sourceId)) {
            return PathResult.failure("Source location does not exist: '" + sourceId + "'");
        }
        if (!graph.hasLocation(destinationId)) {
            return PathResult.failure("Destination location does not exist: '" + destinationId + "'");
        }
        if (sourceId.equals(destinationId)) {
            // Trivial case: the route is just the single location, zero distance.
            Location only = graph.getLocation(sourceId).orElseThrow();
            return PathResult.success(new ArrayList<>(List.of(only)), 0.0);
        }

        // distances: best known distance from source to each location, starts at infinity.
        Map<String, Double> distances = new HashMap<>();
        // parents: tracks how we reached each location, used to rebuild the path afterward.
        Map<String, String> parents = new HashMap<>();
        // finalized: locations whose shortest distance is confirmed and will never change again.
        Set<String> finalized = new HashSet<>();
        // priority queue: always pops the not-yet-finalized location with the smallest known distance.
        MinPriorityQueue<Node> priorityQueue = new MinPriorityQueue<>();

        initializeDistances(sourceId, distances);
        priorityQueue.offer(new Node(sourceId, 0.0));

        while (!priorityQueue.isEmpty()) {
            Node current = priorityQueue.poll();
            String currentId = current.getLocationId();

            // Lazy deletion: this entry is a stale duplicate of a location
            // already finalized with a shorter (or equal) distance. Skip it.
            if (finalized.contains(currentId)) {
                continue;
            }
            finalized.add(currentId);

            // Early exit: once the destination itself is finalized, its
            // distance and parent chain are final — no need to keep searching.
            if (currentId.equals(destinationId)) {
                break;
            }

            relaxNeighbors(currentId, finalized, distances, parents, priorityQueue);
        }

        double shortestDistance = distances.getOrDefault(destinationId, Double.POSITIVE_INFINITY);
        if (Double.isInfinite(shortestDistance)) {
            return PathResult.failure(
                    "No route exists between '" + sourceId + "' and '" + destinationId + "'. "
                            + "The locations are disconnected in the current road network.");
        }

        List<Location> route = reconstructPath(parents, sourceId, destinationId);
        return PathResult.success(route, shortestDistance);
    }

    /**
     * Sets every location's initial distance to infinity, except the
     * source, which starts at zero (distance to itself).
     */
    private void initializeDistances(String sourceId, Map<String, Double> distances) {
        for (Location location : graph.getAllLocations()) {
            distances.put(location.getId(), Double.POSITIVE_INFINITY);
        }
        distances.put(sourceId, 0.0);
    }

    /**
     * Relaxation step: for each road out of {@code currentId}, checks
     * whether reaching that neighbor via {@code currentId} is shorter
     * than the neighbor's currently known best distance. If so, the
     * neighbor's distance and parent are updated, and a fresh queue
     * entry is pushed with the improved distance (lazy-deletion style,
     * since the old stale entry for that neighbor is simply left in
     * place and skipped later if it's popped).
     */
    private void relaxNeighbors(String currentId, Set<String> finalized,
                                 Map<String, Double> distances, Map<String, String> parents,
                                 MinPriorityQueue<Node> priorityQueue) {
        double currentDistance = distances.get(currentId);

        for (Edge edge : graph.getNeighbors(currentId)) {
            String neighborId = edge.getNeighbor(currentId);
            if (finalized.contains(neighborId)) {
                // Already finalized with an optimal (equal-or-shorter) distance; skip.
                continue;
            }

            double candidateDistance = currentDistance + edge.getWeight();
            if (candidateDistance < distances.get(neighborId)) {
                distances.put(neighborId, candidateDistance);
                parents.put(neighborId, currentId);
                priorityQueue.offer(new Node(neighborId, candidateDistance));
            }
        }
    }

    /**
     * Rebuilds the shortest route as an ordered list of locations by
     * walking backward through the {@code parents} map from the
     * destination to the source, inserting each location at the front
     * of the result so it ends up ordered source -> destination without
     * a separate reverse pass.
     */
    private List<Location> reconstructPath(Map<String, String> parents, String sourceId, String destinationId) {
        LinkedList<Location> path = new LinkedList<>();
        String currentId = destinationId;

        while (currentId != null) {
            String currentNodeId = currentId;
            Location location = graph.getLocation(currentNodeId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Location vanished during path reconstruction: " + currentNodeId));
            path.addFirst(location);

            if (currentNodeId.equals(sourceId)) {
                break;
            }
            currentId = parents.get(currentNodeId);
        }

        return path;
    }
}
