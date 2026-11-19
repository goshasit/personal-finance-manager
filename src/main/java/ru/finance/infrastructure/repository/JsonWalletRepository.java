
package ru.finance.infrastructure.repository;

import ru.finance.core.model.Wallet;
import ru.finance.core.repository.WalletRepository;
import ru.finance.infrastructure.persistence.JsonStorage;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

public class JsonWalletRepository implements WalletRepository {

    private final Path directory;
    private final JsonStorage storage;

    public JsonWalletRepository(Path directory, JsonStorage storage) {
        this.directory = directory;
        this.storage = storage;
    }

    @Override
    public synchronized void save(UUID userId, Wallet wallet) {
        validateUserId(userId);

        if (wallet == null) {
            throw new IllegalArgumentException(
                    "Wallet cannot be null"
            );
        }

        Path filePath = getWalletPath(userId);

        storage.write(filePath, wallet);
    }

    @Override
    public synchronized Optional<Wallet> findByUserId(UUID userId) {
        validateUserId(userId);

        Path filePath = getWalletPath(userId);

        return storage.read(filePath, Wallet.class);
    }

    private Path getWalletPath(UUID userId) {
        return directory.resolve(userId + ".json");
    }

    private void validateUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "User ID cannot be null"
            );
        }
    }
}
