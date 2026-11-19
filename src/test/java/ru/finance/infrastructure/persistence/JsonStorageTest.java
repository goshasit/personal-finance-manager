
package ru.finance.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JsonStorageTest {

    @TempDir
    Path tempDir;

    private final JsonStorage storage = new JsonStorage();

    @Test
    void shouldSaveAndReadJson() {
        Path file = tempDir.resolve("test.json");

        TestData original = new TestData(
                "Test",
                new BigDecimal("1500.50"),
                LocalDateTime.of(2026, 11, 19, 10, 30)
        );

        storage.write(file, original);

        Optional<TestData> result =
                storage.read(file, TestData.class);

        assertTrue(result.isPresent());
        assertEquals("Test", result.get().name);
        assertEquals(
                0,
                original.amount.compareTo(result.get().amount)
        );
        assertEquals(original.date, result.get().date);
    }

    @Test
    void shouldReturnEmptyWhenFileDoesNotExist() {
        Path file = tempDir.resolve("missing.json");

        Optional<TestData> result =
                storage.read(file, TestData.class);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldCreateParentDirectories() {
        Path file = tempDir
                .resolve("nested")
                .resolve("folder")
                .resolve("test.json");

        TestData data = new TestData(
                "Nested",
                new BigDecimal("100"),
                LocalDateTime.now()
        );

        assertDoesNotThrow(() -> storage.write(file, data));
        assertTrue(java.nio.file.Files.exists(file));
    }

    private static class TestData {

        private String name;
        private BigDecimal amount;
        private LocalDateTime date;

        public TestData(
                String name,
                BigDecimal amount,
                LocalDateTime date
        ) {
            this.name = name;
            this.amount = amount;
            this.date = date;
        }
    }
}
