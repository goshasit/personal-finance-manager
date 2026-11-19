package ru.finance.core.service;

import org.mindrot.jbcrypt.BCrypt;
import ru.finance.core.model.User;
import ru.finance.core.model.Wallet;
import ru.finance.core.repository.UserRepository;
import ru.finance.core.repository.WalletRepository;

public class AuthService {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    public AuthService(
            UserRepository userRepository,
            WalletRepository walletRepository
    ) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
    }

    public User register(String login, String password) {
        validateLogin(login);
        validatePassword(password);

        if (userRepository.findByLogin(login.trim()).isPresent()) {
            throw new IllegalArgumentException(
                    "User with this login already exists"
            );
        }

        String passwordHash = BCrypt.hashpw(
                password,
                BCrypt.gensalt(12)
        );

        User user = new User(login.trim(), passwordHash);

        userRepository.save(user);
        walletRepository.save(user.getId(), user.getWallet());

        return user;
    }

    public User login(String login, String password) {
        validateLogin(login);

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Password cannot be empty"
            );
        }

        User user = userRepository.findByLogin(login.trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid login or password"
                ));

        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new IllegalArgumentException(
                    "Invalid login or password"
            );
        }

        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseGet(Wallet::new);

        user.setWallet(wallet);

        return user;
    }

    public void saveWallet(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        walletRepository.save(user.getId(), user.getWallet());
    }

    private void validateLogin(String login) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException(
                    "Login cannot be empty"
            );
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Password must contain at least "
                            + MIN_PASSWORD_LENGTH + " characters"
            );
        }
    }
}