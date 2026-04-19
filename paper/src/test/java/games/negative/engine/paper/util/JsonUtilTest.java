package games.negative.engine.paper.util;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonUtilTest {

    private static final Gson GSON = new Gson();

    @TempDir
    Path tempDir;

    @Test
    void saveToFileCreatesParentDirectories() {
        Path file = tempDir.resolve("nested/config/data.json");

        JsonUtil.saveToFile(file, new Payload("value"), GSON);

        assertTrue(Files.exists(file));
        Optional<Payload> payload = JsonUtil.loadFromFile(file, Payload.class, GSON);
        assertEquals("value", payload.orElseThrow().value());
    }

    @Test
    void typedSaveAndLoadRoundTripsCollections() {
        Path file = tempDir.resolve("typed/list.json");
        Type type = List.class;

        JsonUtil.saveToFile(file, List.of("a", "b"), type, GSON);

        Optional<List> payload = JsonUtil.loadTypeFromFile(file, type, GSON);
        assertIterableEquals(List.of("a", "b"), payload.orElseThrow());
    }

    private record Payload(String value) {
    }
}
