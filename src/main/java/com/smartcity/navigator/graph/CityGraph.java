package com.smartcity.navigator.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.smartcity.navigator.model.Edge;
import com.smartcity.navigator.model.Location;

/**
 * Represents the city's road network as an undirected, weighted graph
 * using an adjacency list.
 * <p>
 * Locations are graph vertices, keyed by their unique id. Roads are
 * weighted, undirected edges: adding a road between {@code A} and
 * {@code B} makes {@code B} reachable from {@code A} and vice versa,
 * both referencing the same {@link Edge} instance.
 * <p>
 * {@code CityGraph} only manages graph structure (locations and roads).
 * It has no knowledge of pathfinding — that responsibility belongs to
 * {@code DijkstraAlgorithm} in the {@code algorithm} package, which
 * queries this class via {@link #getNeighbors(String)}.
 * <p>
 * This class is not thread-safe. It is designed for single-threaded use
 * from the Swing Event Dispatch Thread via {@code RouteService}.
 *
 * @author Smart City Route Navigator Team
 */
public class CityGraph {

    /** Locations keyed by id, in insertion order (drives UI dropdown ordering). */
    private final Map<String, Location> locations = new LinkedHashMap<>();

    /** Adjacency list: location id -> list of edges incident to that location. */
    private final Map<String, List<Edge>> adjacencyList = new LinkedHashMap<>();

    /**
     * Cached, deduplicated view of every edge in the graph, lazily built
     * by {@link #getAllEdges()} and invalidated ({@code null}'d out) by
     * any operation that changes the road set. Without this cache,
     * {@code getAllEdges()} rebuilds an O(E) set on every call — and it
     * is called on every {@code MapPanel} repaint and every
     * {@code GraphLoader.saveToFile}, so for a UI that repaints on every
     * zoom/resize/highlight change, this turns a repeated O(E) rebuild
     * into an O(1) cache hit between actual graph edits.
     */
    private Set<Edge> cachedEdges = null;

    /**
     * Adds a new location (vertex) to the graph. If a location with the
     * same id already exists, it is replaced and its existing roads are
     * preserved (only the location metadata is updated).
     *
     * @param location the location to add, must not be null
     */
    public void addLocation(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Location must not be null");
        }
        synchronized (this) {
            locations.put(location.getId(), location);
            adjacencyList.putIfAbsent(location.getId(), new ArrayList<>());
            cachedEdges = null;
        }
    }

    /**
     * Removes a location and every road connected to it (dynamic deletion).
     *
     * @param locationId id of the location to remove
     * @return {@code true} if a location was removed, {@code false} if no such location existed
     */
    public boolean removeLocation(String locationId) {
        synchronized (this) {
            if (!locations.containsKey(locationId)) {
                return false;
            }
            // Remove this location's own adjacency entry.
            List<Edge> ownEdges = adjacencyList.remove(locationId);

            // Remove any edge pointing at this location from every other location's list.
            if (ownEdges != null) {
                for (Edge edge : ownEdges) {
                    String neighborId = edge.getNeighbor(locationId);
                    List<Edge> neighborEdges = adjacencyList.get(neighborId);
                    if (neighborEdges != null) {
                        neighborEdges.removeIf(e -> e.connects(locationId, neighborId));
                    }
                }
            }

            locations.remove(locationId);
            cachedEdges = null; // this location's edges (if any) are gone; rebuild lazily next time
            return true;
        }
    }

    /**
     * Adds a weighted, undirected road between two existing locations.
     *
     * @param sourceId      id of one endpoint
     * @param destinationId id of the other endpoint
     * @param weight        distance in kilometers, must be positive
     * @throws IllegalArgumentException if either location doesn't exist, the road already
     *                                   exists, or the endpoints are the same location
     */
    public void addRoad(String sourceId, String destinationId, double weight) {
        requireLocation(sourceId);
        requireLocation(destinationId);
        if (hasRoad(sourceId, destinationId)) {
            throw new IllegalArgumentException(
                    "A road already exists between '" + sourceId + "' and '" + destinationId + "'");
        }
        // A single Edge instance is shared by both endpoints' adjacency lists,
        // since the road is undirected and represents one physical connection.
        synchronized (this) {
            Edge edge = new Edge(sourceId, destinationId, weight);
            adjacencyList.get(sourceId).add(edge);
            adjacencyList.get(destinationId).add(edge);
            cachedEdges = null; // a new edge exists; rebuild lazily next time
        }
    }

    /**
     * Removes the road (in either direction) between two locations (dynamic deletion).
     *
     * @param sourceId      id of one endpoint
     * @param destinationId id of the other endpoint
     * @return {@code true} if a road was removed, {@code false} if no such road existed
     */
    public boolean removeRoad(String sourceId, String destinationId) {
        synchronized (this) {
            List<Edge> sourceEdges = adjacencyList.get(sourceId);
            List<Edge> destEdges = adjacencyList.get(destinationId);
            if (sourceEdges == null || destEdges == null) {
                return false;
            }
            boolean removed = sourceEdges.removeIf(e -> e.connects(sourceId, destinationId));
            destEdges.removeIf(e -> e.connects(sourceId, destinationId));
            if (removed) {
                cachedEdges = null; // an edge is gone; rebuild lazily next time
            }
            return removed;
        }
    }

    /**
     * Checks whether a direct road exists between two locations.
     */
    public boolean hasRoad(String sourceId, String destinationId) {
        synchronized (this) {
            List<Edge> edges = adjacencyList.get(sourceId);
            if (edges == null) {
                return false;
            }
            return edges.stream().anyMatch(e -> e.connects(sourceId, destinationId));
        }
    }

    /**
     * Checks whether a location with the given id exists in the graph.
     */
    public boolean hasLocation(String locationId) {
        synchronized (this) {
            return locations.containsKey(locationId);
        }
    }

    /**
     * Looks up a location by id.
     *
     * @param locationId id to look up
     * @return an {@link Optional} containing the location if found, empty otherwise
     */
    public Optional<Location> getLocation(String locationId) {
        synchronized (this) {
            return Optional.ofNullable(locations.get(locationId));
        }
    }

    /**
     * Returns all locations currently in the graph, in the order they were added.
     * The returned collection is unmodifiable; use {@link #addLocation} /
     * {@link #removeLocation} to mutate the graph.
     */
    public Collection<Location> getAllLocations() {
        synchronized (this) {
            return Collections.unmodifiableCollection(new ArrayList<>(locations.values()));
        }
    }

    /**
     * Returns every road (edge) in the graph exactly once, even though
     * each undirected edge is internally referenced from two adjacency
     * lists. The result is cached after the first call and reused
     * until the next structural mutation ({@link #addRoad},
     * {@link #removeRoad}, {@link #removeLocation}, or {@link #clear}),
     * turning what was an O(E) rebuild on every call into an O(1) cache
     * hit for the common case of repeated reads (e.g. every UI repaint)
     * between infrequent graph edits.
     */
    public Set<Edge> getAllEdges() {
        synchronized (this) {
            if (cachedEdges == null) {
                Set<Edge> edges = new LinkedHashSet<>();
                for (List<Edge> edgeList : adjacencyList.values()) {
                    edges.addAll(edgeList);
                }
                cachedEdges = Collections.unmodifiableSet(edges);
            }
            return cachedEdges;
        }
    }

    /**
     * Returns the roads directly connected to a location, used by
     * {@code DijkstraAlgorithm} to relax neighboring distances.
     *
     * @param locationId id of the location whose neighbors are requested
     * @return unmodifiable list of edges incident to the location
     * @throws IllegalArgumentException if the location doesn't exist
     */
    public List<Edge> getNeighbors(String locationId) {
        synchronized (this) {
            requireLocation(locationId);
            return Collections.unmodifiableList(new ArrayList<>(adjacencyList.get(locationId)));
        }
    }

    /**
     * @return the number of locations currently in the graph
     */
    public int locationCount() {
        synchronized (this) {
            return locations.size();
        }
    }

    /**
     * @return the number of distinct roads currently in the graph
     */
    public int roadCount() {
        return getAllEdges().size();
    }

    /**
     * @return {@code true} if the graph has no locations
     */
    public boolean isEmpty() {
        synchronized (this) {
            return locations.isEmpty();
        }
    }

    /**
     * Removes every location and road, resetting the graph to empty.
    * Useful when an existing graph instance must be emptied in place.
     */
    public void clear() {
        synchronized (this) {
            locations.clear();
            adjacencyList.clear();
            cachedEdges = null;
        }
    }

    private void requireLocation(String locationId) {
        if (!locations.containsKey(locationId)) {
            throw new IllegalArgumentException("Unknown location id: '" + locationId + "'");
        }
    }
}
