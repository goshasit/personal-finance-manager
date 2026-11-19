
package ru.finance.core.repository;

import ru.finance.core.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    void save(User user);

    Optional<User> findByLogin(String login);

    Optional<User> findById(UUID id);

    List<User> findAll();
}
