package com.smartcity.navigator.service.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.smartcity.navigator.model.Location;

class GeminiServiceTest {

    private final GeminiService geminiService = new GeminiService();
    private final List<Location> locations = List.of(
            new Location("L1", "Home"),
            new Location("L2", "Hospital"),
            new Location("L7", "Airport"));

    @Test
    void parsesStructuredGeminiResponseAndCanonicalizesKnownIds() {
        String response = """
                {
                  "candidates": [{
                    "content": {"parts": [{"text": "{\\"sourceId\\":\\"l1\\",\\"destinationId\\":\\"L7\\"}"}]}
                  }]
                }
                """;

        GeminiService.ParsedRoute parsed = geminiService.parseGeminiResponse(response);
        GeminiService.ParsedRoute validated = GeminiService.validateParsedRoute(parsed, locations);

        assertNotNull(validated);
        assertEquals("L1", validated.sourceId);
        assertEquals("L7", validated.destinationId);
    }

    @Test
    void rejectsMalformedOrGraphInvalidAiSelections() {
        GeminiService.ParsedRoute malformed = geminiService.parseGeminiResponse("{\"candidates\": []}");

        assertNull(malformed);
        assertNull(GeminiService.validateParsedRoute(
                new GeminiService.ParsedRoute("L1", "UNKNOWN"), locations));
        assertNull(GeminiService.validateParsedRoute(
                new GeminiService.ParsedRoute("L2", "L2"), locations));
    }

    @Test
    void configuresStructuredOutputWithEnoughBudgetForMinimalThinking() throws Exception {
        Method buildPayload = GeminiService.class.getDeclaredMethod(
                "buildRequestPayload", String.class, List.class);
        buildPayload.setAccessible(true);

        JsonObject payload = (JsonObject) buildPayload.invoke(
                geminiService, "Take me from Home to Airport", locations);
        JsonObject generationConfig = payload.getAsJsonObject("generationConfig");

        assertEquals("application/json", generationConfig.get("responseMimeType").getAsString());
        assertEquals("minimal", generationConfig.getAsJsonObject("thinkingConfig")
                .get("thinkingLevel").getAsString());
        assertEquals(256, generationConfig.get("maxOutputTokens").getAsInt());
    }
}
