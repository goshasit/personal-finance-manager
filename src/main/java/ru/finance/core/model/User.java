
package ru.finance.core.model;

import java.util.Objects;
import java.util.UUID;

public class User {

    private UUID id;
    private String login;
    private String passwordHash;
    private Wallet wallet;

    public User(String login, String passwordHash) {
        this(UUID.randomUUID(), login, passwordHash);
    }

    public User(UUID id, String login, String passwordHash) {
        if (id == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Login cannot be empty");
        }

        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException(
                    "Password hash cannot be empty"
            );
        }

        this.id = id;
        this.login = login.trim();
        this.passwordHash = passwordHash;
        this.wallet = new Wallet();
    }

    public UUID getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = Objects.requireNonNull(
                wallet,
                "Wallet cannot be null"
        );
    }

    public boolean checkPasswordHash(String passwordHash) {
        return Objects.equals(this.passwordHash, passwordHash);
    }
}
