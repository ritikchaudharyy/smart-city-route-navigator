package com.smartcity.navigator.graph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;

import com.smartcity.navigator.model.Edge;
import com.smartcity.navigator.model.Location;

/**
 * Loads and saves {@link CityGraph} instances using a small, dependency-free
 * text format, and provides the application's bundled default city dataset.
 * <p>
 * File format — one directive per line, blank lines and lines starting
 * with {@code #} are ignored:
 * <pre>
 * LOCATION,id,name,x,y
 * ROAD,sourceId,destinationId,weight
 * </pre>
 * Locations must be declared before any road that references them. This
 * plain-text format keeps the project dependency-free (no JSON/XML
 * library required) while remaining trivial to hand-edit or generate.
 *
 * @author Smart City Route Navigator Team
 */
public final class GraphLoader {

    /** Classpath location of the bundled default city dataset. */
    private static final String DEFAULT_CITY_RESOURCE = "/data/default-city.dat";

    private static final String LOCATION_DIRECTIVE = "LOCATION";
    private static final String ROAD_DIRECTIVE = "ROAD";
    private static final String COMMENT_PREFIX = "#";
    private static final String FIELD_SEPARATOR = ",";

    private GraphLoader() {
        // Utility class: all functionality is exposed via static methods.
    }

    /**
     * Loads the application's bundled default city, shown on first launch
     * and used by the "New City" menu action to reset to a known-good state.
     *
     * @return the default {@link CityGraph}
     * @throws GraphLoadException if the bundled resource is missing or malformed
     *                             (indicates a packaging problem, not a user error)
     */
    public static CityGraph loadDefaultCity() throws GraphLoadException {
        try (InputStream stream = GraphLoader.class.getResourceAsStream(DEFAULT_CITY_RESOURCE)) {
            if (stream == null) {
                throw new GraphLoadException("Bundled default city resource not found: " + DEFAULT_CITY_RESOURCE);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                return parse(reader);
            }
        } catch (IOException e) {
            throw new GraphLoadException("Failed to read bundled default city data", e);
        }
    }

    /**
     * Loads a {@link CityGraph} from a user-selected file (File &gt; Load Graph).
     *
     * @param file the file to read
     * @return the parsed {@link CityGraph}
     * @throws GraphLoadException if the file cannot be read or its contents are invalid
     */
    public static CityGraph loadFromFile(File file) throws GraphLoadException {
        if (file == null || !file.exists()) {
            throw new GraphLoadException("The selected graph file does not exist.");
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            return parse(reader);
        } catch (IOException e) {
            throw new GraphLoadException("Failed to read graph file: " + file.getName(), e);
        }
    }

    /**
     * Saves a {@link CityGraph} to a file in the format understood by
     * {@link #loadFromFile(File)} (File &gt; Save Graph).
     *
     * @param graph the graph to persist
     * @param file  the destination file (overwritten if it already exists)
     * @throws GraphLoadException if the file cannot be written
     */
    public static void saveToFile(CityGraph graph, File file) throws GraphLoadException {
        if (graph == null) {
            throw new GraphLoadException("There is no graph available to save.");
        }
        if (file == null) {
            throw new GraphLoadException("Choose a file location before saving the graph.");
        }
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8))) {
            writer.println("# Smart City Route Navigator - saved graph data");
            writer.println("# Locations");
            for (Location location : graph.getAllLocations()) {
                writer.printf(Locale.US, "%s,%s,%s,%s,%s%n",
                        LOCATION_DIRECTIVE, escapeField(location.getId()), escapeField(location.getName()), location.getX(), location.getY());
            }
            writer.println("# Roads");
            for (Edge edge : graph.getAllEdges()) {
                writer.printf(Locale.US, "%s,%s,%s,%s%n",
                        ROAD_DIRECTIVE, escapeField(edge.getSourceId()), escapeField(edge.getDestinationId()), edge.getWeight());
            }
            if (writer.checkError()) {
                throw new IOException("The graph file could not be written completely.");
            }
        } catch (IOException e) {
            throw new GraphLoadException("Failed to write graph file: " + file.getName(), e);
        }
    }

    /**
     * Shared parsing routine used by both {@link #loadDefaultCity()} and
     * {@link #loadFromFile(File)}.
     */
    private static CityGraph parse(BufferedReader reader) throws IOException, GraphLoadException {
        CityGraph graph = new CityGraph();
        String line;
        int lineNumber = 0;

        while ((line = reader.readLine()) != null) {
            lineNumber++;
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith(COMMENT_PREFIX)) {
                continue;
            }

            String[] fields = splitFields(trimmed);
            for (int index = 0; index < fields.length; index++) {
                fields[index] = fields[index].trim();
            }
            try {
                if (LOCATION_DIRECTIVE.equals(fields[0])) {
                    parseLocation(graph, fields);
                } else if (ROAD_DIRECTIVE.equals(fields[0])) {
                    parseRoad(graph, fields);
                } else {
                    throw new GraphLoadException("Unknown directive '" + fields[0] + "' at line " + lineNumber);
                }
            } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
                throw new GraphLoadException("Malformed data at line " + lineNumber + ": \"" + line + "\"", e);
            }
        }

        if (graph.isEmpty()) {
            throw new GraphLoadException("Graph data contained no locations");
        }
        return graph;
    }

    private static String[] splitFields(String line) {
        java.util.List<String> fields = new java.util.ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (character == '"') {
                if (quoted && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    field.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (character == FIELD_SEPARATOR.charAt(0) && !quoted) {
                fields.add(field.toString().trim());
                field.setLength(0);
            } else {
                field.append(character);
            }
        }
        if (quoted) {
            throw new IllegalArgumentException("Unclosed quoted field");
        }
        fields.add(field.toString().trim());
        return fields.toArray(String[]::new);
    }

    private static String escapeField(String value) {
        if (value.indexOf(',') < 0 && value.indexOf('"') < 0) {
            return value;
        }
        return '"' + value.replace("\"", "\"\"") + '"';
    }

    private static void parseLocation(CityGraph graph, String[] fields) {
        // LOCATION,id,name,x,y
        requireFieldCount(fields, 5, LOCATION_DIRECTIVE);
        String id = fields[1];
        String name = fields[2];
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("LOCATION id must not be empty");
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("LOCATION name must not be empty");
        }
        if (graph.hasLocation(id)) {
            throw new IllegalArgumentException("Duplicate LOCATION id: '" + id + "'");
        }
        double x = Double.parseDouble(fields[3]);
        double y = Double.parseDouble(fields[4]);
        graph.addLocation(new Location(id, name, x, y));
    }

    private static void parseRoad(CityGraph graph, String[] fields) throws GraphLoadException {
        // ROAD,sourceId,destinationId,weight
        requireFieldCount(fields, 4, ROAD_DIRECTIVE);
        String sourceId = fields[1];
        String destinationId = fields[2];
        double weight = Double.parseDouble(fields[3]);

        if (sourceId == null || sourceId.isEmpty() || destinationId == null || destinationId.isEmpty()) {
            throw new GraphLoadException("ROAD endpoints must not be empty");
        }
        if (sourceId.equals(destinationId)) {
            throw new GraphLoadException("ROAD cannot be a self-loop: " + sourceId);
        }
        if (weight <= 0) {
            throw new GraphLoadException("ROAD weight must be positive: " + weight);
        }

        if (!graph.hasLocation(sourceId) || !graph.hasLocation(destinationId)) {
            throw new GraphLoadException(
                    "Road references unknown location(s): " + sourceId + " -> " + destinationId
                            + " (locations must be declared before roads)");
        }
        graph.addRoad(sourceId, destinationId, weight);
    }

    private static void requireFieldCount(String[] fields, int expectedCount, String directive) {
        if (fields.length != expectedCount) {
            throw new IllegalArgumentException(directive + " entries must contain exactly "
                    + (expectedCount - 1) + " values");
        }
    }
}
