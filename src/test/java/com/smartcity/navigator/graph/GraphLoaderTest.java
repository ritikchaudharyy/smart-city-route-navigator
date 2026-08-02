package com.smartcity.navigator.graph;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class GraphLoaderTest {

    @Test
    void loadFromFile_rejectsNonFiniteRoadWeights() throws IOException {
        assertInvalidGraph("""
                LOCATION,A,Alpha,0,0
                LOCATION,B,Beta,10,10
                ROAD,A,B,NaN
                """);
    }

    @Test
    void loadFromFile_rejectsMalformedRecordsInsteadOfIgnoringExtraData() throws IOException {
        assertInvalidGraph("LOCATION,A,Alpha,0,0,unexpected");
    }

    private void assertInvalidGraph(String data) throws IOException {
        Path graphFile = Files.createTempFile("smart-city-invalid-graph", ".dat");
        try {
            Files.writeString(graphFile, data);
            assertThrows(GraphLoadException.class, () -> GraphLoader.loadFromFile(graphFile.toFile()));
        } finally {
            Files.deleteIfExists(graphFile);
        }
    }
}
