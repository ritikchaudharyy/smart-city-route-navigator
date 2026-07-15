package com.smartcity.navigator.model;

import java.util.Objects;

/**
 * Represents a single location (vertex) in the city road network.
 * <p>
 * Each {@code Location} has a unique identifier, a human-readable name,
 * and optional map coordinates ({@code x}, {@code y}) used by the UI's
 * {@code MapPanel} to visually plot the location. Coordinates default to
 * {@code 0.0} and are safe to ignore for non-visual use cases such as
 * console output or unit testing.
 * <p>
 * Two locations are considered equal if they share the same {@code id},
 * since the id is the unique key used throughout the graph, algorithm,
 * and service layers.
 *
 * @author Smart City Route Navigator Team
 */
public final class Location {

    private final String id;
    private String name;
    private double x;
    private double y;

    /**
     * Creates a location with map coordinates.
     *
     * @param id   unique identifier for this location (e.g. "L1"), must not be null or blank
     * @param name human-readable display name (e.g. "Home"), must not be null or blank
     * @param x    horizontal coordinate for map rendering
     * @param y    vertical coordinate for map rendering
     */
    public Location(String id, String name, double x, double y) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Location id must not be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Location name must not be null or blank");
        }
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
    }

    /**
     * Creates a location without explicit map coordinates (defaults to origin).
     * Convenient for tests and data that doesn't need visual placement yet.
     *
     * @param id   unique identifier for this location
     * @param name human-readable display name
     */
    public Location(String id, String name) {
        this(id, name, 0.0, 0.0);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Location name must not be null or blank");
        }
        this.name = name;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    /**
     * Locations are considered equal when their ids match, since the id
     * is the authoritative key used by {@code CityGraph}'s adjacency list.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Location)) {
            return false;
        }
        Location that = (Location) other;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}
