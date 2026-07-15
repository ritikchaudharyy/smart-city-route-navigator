package com.smartcity.navigator.utils;

import java.util.Optional;

import com.smartcity.navigator.graph.CityGraph;

/**
 * Centralized input validation, returning a human-readable error message
 * ({@link Optional#empty()} means "valid") rather than throwing, since
 * failed validation here represents an expected user-input situation,
 * not an exceptional program state.
 * <p>
 * Keeping validation here — instead of duplicating checks inside
 * {@code RoutePanel} or {@code MainFrame} — is what lets the UI layer
 * stay free of business logic: the UI only needs to display whatever
 * message a validator (via {@code RouteService}) returns.
 *
 * @author Smart City Route Navigator Team
 */
public final class Validators {

    private Validators() {
        // Utility class: not instantiable.
    }

    /**
     * @return {@code true} if {@code value} is null, empty, or only whitespace
     */
    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Validates a source/destination pair before running the shortest-path
     * search, covering every rule the spec calls out: no empty source, no
     * empty destination, no identical source and destination, and both
     * locations must actually exist in the current graph.
     *
     * @param sourceId      selected source location id (may be null/blank)
     * @param destinationId selected destination location id (may be null/blank)
     * @param graph         the current city graph
     * @return an error message if invalid, or {@link Optional#empty()} if valid
     */
    public static Optional<String> validateRouteSelection(String sourceId, String destinationId, CityGraph graph) {
        if (isBlank(sourceId)) {
            return Optional.of("Please select a source location.");
        }
        if (isBlank(destinationId)) {
            return Optional.of("Please select a destination location.");
        }
        if (sourceId.equals(destinationId)) {
            return Optional.of("Source and destination must be different locations.");
        }
        if (graph == null || graph.isEmpty()) {
            return Optional.of("The city graph is empty. Please load or create a city first.");
        }
        if (!graph.hasLocation(sourceId)) {
            return Optional.of("Source location no longer exists in the graph.");
        }
        if (!graph.hasLocation(destinationId)) {
            return Optional.of("Destination location no longer exists in the graph.");
        }
        return Optional.empty();
    }

    /**
     * Validates a new location before it is added to the graph.
     *
     * @return an error message if invalid, or {@link Optional#empty()} if valid
     */
    public static Optional<String> validateNewLocation(String id, String name, CityGraph graph) {
        String normalizedId = Helpers.nullSafeTrim(id);

        if (isBlank(id)) {
            return Optional.of("Location id must not be empty.");
        }
        if (isBlank(name)) {
            return Optional.of("Location name must not be empty.");
        }
        if (graph.hasLocation(normalizedId)) {
            return Optional.of("A location with id '" + normalizedId + "' already exists.");
        }
        return Optional.empty();
    }

    /**
     * Validates a new road before it is added to the graph.
     *
     * @return an error message if invalid, or {@link Optional#empty()} if valid
     */
    public static Optional<String> validateNewRoad(String sourceId, String destinationId, double weight, CityGraph graph) {
        String normalizedSourceId = Helpers.nullSafeTrim(sourceId);
        String normalizedDestinationId = Helpers.nullSafeTrim(destinationId);

        if (isBlank(sourceId) || isBlank(destinationId)) {
            return Optional.of("Both a source and destination location must be selected.");
        }
        if (normalizedSourceId.equals(normalizedDestinationId)) {
            return Optional.of("A road cannot connect a location to itself.");
        }
        if (!graph.hasLocation(normalizedSourceId) || !graph.hasLocation(normalizedDestinationId)) {
            return Optional.of("Both locations must exist before a road can connect them.");
        }
        if (weight <= 0) {
            return Optional.of("Road distance must be a positive number.");
        }
        if (graph.hasRoad(normalizedSourceId, normalizedDestinationId)) {
            return Optional.of("A road already exists between these two locations.");
        }
        return Optional.empty();
    }
}
