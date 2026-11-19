
package ru.finance.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.finance.core.model.*;
import ru.finance.infrastructure.repository.JsonUserRepository;
import ru.finance.infrastructure.repository.JsonWalletRepository;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldSaveAndFindUserByLogin() {
        JsonStorage storage = new JsonStorage();

        JsonUserRepository repository = new JsonUserRepository(
                tempDir.resolve("users.json"),
                storage
        );

        User user = new User("gosha", "password-hash");

        repository.save(user);

        var result = repository.findByLogin("gosha");

        assertTrue(result.isPresent());
        assertEquals(user.getId(), result.get().getId());
        assertEquals("gosha", result.get().getLogin());
        assertEquals("password-hash", result.get().getPasswordHash());
    }

    @Test
    void shouldNotDuplicateUserWhenSavingAgain() {
        JsonStorage storage = new JsonStorage();

        JsonUserRepository repository = new JsonUserRepository(
                tempDir.resolve("users.json"),
                storage
        );

        User user = new User("gosha", "password-hash");

        repository.save(user);
        repository.save(user);

        assertEquals(1, repository.findAll().size());
    }

    @Test
    void shouldSaveAndLoadWallet() {
        JsonStorage storage = new JsonStorage();

        JsonWalletRepository repository = new JsonWalletRepository(
                tempDir.resolve("wallets"),
                storage
        );

        UUID userId = UUID.randomUUID();

        Wallet wallet = new Wallet();

        Category category = new Category(
                "Зарплата",
                CategoryType.INCOME
        );

        wallet.addCategory(category);

        wallet.addTransaction(new Transaction(
                new BigDecimal("50000"),
                TransactionType.INCOME,
                category.getId(),
                "Зарплата"
        ));

        repository.save(userId, wallet);

        var result = repository.findByUserId(userId);

        assertTrue(result.isPresent());

        Wallet loadedWallet = result.get();

        assertEquals(
                0,
                new BigDecimal("50000").compareTo(
                        loadedWallet.getBalance()
                )
        );

        assertEquals(1, loadedWallet.getTransactions().size());
        assertEquals(1, loadedWallet.getCategories().size());
    }

    @Test
    void shouldReturnEmptyWhenWalletDoesNotExist() {
        JsonStorage storage = new JsonStorage();

        JsonWalletRepository repository = new JsonWalletRepository(
                tempDir.resolve("wallets"),
                storage
        );

        assertTrue(
                repository.findByUserId(UUID.randomUUID()).isEmpty()
        );
    }
}
