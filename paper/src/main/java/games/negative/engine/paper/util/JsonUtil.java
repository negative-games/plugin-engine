package games.negative.engine.paper.util;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Utility class for JSON operations using Gson.
 */
@Slf4j
public final class JsonUtil {

    private JsonUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Loads a JSON object from a file.
     * @param file the file to load from
     * @param clazz the class of the object to load
     * @param gson the Gson instance to use for deserialization
     * @param <T> the type of the object to load
     * @return an Optional containing the loaded object, or empty if loading failed
     */
    public static <T> Optional<T> loadFromFile(File file, Class<T> clazz, Gson gson) {
        return loadFromFile(path(file), clazz, gson);
    }

    public static <T> Optional<T> loadFromFile(Path file, Class<T> clazz, Gson gson) {
        if (!isRegularFile(file)) {
            log.error("File {} does not exist or is not a valid file", pathString(file));
            return Optional.empty();
        }

        try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            T object = gson.fromJson(reader, clazz);
            return Optional.ofNullable(object);
        } catch (IOException exception) {
            log.error("Failed to load json from file {}", file.toAbsolutePath(), exception);
            return Optional.empty();
        }
    }

    /**
     * Loads a JSON object from a file using a Type.
     * @param file the file to load from
     * @param type the Type of the object to load
     * @param gson the Gson instance to use for deserialization
     * @param <T> the type of the object to load
     * @return an Optional containing the loaded object, or empty if loading failed
     */
    public static <T> Optional<T> loadTypeFromFile(File file, Type type, Gson gson) {
        return loadTypeFromFile(path(file), type, gson);
    }

    public static <T> Optional<T> loadTypeFromFile(Path file, Type type, Gson gson) {
        if (!isRegularFile(file)) {
            log.error("File {} does not exist or is not a valid file", pathString(file));
            return Optional.empty();
        }

        try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            T object = gson.fromJson(reader, type);
            return Optional.ofNullable(object);
        } catch (IOException exception) {
            log.error("Failed to load json from file {}", file.toAbsolutePath(), exception);
            return Optional.empty();
        }
    }

    /**
     * Loads all JSON objects from a directory.
     * @param directory the directory to load from
     * @param clazz the class of the objects to load
     * @param gson the Gson instance to use for deserialization
     * @param <T> the type of the objects to load
     * @return a collection of loaded objects
     */
    public static <T> Collection<T> loadFromDirectory(File directory, Class<T> clazz, Gson gson) {
        return loadFromDirectory(path(directory), clazz, gson);
    }

    public static <T> Collection<T> loadFromDirectory(Path directory, Class<T> clazz, Gson gson) {
        if (!isDirectory(directory)) {
            log.error("Directory {} does not exist or is not a valid directory", pathString(directory));
            return Collections.emptyList();
        }

        try (Stream<Path> paths = Files.list(directory)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .map(path -> loadFromFile(path, clazz, gson))
                    .flatMap(Optional::stream)
                    .toList();
        } catch (IOException exception) {
            log.error("Failed to read json directory {}", directory.toAbsolutePath(), exception);
            return Collections.emptyList();
        }
    }

    /**
     * Saves a JSON object to a file.
     * @param file the file to save to
     * @param object the object to save
     * @param gson the Gson instance to use for serialization
     * @param <T> the type of the object to save
     */
    public static <T> void saveToFile(File file, T object, Gson gson) {
        saveToFile(path(file), object, gson);
    }

    public static <T> void saveToFile(Path file, T object, Gson gson) {
        writeJson(file, gson, object, null);
    }

    /**
     * Saves a JSON object to a file using a Type.
     * @param file the file to save to
     * @param object the object to save
     * @param type the Type of the object to save
     * @param gson the Gson instance to use for serialization
     */
    public static <T> void saveToFile(File file, T object, Type type, Gson gson) {
        saveToFile(path(file), object, type, gson);
    }

    public static <T> void saveToFile(Path file, T object, Type type, Gson gson) {
        writeJson(file, gson, object, type);
    }

    private static Path path(File file) {
        return file == null ? null : file.toPath();
    }

    private static boolean isRegularFile(Path file) {
        return file != null && Files.isRegularFile(file);
    }

    private static boolean isDirectory(Path directory) {
        return directory != null && Files.isDirectory(directory);
    }

    private static String pathString(Path path) {
        return path == null ? "null" : path.toAbsolutePath().toString();
    }

    private static void prepareParentDirectory(Path file) throws IOException {
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

    private static <T> void writeJson(Path file, Gson gson, T object, Type type) {
        if (file == null) {
            log.error("File is null, cannot save object");
            return;
        }

        try {
            prepareParentDirectory(file);
            try (var writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                if (type == null) {
                    gson.toJson(object, writer);
                } else {
                    gson.toJson(object, type, writer);
                }
            }
        } catch (IOException exception) {
            log.error("Failed to save to file {}", file.toAbsolutePath(), exception);
        }
    }
}
