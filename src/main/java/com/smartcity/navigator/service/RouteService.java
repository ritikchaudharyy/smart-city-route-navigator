package com.smartcity.navigator.service;

import java.io.File;
import java.util.Collection;
import java.util.Optional;

import com.smartcity.navigator.algorithm.DijkstraAlgorithm;
import com.smartcity.navigator.graph.CityGraph;
import com.smartcity.navigator.graph.GraphLoadException;
import com.smartcity.navigator.graph.GraphLoader;
import com.smartcity.navigator.model.Location;
import com.smartcity.navigator.model.PathResult;
import com.smartcity.navigator.utils.Helpers;
import com.smartcity.navigator.utils.Validators;

/**
 * The single bridge between the UI layer and the graph/algorithm layers.
 * <p>
 * Per the project architecture, no business logic — validation, graph
 * mutation, or pathfinding — belongs inside the UI. Every Swing action
 * listener in the {@code ui} package should do nothing more than: read
 * what the user selected, call one method here, and display whatever
 * comes back. {@code RouteService} is responsible for:
 * <ul>
 *   <li>owning the current {@link CityGraph} instance;</li>
 *   <li>validating input via {@link Validators} before delegating to
 *       {@link DijkstraAlgorithm};</li>
 *   <li>loading, saving, and resetting the graph (backing the File menu);</li>
 *   <li>adding/removing locations and roads (backing the "Add Locations"
 *       / "Add Roads" core features), with validation applied uniformly.</li>
 * </ul>
 * This class is not thread-safe; it is intended for single-threaded use
 * from the Swing Event Dispatch Thread.
 *
 * @author Smart City Route Navigator Team
 */
public class RouteService {

    private CityGraph graph;

    /**
     * Creates a service backed by the application's bundled default city.
     *
     * @throws GraphLoadException if the bundled default city resource is missing or malformed
     */
    public RouteService() throws GraphLoadException {
        this.graph = GraphLoader.loadDefaultCity();
    }

    /**
     * Creates a service backed by an already-constructed graph — useful
     * for unit tests and for injecting a graph built via {@code GraphBuilder}.
     *
     * @param graph the graph to operate on, must not be null
     */
    public RouteService(CityGraph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph must not be null");
        }
        this.graph = graph;
    }

    /**
     * Validates the requested source/destination, then runs Dijkstra's
     * algorithm and returns the outcome. Validation failures are returned
     * as a failed {@link PathResult} rather than thrown, so the UI can
     * handle every outcome — success, no-path, and bad-input — through
     * the same single code path.
     *
     * @param sourceId      id of the selected source location
     * @param destinationId id of the selected destination location
     * @return the shortest-path result, successful or otherwise
     */
    public PathResult findRoute(String sourceId, String destinationId) {
        Optional<String> validationError = Validators.validateRouteSelection(sourceId, destinationId, graph);
        if (validationError.isPresent()) {
            return PathResult.failure(validationError.get());
        }
        DijkstraAlgorithm algorithm = new DijkstraAlgorithm(graph);
        return algorithm.findShortestPath(sourceId, destinationId);
    }

    /**
     * @return every location in the current graph, in insertion order —
     *         used to populate the source/destination dropdowns
     */
    public Collection<Location> getAllLocations() {
        return graph.getAllLocations();
    }

    /**
     * @return the graph currently backing this service (read access for
     *         the future {@code MapPanel} and status displays)
     */
    public CityGraph getGraph() {
        return graph;
    }

    /**
     * @return {@code true} if the current graph has no locations
     */
    public boolean isEmpty() {
        return graph.isEmpty();
    }

    /**
     * Replaces the current graph with a fresh copy of the bundled default
     * city. Backs the File &gt; New City menu action.
     *
     * @throws GraphLoadException if the bundled default city resource is missing or malformed
     */
    public void resetToDefaultCity() throws GraphLoadException {
        this.graph = GraphLoader.loadDefaultCity();
    }

    /**
     * Replaces the current graph with one loaded from a file. Backs the
     * File &gt; Load Graph menu action.
     *
     * @param file the file to load
     * @throws GraphLoadException if the file is missing, unreadable, or malformed
     */
    public void loadGraphFromFile(File file) throws GraphLoadException {
        this.graph = GraphLoader.loadFromFile(file);
    }

    /**
     * Persists the current graph to a file. Backs the File &gt; Save Graph
     * menu action.
     *
     * @param file destination file (overwritten if it already exists)
     * @throws GraphLoadException if the file cannot be written
     */
    public void saveGraphToFile(File file) throws GraphLoadException {
        GraphLoader.saveToFile(graph, file);
    }

    /**
     * Validates and adds a new location to the current graph.
     *
     * @throws IllegalArgumentException if validation fails (blank id/name, or duplicate id)
     */
    public void addLocation(String id, String name, double x, double y) {
        String normalizedId = Helpers.nullSafeTrim(id);
        String normalizedName = Helpers.nullSafeTrim(name);

        Optional<String> validationError = Validators.validateNewLocation(normalizedId, normalizedName, graph);
        if (validationError.isPresent()) {
            throw new IllegalArgumentException(validationError.get());
        }
        graph.addLocation(new Location(normalizedId, normalizedName, x, y));
    }

    /**
     * Removes a location (and every road connected to it) from the current graph.
     *
     * @return {@code true} if a location was removed, {@code false} if no such location existed
     */
    public boolean removeLocation(String id) {
        return graph.removeLocation(id);
    }

    /**
     * Validates and adds a new road between two existing locations.
     *
     * @throws IllegalArgumentException if validation fails (missing locations,
     *                                   self-loop, non-positive weight, or duplicate road)
     */
    public void addRoad(String sourceId, String destinationId, double weight) {
        String normalizedSourceId = Helpers.nullSafeTrim(sourceId);
        String normalizedDestinationId = Helpers.nullSafeTrim(destinationId);

        Optional<String> validationError = Validators.validateNewRoad(normalizedSourceId, normalizedDestinationId, weight, graph);
        if (validationError.isPresent()) {
            throw new IllegalArgumentException(validationError.get());
        }
        graph.addRoad(normalizedSourceId, normalizedDestinationId, weight);
    }

    /**
     * Removes the road between two locations, if one exists.
     *
     * @return {@code true} if a road was removed, {@code false} if no such road existed
     */
    public boolean removeRoad(String sourceId, String destinationId) {
        return graph.removeRoad(sourceId, destinationId);
    }
}
