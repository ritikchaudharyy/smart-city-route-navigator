package com.smartcity.navigator.model;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Encapsulates the outcome of running {@code DijkstraAlgorithm} between
 * a source and destination location.
 * <p>
 * A {@code PathResult} is either:
 * <ul>
 *   <li><b>successful</b> — holding an ordered {@link #route} of
 *       locations from source to destination and the {@link #totalDistance}
 *       between them, or</li>
 *   <li><b>unsuccessful</b> — holding no route (empty list), a distance
 *       of {@link Double#POSITIVE_INFINITY}, and a human-readable
 *       {@link #message} explaining why (e.g. the locations are
 *       disconnected).</li>
 * </ul>
 * Use the {@link #success(List, double)} and {@link #failure(String)}
 * factory methods rather than the constructor directly — they make the
 * intent obvious at every call site in {@code RouteService} and the UI.
 *
 * @author Smart City Route Navigator Team
 */
public final class PathResult {

    private final List<Location> route;
    private final double totalDistance;
    private final boolean pathFound;
    private final String message;

    private PathResult(List<Location> route, double totalDistance, boolean pathFound, String message) {
        this.route = Collections.unmodifiableList(route);
        this.totalDistance = totalDistance;
        this.pathFound = pathFound;
        this.message = message;
    }

    /**
     * Builds a successful result for a path that was found.
     *
     * @param route         ordered list of locations from source to destination (inclusive)
     * @param totalDistance sum of edge weights along the route, in kilometers
     * @return a {@code PathResult} representing success
     */
    public static PathResult success(List<Location> route, double totalDistance) {
        if (route == null || route.isEmpty()) {
            throw new IllegalArgumentException("A successful PathResult must contain a non-empty route");
        }
        String summary = "Shortest route found with " + (route.size() - 1) + " road(s).";
        return new PathResult(route, totalDistance, true, summary);
    }

    /**
     * Builds a failed result (e.g. no path exists between the two locations,
     * or invalid input was supplied).
     *
     * @param message human-readable explanation shown to the user
     * @return a {@code PathResult} representing failure
     */
    public static PathResult failure(String message) {
        return new PathResult(Collections.emptyList(), Double.POSITIVE_INFINITY, false, message);
    }

    public List<Location> getRoute() {
        return route;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public boolean isPathFound() {
        return pathFound;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Renders the route as an arrow-separated, human-readable string,
     * e.g. {@code "Home -> Market -> Hospital -> Mall"}.
     * Returns an empty string when no path was found.
     */
    public String getFormattedRoute() {
        if (!pathFound) {
            return "";
        }
        return route.stream()
                .map(Location::getName)
                .collect(Collectors.joining(" -> "));
    }

    @Override
    public String toString() {
        return pathFound
                ? getFormattedRoute() + " | Total Distance: " + totalDistance + " km"
                : "No route found: " + message;
    }
}
