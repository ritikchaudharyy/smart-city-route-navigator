package com.smartcity.navigator.model;

/**
 * A lightweight, immutable helper record used exclusively by
 * {@code DijkstraAlgorithm}'s {@link java.util.PriorityQueue}.
 * <p>
 * Unlike {@link Location}, which is a full domain entity, {@code Node}
 * only carries what the algorithm needs while relaxing edges: the id of
 * the location and the current best-known tentative distance to reach it
 * from the source. Keeping this separate from {@code Location} avoids
 * mutating domain objects during pathfinding and keeps the priority
 * queue's comparisons cheap.
 * <p>
 * {@code Node} is ordered naturally by {@link #distance} (ascending),
 * which is exactly the ordering Dijkstra's algorithm needs to always
 * expand the closest unvisited location next.
 *
 * @author Smart City Route Navigator Team
 */
public final class Node implements Comparable<Node> {

    private final String locationId;
    private final double distance;

    /**
     * Creates a priority queue entry for Dijkstra's algorithm.
     *
     * @param locationId id of the location this entry refers to
     * @param distance   tentative shortest distance from the source to this location
     */
    public Node(String locationId, double distance) {
        if (locationId == null || locationId.isBlank()) {
            throw new IllegalArgumentException("Node locationId must not be null or blank");
        }
        this.locationId = locationId;
        this.distance = distance;
    }

    public String getLocationId() {
        return locationId;
    }

    public double getDistance() {
        return distance;
    }

    /**
     * Orders nodes by ascending distance so the {@link java.util.PriorityQueue}
     * always pops the location with the smallest tentative distance next —
     * the core greedy step of Dijkstra's algorithm.
     */
    @Override
    public int compareTo(Node other) {
        return Double.compare(this.distance, other.distance);
    }

    @Override
    public String toString() {
        return locationId + " (dist=" + distance + ")";
    }
}
