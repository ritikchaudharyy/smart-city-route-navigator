package com.smartcity.navigator.service.ai;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.smartcity.navigator.model.Location;
import com.smartcity.navigator.utils.AppLogger;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Converts a natural-language route request into two existing location IDs.
 * <p>
 * Gemini is deliberately an intent parser only. Every returned ID is checked
 * against the current graph before the UI can delegate to {@code RouteService},
 * where the local Dijkstra implementation remains the sole route authority.
 * Network work runs on a daemon executor and callbacks are delivered on the
 * Swing Event Dispatch Thread.
 *
 * Resilience: the service now includes a conservative local parser for simple
 * "source to destination" phrasing. The local parser is tried first and as a
 * fallback if the remote Gemini call fails, so simple queries continue to work
 * even when the API is unconfigured or temporarily unavailable.
 */
public final class GeminiService {

    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    private static final int MAX_QUERY_LENGTH = 500;

    private final OkHttpClient httpClient;
    private final Gson gson;
    private final ExecutorService requestExecutor;
    private volatile String apiKey;

    /** Uses the {@code GEMINI_API_KEY} environment variable when available. */
    public GeminiService() {
        this(createHttpClient(), new Gson(), System.getenv("GEMINI_API_KEY"));
    }

    GeminiService(OkHttpClient httpClient, Gson gson, String apiKey) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
        this.gson = Objects.requireNonNull(gson, "gson must not be null");
        this.requestExecutor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "gemini-route-parser");
            thread.setDaemon(true);
            return thread;
        });
        setApiKey(apiKey);
    }

    private static OkHttpClient createHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(20))
                .callTimeout(Duration.ofSeconds(25))
                .build();
    }

    /**
     * Sets an in-memory API key for the current application session. A blank
     * value disables AI parsing and leaves manual route selection available.
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey == null || apiKey.isBlank() ? null : apiKey.trim();
    }

    public boolean isApiKeyConfigured() {
        return apiKey != null;
    }

    /** Callback for a validated location pair or a user-safe failure message. */
    public interface AIParserCallback {
        void onSuccess(String sourceId, String destinationId);

        void onFailure(String errorMessage);
    }

    /** Callback for a validated route or graph-edit command. */
    public interface AICommandCallback {
        void onSuccess(AICommand command);

        void onFailure(String errorMessage);
    }

    /** The supported, locally validated AI intent types. */
    public enum AICommandType {
        FIND_ROUTE, ADD_LOCATION, REMOVE_LOCATION, ADD_ROAD, REMOVE_ROAD
    }

    /**
     * Structured parameters only; this class never mutates the graph. The UI
     * passes this validated data to the existing RouteService methods.
     */
    public static final class AICommand {
        private final AICommandType type;
        private final String sourceId;
        private final String destinationId;
        private final String locationId;
        private final String locationName;
        private final String anchorId;
        private final double distanceKm;
        private final boolean oneWayRequested;

        private AICommand(AICommandType type, String sourceId, String destinationId,
                String locationId, String locationName, String anchorId,
                double distanceKm, boolean oneWayRequested) {
            this.type = type;
            this.sourceId = sourceId;
            this.destinationId = destinationId;
            this.locationId = locationId;
            this.locationName = locationName;
            this.anchorId = anchorId;
            this.distanceKm = distanceKm;
            this.oneWayRequested = oneWayRequested;
        }

        public AICommandType getType() { return type; }
        public String getSourceId() { return sourceId; }
        public String getDestinationId() { return destinationId; }
        public String getLocationId() { return locationId; }
        public String getLocationName() { return locationName; }
        public String getAnchorId() { return anchorId; }
        public double getDistanceKm() { return distanceKm; }
        public boolean isOneWayRequested() { return oneWayRequested; }
    }

    /**
     * Parses a route intent asynchronously. Successful IDs are always exact,
     * canonical IDs from {@code locations}; unmatched, ambiguous, and
     * same-location intents are rejected before the callback is invoked.
     *
     * A conservative local parser is attempted first so simple "A to B" queries
     * work even when the Gemini API is unavailable or the key is misconfigured.
     */
    public void parseRouteQueryAsync(String userQuery, Collection<Location> locations, AIParserCallback callback) {
        Objects.requireNonNull(callback, "callback must not be null");

        String normalizedQuery = userQuery == null ? "" : userQuery.trim();
        if (normalizedQuery.isEmpty()) {
            dispatchFailure(callback, "Enter a route request before asking the AI assistant.");
            return;
        }
        if (normalizedQuery.length() > MAX_QUERY_LENGTH) {
            dispatchFailure(callback, "Keep the AI route request to " + MAX_QUERY_LENGTH + " characters or fewer.");
            return;
        }

        List<Location> locationSnapshot = locations == null ? List.of() : new ArrayList<>(locations);
        if (locationSnapshot.size() < 2) {
            dispatchFailure(callback, "Load at least two locations before using AI route parsing.");
            return;
        }

        final boolean keyConfigured = isApiKeyConfigured();

        requestExecutor.execute(() -> {
            try {
                // Fast local attempt: this is intentionally conservative and will
                // only return a match when it's unambiguous.
                ParsedRoute localParsed = tryLocalParseRoute(normalizedQuery, locationSnapshot);
                ParsedRoute validatedLocal = validateParsedRoute(localParsed, locationSnapshot);
                if (validatedLocal != null) {
                    dispatchSuccess(callback, validatedLocal.sourceId, validatedLocal.destinationId);
                    return;
                }

                // If there is no API key configured, don't attempt the network call.
                if (!keyConfigured) {
                    dispatchFailure(callback,
                            "AI route parsing is not configured. Set the GEMINI_API_KEY environment variable and restart the app.");
                    return;
                }

                // Remote attempt via Gemini.
                ParsedRoute parsedRoute = executeRequest(normalizedQuery, locationSnapshot);
                ParsedRoute validatedRoute = validateParsedRoute(parsedRoute, locationSnapshot);
                if (validatedRoute == null) {
                    dispatchFailure(callback,
                            "I could not confidently match both places to the current city map. Please choose them manually.");
                    return;
                }
                dispatchSuccess(callback, validatedRoute.sourceId, validatedRoute.destinationId);
            } catch (GeminiApiException exception) {
                // Diagnostic: log the exact status and response body.
                AppLogger.warn("Gemini route parsing request failed with HTTP " + exception.statusCode
                        + " - response body: " + exception.responseBody);

                // As a last-resort fallback, try the local parser again before failing.
                ParsedRoute fallback = tryLocalParseRoute(normalizedQuery, locationSnapshot);
                ParsedRoute validatedFallback = validateParsedRoute(fallback, locationSnapshot);
                if (validatedFallback != null) {
                    dispatchSuccess(callback, validatedFallback.sourceId, validatedFallback.destinationId);
                    return;
                }

                dispatchFailure(callback, describeApiError(exception));
            } catch (SocketTimeoutException exception) {
                AppLogger.warn("Gemini route parsing request timed out.");
                dispatchFailure(callback, "The AI assistant took too long to respond. Please try again or choose locations manually.");
            } catch (IOException exception) {
                AppLogger.warn("Gemini route parsing network request failed: " + exception.getClass().getSimpleName());
                dispatchFailure(callback, "The AI assistant is unavailable. Check your internet connection and try again.");
            } catch (RuntimeException exception) {
                AppLogger.error("Unexpected Gemini route parsing failure", exception);
                dispatchFailure(callback, "The AI route request could not be processed. Please choose locations manually.");
            }
        });
    }

    /**
     * Interprets a route request or a graph-edit request. Gemini supplies only
     * structured parameters; all identifiers and distances are revalidated
     * against the local graph before the callback receives them.
     */
    public void parseCityCommandAsync(String userQuery, Collection<Location> locations, AICommandCallback callback) {
        Objects.requireNonNull(callback, "callback must not be null");

        String normalizedQuery = userQuery == null ? "" : userQuery.trim();
        if (normalizedQuery.isEmpty()) {
            dispatchFailure(callback, "Enter a route or graph-edit request for the AI assistant.");
            return;
        }
        if (normalizedQuery.length() > MAX_QUERY_LENGTH) {
            dispatchFailure(callback, "Keep the AI request to " + MAX_QUERY_LENGTH + " characters or fewer.");
            return;
        }
        if (!isApiKeyConfigured()) {
            dispatchFailure(callback,
                    "AI route parsing is not configured. Set the GEMINI_API_KEY environment variable and restart the app.");
            return;
        }

        List<Location> locationSnapshot = locations == null ? List.of() : new ArrayList<>(locations);
        if (locationSnapshot.isEmpty()) {
            dispatchFailure(callback, "Load a city graph before using AI commands.");
            return;
        }

        requestExecutor.execute(() -> {
            try {
                AICommand command = validateCityCommand(executeCommandRequest(normalizedQuery, locationSnapshot), locationSnapshot);
                if (command == null) {
                    dispatchFailure(callback,
                            "I could not confidently match that request to the current city map. Please use existing location names and include a positive distance for a new road.");
                    return;
                }
                dispatchSuccess(callback, command);
            } catch (GeminiApiException exception) {
                AppLogger.warn("Gemini city command request failed with HTTP " + exception.statusCode
                        + " - response body: " + exception.responseBody);
                dispatchFailure(callback, describeApiError(exception));
            } catch (SocketTimeoutException exception) {
                AppLogger.warn("Gemini city command request timed out.");
                dispatchFailure(callback, "The AI assistant took too long to respond. Please try again.");
            } catch (IOException exception) {
                AppLogger.warn("Gemini city command network request failed: " + exception.getClass().getSimpleName());
                dispatchFailure(callback, "The AI assistant is unavailable. Check your internet connection and try again.");
            } catch (RuntimeException exception) {
                AppLogger.error("Unexpected Gemini city command failure", exception);
                dispatchFailure(callback, "The AI request could not be processed. Please use the graph menu instead.");
            }
        });
    }

    private ParsedRoute executeRequest(String query, List<Location> locations) throws IOException {
        RequestBody body = RequestBody.create(gson.toJson(buildRequestPayload(query, locations)), JSON_MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(GEMINI_API_URL)
                .header("x-goog-api-key", apiKey)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() == null ? "" : response.body().string();
            if (!response.isSuccessful()) {
                throw new GeminiApiException(response.code(), responseBody);
            }
            return parseGeminiResponse(responseBody);
        }
    }

    /** Builds the documented GenerateContent system-instruction and JSON-output request. */
    private JsonObject buildRequestPayload(String query, List<Location> locations) {
        JsonObject payload = new JsonObject();
        payload.add("systemInstruction", createTextContent(buildSystemInstruction(locations)));

        JsonArray contents = new JsonArray();
        contents.add(createTextContent(query));
        payload.add("contents", contents);

        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("responseMimeType", "application/json");
        generationConfig.add("responseSchema", createRouteSchema());
        JsonObject thinkingConfig = new JsonObject();
        thinkingConfig.addProperty("thinkingLevel", "minimal");
        generationConfig.add("thinkingConfig", thinkingConfig);
        generationConfig.addProperty("maxOutputTokens", 256);
        payload.add("generationConfig", generationConfig);
        return payload;
    }

    private JsonObject buildCommandRequestPayload(String query, List<Location> locations) {
        JsonObject payload = new JsonObject();
        payload.add("systemInstruction", createTextContent(buildCommandSystemInstruction(locations)));

        JsonArray contents = new JsonArray();
        contents.add(createTextContent(query));
        payload.add("contents", contents);

        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("responseMimeType", "application/json");
        generationConfig.add("responseSchema", createCommandSchema());
        JsonObject thinkingConfig = new JsonObject();
        thinkingConfig.addProperty("thinkingLevel", "minimal");
        generationConfig.add("thinkingConfig", thinkingConfig);
        generationConfig.addProperty("maxOutputTokens", 256);
        payload.add("generationConfig", generationConfig);
        return payload;
    }

    private JsonObject createTextContent(String text) {
        JsonObject part = new JsonObject();
        part.addProperty("text", text);
        JsonArray parts = new JsonArray();
        parts.add(part);
        JsonObject content = new JsonObject();
        content.add("parts", parts);
        return content;
    }

    private JsonObject createRouteSchema() {
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "OBJECT");

        JsonObject properties = new JsonObject();
        properties.add("sourceId", createStringSchema("Exact ID of the route origin, or an empty string."));
        properties.add("destinationId", createStringSchema("Exact ID of the route destination, or an empty string."));
        schema.add("properties", properties);

        JsonArray required = new JsonArray();
        required.add("sourceId");
        required.add("destinationId");
        schema.add("required", required);
        return schema;
    }

    private JsonObject createCommandSchema() {
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "OBJECT");

        JsonObject properties = new JsonObject();
        properties.add("action", createStringSchema("FIND_ROUTE, ADD_LOCATION, REMOVE_LOCATION, ADD_ROAD, or REMOVE_ROAD."));
        properties.add("sourceId", createStringSchema("Exact existing source ID, otherwise empty."));
        properties.add("destinationId", createStringSchema("Exact existing destination ID, otherwise empty."));
        properties.add("locationId", createStringSchema("New or existing location ID, otherwise empty."));
        properties.add("locationName", createStringSchema("Display name for a new location, otherwise empty."));
        properties.add("anchorId", createStringSchema("Exact existing location ID to connect a new location to, otherwise empty."));
        JsonObject distanceSchema = new JsonObject();
        distanceSchema.addProperty("type", "NUMBER");
        distanceSchema.addProperty("description", "Positive road distance in kilometres, or 0 when not applicable.");
        properties.add("distanceKm", distanceSchema);
        JsonObject oneWaySchema = new JsonObject();
        oneWaySchema.addProperty("type", "BOOLEAN");
        oneWaySchema.addProperty("description", "True only when the user explicitly requests a one-way road.");
        properties.add("oneWay", oneWaySchema);
        schema.add("properties", properties);

        JsonArray required = new JsonArray();
        for (String property : List.of("action", "sourceId", "destinationId", "locationId", "locationName",
                "anchorId", "distanceKm", "oneWay")) {
            required.add(property);
        }
        schema.add("required", required);
        return schema;
    }

    private JsonObject createStringSchema(String description) {
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "STRING");
        schema.addProperty("description", description);
        return schema;
    }

    private String buildSystemInstruction(List<Location> locations) {
        StringBuilder locationContext = new StringBuilder();
        for (Location location : locations) {
            locationContext.append("- ID: ").append(location.getId())
                    .append(" | Name: ").append(location.getName()).append('\n');
        }

        return "You are an intent parser for a city route application. "
                + "You never calculate routes, invent locations, or explain your answer. "
                + "Identify one starting location and one destination from the user's request. "
                + "The user may write in English, Hindi, or a mixture of both. "
                + "Return the exact IDs from the valid location list. If either location is missing or ambiguous, "
                + "return empty strings for both sourceId and destinationId.\n\n"
                + "Valid locations:\n" + locationContext;
    }

    private String buildCommandSystemInstruction(List<Location> locations) {
        StringBuilder locationContext = new StringBuilder();
        for (Location location : locations) {
            locationContext.append("- ID: ").append(location.getId())
                    .append(" | Name: ").append(location.getName()).append('\n');
        }

        return "You are an intent parser for a city route application. Return JSON only; never explain or calculate a route. "
                + "The user may write English, Hindi, or a mixture. Recognize exactly one action: FIND_ROUTE, "
                + "ADD_LOCATION, REMOVE_LOCATION, ADD_ROAD, or REMOVE_ROAD. Existing references must use exact IDs "
                + "from the valid list. For FIND_ROUTE set sourceId and destinationId. For ADD_LOCATION, set a unique "
                + "new locationId, locationName, anchorId, and a positive distanceKm: it means add the location and a road "
                + "to the anchor. For ADD_ROAD set sourceId, destinationId, and a positive distanceKm. For REMOVE_LOCATION "
                + "set locationId. For REMOVE_ROAD set sourceId and destinationId. Set unrelated strings to empty, distanceKm "
                + "to 0, and oneWay to false. Set oneWay true only if explicitly requested. If unclear or invalid, return empty "
                + "strings and distanceKm 0. The graph supports only two-way roads; still report an explicit one-way request "
                + "with oneWay true so the application can explain that limitation.\n\nValid locations:\n" + locationContext;
    }

    /** Parses the candidate text without trusting it as a graph-valid route. */
    ParsedRoute parseGeminiResponse(String rawJson) {
        try {
            JsonObject root = JsonParser.parseString(rawJson).getAsJsonObject();
            JsonArray candidates = root.getAsJsonArray("candidates");
            if (candidates == null || candidates.isEmpty()) {
                return null;
            }

            JsonObject candidate = candidates.get(0).getAsJsonObject();
            JsonObject content = candidate.getAsJsonObject("content");
            if (content == null || !content.has("parts")) {
                return null;
            }

            StringBuilder responseText = new StringBuilder();
            for (JsonElement part : content.getAsJsonArray("parts")) {
                JsonObject partObject = part.getAsJsonObject();
                if (partObject.has("text") && !partObject.get("text").isJsonNull()) {
                    responseText.append(partObject.get("text").getAsString());
                }
            }
            if (responseText.isEmpty()) {
                return null;
            }

            JsonObject extracted = JsonParser.parseString(stripCodeFence(responseText.toString())).getAsJsonObject();
            return new ParsedRoute(readString(extracted, "sourceId"), readString(extracted, "destinationId"));
        } catch (RuntimeException exception) {
            AppLogger.warn("Gemini returned an unreadable route-intent response.");
            return null;
        }
    }

    /** Parses the model's command JSON without trusting it as graph-safe input. */
    AICommand parseCityCommandResponse(String rawJson) {
        try {
            JsonObject root = JsonParser.parseString(rawJson).getAsJsonObject();
            JsonArray candidates = root.getAsJsonArray("candidates");
            if (candidates == null || candidates.isEmpty()) {
                return null;
            }
            JsonObject candidate = candidates.get(0).getAsJsonObject();
            JsonObject content = candidate.getAsJsonObject("content");
            if (content == null || !content.has("parts")) {
                return null;
            }

            StringBuilder responseText = new StringBuilder();
            for (JsonElement part : content.getAsJsonArray("parts")) {
                JsonObject partObject = part.getAsJsonObject();
                if (partObject.has("text") && !partObject.get("text").isJsonNull()) {
                    responseText.append(partObject.get("text").getAsString());
                }
            }
            JsonObject extracted = JsonParser.parseString(stripCodeFence(responseText.toString())).getAsJsonObject();
            AICommandType type = readCommandType(extracted);
            if (type == null) {
                return null;
            }
            return new AICommand(type, readString(extracted, "sourceId"), readString(extracted, "destinationId"),
                    readString(extracted, "locationId"), readString(extracted, "locationName"),
                    readString(extracted, "anchorId"), readNumber(extracted, "distanceKm"),
                    readBoolean(extracted, "oneWay"));
        } catch (RuntimeException exception) {
            AppLogger.warn("Gemini returned an unreadable city-command response.");
            return null;
        }
    }

    /** Converts model values to exact graph IDs and rejects unsafe route selections. */
    static ParsedRoute validateParsedRoute(ParsedRoute parsedRoute, Collection<Location> locations) {
        if (parsedRoute == null || parsedRoute.sourceId == null || parsedRoute.destinationId == null) {
            return null;
        }

        Map<String, String> canonicalIds = new LinkedHashMap<>();
        for (Location location : locations) {
            canonicalIds.put(location.getId().toLowerCase(Locale.ROOT), location.getId());
        }

        String sourceId = canonicalIds.get(parsedRoute.sourceId.trim().toLowerCase(Locale.ROOT));
        String destinationId = canonicalIds.get(parsedRoute.destinationId.trim().toLowerCase(Locale.ROOT));
        if (sourceId == null || destinationId == null || sourceId.equals(destinationId)) {
            return null;
        }
        return new ParsedRoute(sourceId, destinationId);
    }

    /** Converts command IDs to exact graph IDs and rejects invalid mutations. */
    static AICommand validateCityCommand(AICommand command, Collection<Location> locations) {
        if (command == null || locations == null) {
            return null;
        }
        Map<String, String> canonicalIds = new LinkedHashMap<>();
        for (Location location : locations) {
            canonicalIds.put(location.getId().toLowerCase(Locale.ROOT), location.getId());
        }

        return switch (command.type) {
            case FIND_ROUTE -> {
                String sourceId = canonicalId(command.sourceId, canonicalIds);
                String destinationId = canonicalId(command.destinationId, canonicalIds);
                yield sourceId == null || destinationId == null || sourceId.equals(destinationId) ? null
                        : new AICommand(command.type, sourceId, destinationId, null, null, null, 0, false);
            }
            case ADD_LOCATION -> {
                String locationId = trimmed(command.locationId);
                String locationName = trimmed(command.locationName);
                String anchorId = canonicalId(command.anchorId, canonicalIds);
                if (locationId == null || locationName == null || anchorId == null || !isPositiveFinite(command.distanceKm)
                        || canonicalIds.containsKey(locationId.toLowerCase(Locale.ROOT))) {
                    yield null;
                }
                yield new AICommand(command.type, null, null, locationId, locationName, anchorId,
                        command.distanceKm, command.oneWayRequested);
            }
            case REMOVE_LOCATION -> {
                String locationId = canonicalId(command.locationId, canonicalIds);
                yield locationId == null ? null
                        : new AICommand(command.type, null, null, locationId, null, null, 0, false);
            }
            case ADD_ROAD -> {
                String sourceId = canonicalId(command.sourceId, canonicalIds);
                String destinationId = canonicalId(command.destinationId, canonicalIds);
                if (sourceId == null || destinationId == null || sourceId.equals(destinationId)
                        || !isPositiveFinite(command.distanceKm)) {
                    yield null;
                }
                yield new AICommand(command.type, sourceId, destinationId, null, null, null,
                        command.distanceKm, command.oneWayRequested);
            }
            case REMOVE_ROAD -> {
                String sourceId = canonicalId(command.sourceId, canonicalIds);
                String destinationId = canonicalId(command.destinationId, canonicalIds);
                yield sourceId == null || destinationId == null || sourceId.equals(destinationId) ? null
                        : new AICommand(command.type, sourceId, destinationId, null, null, null, 0, false);
            }
        };
    }

    private static String readString(JsonObject object, String property) {
        if (!object.has(property) || object.get(property).isJsonNull()) {
            return null;
        }
        String value = object.get(property).getAsString().trim();
        return value.isEmpty() ? null : value;
    }

    private static AICommandType readCommandType(JsonObject object) {
        String action = readString(object, "action");
        if (action == null) {
            return null;
        }
        try {
            return AICommandType.valueOf(action.trim().toUpperCase(Locale.ROOT)
                    .replace('-', '_').replace(' ', '_'));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static double readNumber(JsonObject object, String property) {
        if (!object.has(property) || object.get(property).isJsonNull()) {
            return Double.NaN;
        }
        try {
            return object.get(property).getAsDouble();
        } catch (RuntimeException exception) {
            return Double.NaN;
        }
    }

    private static boolean readBoolean(JsonObject object, String property) {
        return object.has(property) && !object.get(property).isJsonNull()
                && object.get(property).getAsBoolean();
    }

    private static String canonicalId(String value, Map<String, String> canonicalIds) {
        String trimmed = trimmed(value);
        return trimmed == null ? null : canonicalIds.get(trimmed.toLowerCase(Locale.ROOT));
    }

    private static String trimmed(String value) {
        if (value == null) {
            return null;
        }
        String result = value.trim();
        return result.isEmpty() ? null : result;
    }

    private static boolean isPositiveFinite(double value) {
        return Double.isFinite(value) && value > 0;
    }

    private static String stripCodeFence(String text) {
        String trimmed = text.trim();
        if (!trimmed.startsWith("```")) {
            return trimmed;
        }
        int firstNewline = trimmed.indexOf('\n');
        int closingFence = trimmed.lastIndexOf("```");
        if (firstNewline >= 0 && closingFence > firstNewline) {
            return trimmed.substring(firstNewline + 1, closingFence).trim();
        }
        return trimmed.replace("```json", "").replace("```", "").trim();
    }

    private static String messageForApiError(int statusCode) {
        return switch (statusCode) {
            case 400 -> "The AI request was rejected. Please simplify your route request and try again.";
            case 401 -> "The Gemini API key is missing or invalid.";
            case 403 -> "The Gemini API key was rejected or lacks Generative Language API access.";
            case 404 -> "The configured Gemini model could not be found. The app's model name may be outdated.";
            case 429 -> "The AI assistant is busy. Please wait a moment and try again.";
            default -> statusCode >= 500
                    ? "The AI assistant is temporarily unavailable. Please try again later."
                    : "The AI route request could not be completed. Please choose locations manually.";
        };
    }

    private AICommand executeCommandRequest(String query, List<Location> locations) throws IOException {
        RequestBody body = RequestBody.create(gson.toJson(buildCommandRequestPayload(query, locations)), JSON_MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(GEMINI_API_URL)
                .header("x-goog-api-key", apiKey)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() == null ? "" : response.body().string();
            if (!response.isSuccessful()) {
                throw new GeminiApiException(response.code(), responseBody);
            }
            return parseCityCommandResponse(responseBody);
        }
    }

    /** Includes the provider response for actionable configuration diagnostics without exposing the API key. */
    private static String describeApiError(GeminiApiException exception) {
        String responseExcerpt = exception.responseBody == null ? "" : exception.responseBody.trim();
        if (responseExcerpt.length() > 700) {
            responseExcerpt = responseExcerpt.substring(0, 700) + "...";
        }
        return messageForApiError(exception.statusCode)
                + " [HTTP " + exception.statusCode + "]"
                + (responseExcerpt.isEmpty() ? "" : " " + responseExcerpt);
    }

    private static void dispatchSuccess(AIParserCallback callback, String sourceId, String destinationId) {
        runOnEventDispatchThread(() -> callback.onSuccess(sourceId, destinationId));
    }

    private static void dispatchSuccess(AICommandCallback callback, AICommand command) {
        runOnEventDispatchThread(() -> callback.onSuccess(command));
    }

    private static void dispatchFailure(AIParserCallback callback, String message) {
        runOnEventDispatchThread(() -> callback.onFailure(message));
    }

    private static void dispatchFailure(AICommandCallback callback, String message) {
        runOnEventDispatchThread(() -> callback.onFailure(message));
    }

    private static void runOnEventDispatchThread(Runnable action) {
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
        } else {
            SwingUtilities.invokeLater(action);
        }
    }

    static final class ParsedRoute {
        final String sourceId;
        final String destinationId;

        ParsedRoute(String sourceId, String destinationId) {
            this.sourceId = sourceId;
            this.destinationId = destinationId;
        }
    }

    /** Conservative, unambiguous local parsing for simple "A to B" style queries.
     * Returns a ParsedRoute containing canonical IDs (not names) when both sides
     * can be matched uniquely; otherwise returns null. */
    private ParsedRoute tryLocalParseRoute(String query, List<Location> locations) {
        if (query == null || query.isBlank()) {
            return null;
        }
        String lower = query.toLowerCase(Locale.ROOT).trim();

        // Try several common phrasings.
        String[] patterns = new String[] {
                "from\\s+(.+?)\\s+to\\s+(.+)",
                "between\\s+(.+?)\\s+and\\s+(.+)",
                "(.+?)\\s+to\\s+(.+)",
                "route\\s+from\\s+(.+?)\\s+to\\s+(.+)"
        };

        for (String pat : patterns) {
            Pattern p = Pattern.compile(pat, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);
            Matcher m = p.matcher(lower);
            if (m.find() && m.groupCount() >= 2) {
                String a = cleanupName(m.group(1));
                String b = cleanupName(m.group(2));
                String idA = matchUniqueLocationId(a, locations);
                String idB = matchUniqueLocationId(b, locations);
                if (idA != null && idB != null && !idA.equals(idB)) {
                    return new ParsedRoute(idA, idB);
                }
                // else ambiguous or same — keep trying other patterns
            }
        }

        // As a very conservative last attempt, split on '-' or '—' or '–' for "A - B" style
        Pattern dash = Pattern.compile("^(.+?)\\s*[\\-—–]\\s*(.+)$");
        Matcher md = dash.matcher(lower);
        if (md.find() && md.groupCount() >= 2) {
            String a = cleanupName(md.group(1));
            String b = cleanupName(md.group(2));
            String idA = matchUniqueLocationId(a, locations);
            String idB = matchUniqueLocationId(b, locations);
            if (idA != null && idB != null && !idA.equals(idB)) {
                return new ParsedRoute(idA, idB);
            }
        }

        return null;
    }

    /** Normalizes a matched fragment: trim, remove surrounding punctuation. */
    private static String cleanupName(String raw) {
        if (raw == null) return null;
        String r = raw.trim();
        // remove leading/trailing punctuation
        r = r.replaceAll("^[\\p{Punct}\\s]+|[\\p{Punct}\\s]+$", "");
        return r;
    }

    /** Attempts to uniquely match a location by id or name (case-insensitive).
     * Returns the canonical id when unique; otherwise null. */
    private static String matchUniqueLocationId(String token, List<Location> locations) {
        if (token == null || token.isBlank()) return null;
        String t = token.trim();
        String tLower = t.toLowerCase(Locale.ROOT);

        String foundId = null;
        int matches = 0;
        for (Location loc : locations) {
            if (loc.getId() != null && loc.getId().equalsIgnoreCase(t)) {
                // exact id match — highest priority
                return loc.getId();
            }
            if (loc.getName() != null && loc.getName().equalsIgnoreCase(t)) {
                foundId = loc.getId();
                matches++;
            }
        }
        if (matches == 1) return foundId;

        // If no exact name match, try contains (but only accept if unique)
        foundId = null;
        matches = 0;
        for (Location loc : locations) {
            if (loc.getName() != null && loc.getName().toLowerCase(Locale.ROOT).contains(tLower)) {
                foundId = loc.getId();
                matches++;
            }
        }
        if (matches == 1) return foundId;

        return null; // ambiguous or not found
    }

    private static final class GeminiApiException extends IOException {
        private final int statusCode;
        private final String responseBody;

        GeminiApiException(int statusCode, String responseBody) {
            this.statusCode = statusCode;
            this.responseBody = responseBody;
        }
    }
}