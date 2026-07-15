package com.smartcity.navigator.model;

import java.util.Objects;

/**
 * Represents a weighted road connecting two locations in the city graph.
 * <p>
 * The road network is undirected, so a single {@code Edge} instance
 * conceptually represents travel in both directions between
 * {@link #sourceId} and {@link #destinationId}. The {@code weight}
 * represents the distance (in kilometers) between the two endpoints.
 * <p>
 * {@code Edge} objects are immutable; to change a road's distance,
 * remove the old edge and add a new one via {@code CityGraph}.
 *
 * @author Smart City Route Navigator Team
 */
public final class Edge {

    private final String sourceId;
    private final String destinationId;
    private final double weight;

    /**
     * Creates a weighted road between two locations.
     *
     * @param sourceId      id of one endpoint location
     * @param destinationId id of the other endpoint location
     * @param weight        distance in kilometers, must be positive
     */
    public Edge(String sourceId, String destinationId, double weight) {
        if (sourceId == null || sourceId.isBlank()) {
            throw new IllegalArgumentException("Edge sourceId must not be null or blank");
        }
        if (destinationId == null || destinationId.isBlank()) {
            throw new IllegalArgumentException("Edge destinationId must not be null or blank");
        }
        if (sourceId.equals(destinationId)) {
            throw new IllegalArgumentException("Edge cannot connect a location to itself");
        }
        if (weight <= 0) {
            throw new IllegalArgumentException("Edge weight must be positive, got: " + weight);
        }
        this.sourceId = sourceId;
        this.destinationId = destinationId;
        this.weight = weight;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public double getWeight() {
        return weight;
    }

    /**
     * Given one endpoint of this edge, returns the id of the other endpoint.
     * Useful when traversing an undirected adjacency list where the edge
     * is stored under both of its endpoints.
     *
     * @param fromId id of the endpoint the traversal is currently at
     * @return id of the neighboring location on the other side of this edge
     * @throws IllegalArgumentException if {@code fromId} is not an endpoint of this edge
     */
    public String getNeighbor(String fromId) {
        if (fromId.equals(sourceId)) {
            return destinationId;
        }
        if (fromId.equals(destinationId)) {
            return sourceId;
        }
        throw new IllegalArgumentException("Location '" + fromId + "' is not an endpoint of this edge");
    }

    /**
     * Checks whether this edge connects the two given location ids,
     * regardless of order (since the graph is undirected).
     */
    public boolean connects(String locationIdA, String locationIdB) {
        return (sourceId.equals(locationIdA) && destinationId.equals(locationIdB))
                || (sourceId.equals(locationIdB) && destinationId.equals(locationIdA));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Edge)) {
            return false;
        }
        Edge that = (Edge) other;
        return Double.compare(weight, that.weight) == 0 && connects(that.sourceId, that.destinationId);
    }

    @Override
    public int hashCode() {
        // Order-independent hash so that Edge(A,B) and Edge(B,A) hash the same,
        // consistent with the order-independent equals() above.
        return Objects.hash(sourceId.hashCode() + destinationId.hashCode(), weight);
    }

    @Override
    public String toString() {
        return sourceId + " <-> " + destinationId + " [" + weight + " km]";
    }
}
