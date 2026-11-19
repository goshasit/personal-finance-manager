package ru.finance.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.finance.core.model.User;
import ru.finance.core.model.Wallet;
import ru.finance.core.repository.UserRepository;
import ru.finance.core.repository.WalletRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private AuthService authService;
    private InMemoryUserRepository userRepository;
    private InMemoryWalletRepository walletRepository;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        walletRepository = new InMemoryWalletRepository();

        authService = new AuthService(
                userRepository,
                walletRepository
        );
    }

    @Test
    void shouldRegisterUser() {
        User user = authService.register("testuser", "password123");

        assertNotNull(user.getId());
        assertEquals("testuser", user.getLogin());
        assertTrue(userRepository.findByLogin("testuser").isPresent());
    }

    @Test
    void shouldStorePasswordAsHash() {
        User user = authService.register("testuser", "password123");

        assertNotEquals("password123", user.getPasswordHash());
    }

    @Test
    void shouldNotRegisterDuplicateLogin() {
        authService.register("testuser", "password123");

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.register("testuser", "password456")
        );
    }

    @Test
    void shouldLoginWithCorrectPassword() {
        authService.register("testuser", "password123");

        User loggedInUser = authService.login(
                "testuser",
                "password123"
        );

        assertEquals("testuser", loggedInUser.getLogin());
    }

    @Test
    void shouldRejectIncorrectPassword() {
        authService.register("testuser", "password123");

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.login("testuser", "wrongpassword")
        );
    }

    @Test
    void shouldRejectShortPassword() {
        assertThrows(
                IllegalArgumentException.class,
                () -> authService.register("testuser", "123")
        );
    }

    @Test
    void shouldSaveAndLoadWallet() {
        User user = authService.register("testuser", "password123");

        user.getWallet().addCategory(
                new ru.finance.core.model.Category(
                        "Food",
                        ru.finance.core.model.CategoryType.EXPENSE
                )
        );

        authService.saveWallet(user);

        User loggedInUser = authService.login(
                "testuser",
                "password123"
        );

        assertEquals(1, loggedInUser.getWallet().getCategories().size());
    }

    private static class InMemoryUserRepository
            implements UserRepository {

        private final List<User> users = new ArrayList<>();

        @Override
        public void save(User user) {
            users.removeIf(existing ->
                    existing.getId().equals(user.getId())
            );
            users.add(user);
        }

        @Override
        public Optional<User> findByLogin(String login) {
            return users.stream()
                    .filter(user ->
                            user.getLogin().equalsIgnoreCase(login)
                    )
                    .findFirst();
        }

        @Override
        public Optional<User> findById(UUID id) {
            return users.stream()
                    .filter(user -> user.getId().equals(id))
                    .findFirst();
        }

        @Override
        public List<User> findAll() {
            return new ArrayList<>(users);
        }
    }

    private static class InMemoryWalletRepository
            implements WalletRepository {

        private final java.util.Map<UUID, Wallet> wallets =
                new java.util.HashMap<>();

        @Override
        public void save(UUID userId, Wallet wallet) {
            wallets.put(userId, wallet);
        }

        @Override
        public Optional<Wallet> findByUserId(UUID userId) {
            return Optional.ofNullable(wallets.get(userId));
        }
    }
}