
package ru.finance.infrastructure.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class JsonStorage {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final Gson gson;

    public JsonStorage() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(
                        LocalDateTime.class,
                        (com.google.gson.JsonSerializer<LocalDateTime>)
                                (source, type, context) ->
                                        context.serialize(
                                                source.format(FORMATTER)
                                        )
                )
                .registerTypeAdapter(
                        LocalDateTime.class,
                        (com.google.gson.JsonDeserializer<LocalDateTime>)
                                (json, type, context) -> {
                                    try {
                                        return LocalDateTime.parse(
                                                json.getAsString(),
                                                FORMATTER
                                        );
                                    } catch (DateTimeParseException exception) {
                                        throw new JsonParseException(
                                                "Invalid date format",
                                                exception
                                        );
                                    }
                                }
                )
                .setPrettyPrinting()
                .create();
    }

    public <T> void write(Path path, T data) {
        try {
            Path parent = path.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (Writer writer = Files.newBufferedWriter(
                    path,
                    StandardCharsets.UTF_8
            )) {
                gson.toJson(data, writer);
            }
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Cannot save JSON file: " + path,
                    exception
            );
        }
    }

    public <T> Optional<T> read(Path path, Type type) {
        if (!Files.exists(path)) {
            return Optional.empty();
        }

        try (Reader reader = Files.newBufferedReader(
                path,
                StandardCharsets.UTF_8
        )) {
            T result = gson.fromJson(reader, type);
            return Optional.ofNullable(result);
        } catch (IOException | JsonParseException exception) {
            throw new IllegalStateException(
                    "Cannot read JSON file: " + path,
                    exception
            );
        }
    }
}
