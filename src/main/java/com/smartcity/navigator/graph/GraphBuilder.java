package com.smartcity.navigator.graph;

import com.smartcity.navigator.model.Location;

/**
 * Fluent builder for constructing a {@link CityGraph} step by step.
 * <p>
 * {@code CityGraph} itself exposes a straightforward mutation API
 * ({@code addLocation}, {@code addRoad}, ...) intended for runtime use
 * by the UI and service layers. {@code GraphBuilder} wraps that same
 * API in a chainable form that reads well when constructing graphs in
 * bulk — for example when assembling the bundled default city, or when
 * writing concise unit tests.
 * <p>
 * Example:
 * <pre>{@code
 * CityGraph city = new GraphBuilder()
 *         .withLocation("L1", "Home", 50, 50)
 *         .withLocation("L2", "Market", 150, 60)
 *         .withRoad("L1", "L2", 5)
 *         .build();
 * }</pre>
 *
 * @author Smart City Route Navigator Team
 */
public class GraphBuilder {

    private final CityGraph graph = new CityGraph();

    /**
     * Adds a location with explicit map coordinates.
     *
     * @return this builder, for chaining
     */
    public GraphBuilder withLocation(String id, String name, double x, double y) {
        graph.addLocation(new Location(id, name, x, y));
        return this;
    }

    /**
     * Adds a location without explicit coordinates (defaults to origin).
     *
     * @return this builder, for chaining
     */
    public GraphBuilder withLocation(String id, String name) {
        graph.addLocation(new Location(id, name));
        return this;
    }

    /**
     * Adds a weighted, undirected road between two already-added locations.
     *
     * @return this builder, for chaining
     */
    public GraphBuilder withRoad(String sourceId, String destinationId, double weight) {
        graph.addRoad(sourceId, destinationId, weight);
        return this;
    }

    /**
     * Finalizes and returns the constructed graph.
     *
     * @return the fully assembled {@link CityGraph}
     */
    public CityGraph build() {
        return graph;
    }
}
