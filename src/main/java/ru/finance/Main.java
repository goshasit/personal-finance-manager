
package ru.finance;

import ru.finance.cli.ConsoleApplication;
import ru.finance.core.service.AuthService;
import ru.finance.core.service.FinanceService;
import ru.finance.infrastructure.persistence.JsonStorage;
import ru.finance.infrastructure.repository.JsonUserRepository;
import ru.finance.infrastructure.repository.JsonWalletRepository;

import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws Exception {
        Path dataDirectory = Path.of("data");
        Path walletsDirectory = dataDirectory.resolve("wallets");

        Files.createDirectories(dataDirectory);
        Files.createDirectories(walletsDirectory);

        JsonStorage storage = new JsonStorage();

        JsonUserRepository userRepository =
                new JsonUserRepository(
                        dataDirectory.resolve("users.json"),
                        storage
                );

        JsonWalletRepository walletRepository =
                new JsonWalletRepository(
                        walletsDirectory,
                        storage
                );

        AuthService authService =
                new AuthService(userRepository, walletRepository);

        FinanceService financeService =
                new FinanceService(walletRepository);

        ConsoleApplication application =
                new ConsoleApplication(authService, financeService);

        application.run();
    }
}
