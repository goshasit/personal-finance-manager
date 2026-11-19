
package ru.finance.infrastructure.repository;

import com.google.gson.reflect.TypeToken;
import ru.finance.core.model.User;
import ru.finance.core.repository.UserRepository;
import ru.finance.infrastructure.persistence.JsonStorage;
import ru.finance.infrastructure.persistence.UserData;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JsonUserRepository implements UserRepository {

    private static final Type USER_LIST_TYPE =
            new TypeToken<List<UserData>>() {
            }.getType();

    private final Path filePath;
    private final JsonStorage storage;

    public JsonUserRepository(Path filePath, JsonStorage storage) {
        this.filePath = filePath;
        this.storage = storage;
    }

    @Override
    public synchronized void save(User user) {
        List<UserData> users = loadUserData();

        UserData userData = new UserData(user);

        users.removeIf(existing ->
                existing.getId().equals(user.getId())
        );

        users.add(userData);

        storage.write(filePath, users);
    }

    @Override
    public synchronized Optional<User> findByLogin(String login) {
        if (login == null || login.isBlank()) {
            return Optional.empty();
        }

        return loadUserData().stream()
                .filter(user ->
                        user.getLogin().equalsIgnoreCase(login.trim())
                )
                .map(UserData::toUser)
                .findFirst();
    }

    @Override
    public synchronized Optional<User> findById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }

        return loadUserData().stream()
                .filter(user -> user.getId().equals(id))
                .map(UserData::toUser)
                .findFirst();
    }

    @Override
    public synchronized List<User> findAll() {
        return loadUserData().stream()
                .map(UserData::toUser)
                .toList();
    }

    private List<UserData> loadUserData() {
        Optional<List<UserData>> users =
                storage.read(filePath, USER_LIST_TYPE);

        return users.orElseGet(() -> new ArrayList<UserData>());
    }
}
