
package ru.finance.infrastructure.persistence;

import ru.finance.core.model.User;

import java.util.UUID;

public class UserData {

    private UUID id;
    private String login;
    private String passwordHash;

    public UserData() {
    }

    public UserData(User user) {
        this.id = user.getId();
        this.login = user.getLogin();
        this.passwordHash = user.getPasswordHash();
    }

    public User toUser() {
        return new User(id, login, passwordHash);
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
}
