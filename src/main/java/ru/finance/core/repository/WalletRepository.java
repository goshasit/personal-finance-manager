
package ru.finance.core.repository;

import ru.finance.core.model.Wallet;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository {

    void save(UUID userId, Wallet wallet);

    Optional<Wallet> findByUserId(UUID userId);
}
