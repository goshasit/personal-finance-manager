
package ru.finance.cli;

import ru.finance.core.model.Category;
import ru.finance.core.model.CategoryType;
import ru.finance.core.model.Transaction;
import ru.finance.core.model.TransactionType;
import ru.finance.core.model.User;
import ru.finance.core.service.AuthService;
import ru.finance.core.service.FinanceService;
import ru.finance.core.model.Budget;
import ru.finance.infrastructure.persistence.JsonStorage;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.List;

public class ConsoleApplication {

    private final AuthService authService;
    private final FinanceService financeService;
    private final Scanner scanner;

    private User currentUser;

    public ConsoleApplication(
            AuthService authService,
            FinanceService financeService
    ) {
        this.authService = authService;
        this.financeService = financeService;
        this.scanner = new Scanner(
                System.in,
                java.nio.charset.StandardCharsets.UTF_8
        );
    }

    public void run() {
        System.out.println("=== PERSONAL FINANCE MANAGER ===");

        while (true) {
            if (currentUser == null) {
                showAuthMenu();
            } else {
                showMainMenu();
            }
        }
    }

    private void showAuthMenu() {
        System.out.println("\n========== МЕНЮ ==========");
        System.out.println("1. Регистрация");
        System.out.println("2. Вход");
        System.out.println("0. Выход");
        System.out.print("Выберите действие: ");

        String choice = scanner.nextLine();

        try {
            switch (choice) {
                case "1" -> register();
                case "2" -> login();
                case "0" -> System.exit(0);
                default -> System.out.println("Неизвестная команда");
            }
        } catch (RuntimeException exception) {
            System.out.println("Ошибка: " + exception.getMessage());
        }
    }

    private void register() {
        System.out.print("Введите логин: ");
        String login = scanner.nextLine();

        System.out.print("Введите пароль: ");
        String password = scanner.nextLine();

        currentUser = authService.register(login, password);

        System.out.println("Регистрация успешна!");
        System.out.println("Добро пожаловать, " + currentUser.getLogin());
    }

    private void login() {
        System.out.print("Введите логин: ");
        String login = scanner.nextLine();

        System.out.print("Введите пароль: ");
        String password = scanner.nextLine();

        currentUser = authService.login(login, password);

        System.out.println("Авторизация успешна!");
        System.out.println("Добро пожаловать, " + currentUser.getLogin());
    }

    private void showMainMenu() {
        System.out.println("\n========== ГЛАВНОЕ МЕНЮ ==========");
        System.out.println("Пользователь: " + currentUser.getLogin());
        System.out.println("1. Просмотреть баланс");
        System.out.println("2. Просмотреть статистику");
        System.out.println("3. Создать категорию");
        System.out.println("4. Просмотреть категории");
        System.out.println("5. Добавить доход");
        System.out.println("6. Добавить расход");
        System.out.println("7. Просмотреть операции");
        System.out.println("8. Выйти из аккаунта");
        System.out.println("9. Редактировать операцию");
        System.out.println("10. Удалить операцию");
        System.out.println("11. Фильтр операций по периоду");
        System.out.println("12. Фильтр операций по категориям");
        System.out.println("13. Создать бюджет");
        System.out.println("14. Просмотреть бюджеты");
        System.out.println("15. Изменить бюджет");
        System.out.println("16. Экспорт операций в CSV");
        System.out.println("17. Импорт операций из CSV");
        System.out.println("18. Экспорт операций в JSON");
        System.out.println("19. Импорт операций из JSON");
        System.out.println("20. Редактировать категорию");
        System.out.println("21. Удалить категорию");
        System.out.println("0. Завершить работу");
        System.out.print("Выберите действие: ");

        String choice = scanner.nextLine();

        try {
            switch (choice) {
                case "1" -> showBalance();
                case "2" -> showStatistics();
                case "3" -> createCategory();
                case "4" -> showCategories();
                case "5" -> addIncome();
                case "6" -> addExpense();
                case "7" -> showTransactions();
                case "8" -> currentUser = null;
                case "9" -> updateTransaction();
                case "10" -> deleteTransaction();
                case "11" -> filterTransactionsByPeriod();
                case "12" -> filterTransactionsByCategories();
                case "13" -> createBudget();
                case "14" -> showBudgets();
                case "15" -> updateBudget();
                case "16" -> exportTransactionsToCsv();
                case "17" -> importTransactionsFromCsv();
                case "18" -> exportTransactionsToJson();
                case "19" -> importTransactionsFromJson();
                case "20" -> updateCategory();
                case "21" -> deleteCategory();
                case "0" -> System.exit(0);
                default -> System.out.println("Неизвестная команда");
            }
        } catch (RuntimeException exception) {
            System.out.println("Ошибка: " + exception.getMessage());
        }
    }

    private void showBalance() {
        System.out.println("Баланс: "
                + financeService.getBalance(currentUser));
    }

    private void showStatistics() {
        System.out.println("Доходы: "
                + financeService.getTotalIncome(currentUser));

        System.out.println("Расходы: "
                + financeService.getTotalExpense(currentUser));

        System.out.println("Баланс: "
                + financeService.getBalance(currentUser));

        System.out.println("\nСтатистика по категориям:");

        for (Category category : financeService.getCategories(currentUser)) {
            System.out.println(
                    category.getName() + ": "
                            + financeService.getCategoryTotal(
                            currentUser,
                            category.getId()
                    )
            );
        }
    }

    private void createCategory() {
        System.out.print("Введите название категории: ");
        String name = scanner.nextLine();

        System.out.println("1. Доход");
        System.out.println("2. Расход");
        System.out.print("Выберите тип категории: ");

        String choice = scanner.nextLine();

        CategoryType type = switch (choice) {
            case "1" -> CategoryType.INCOME;
            case "2" -> CategoryType.EXPENSE;
            default -> throw new IllegalArgumentException(
                    "Некорректный тип категории"
            );
        };

        financeService.createCategory(currentUser, name, type);

        System.out.println("Категория создана!");
    }

    private void showCategories() {
        List<Category> categories =
                financeService.getCategories(currentUser);

        if (categories.isEmpty()) {
            System.out.println("Категорий пока нет");
            return;
        }

        System.out.println("\n========== КАТЕГОРИИ ==========");

        for (int i = 0; i < categories.size(); i++) {
            Category category = categories.get(i);

            System.out.printf(
                    "%d. %s (%s)%n",
                    i + 1,
                    category.getName(),
                    category.getType()
            );
        }
    }


    private void updateCategory() {
        List<Category> categories =
                financeService.getCategories(currentUser);

        if (categories.isEmpty()) {
            System.out.println("Категорий пока нет.");
            return;
        }

        showCategories();

        System.out.print("Введите номер категории: ");

        int index;

        try {
            index = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Введите номер числом.");
            return;
        }

        if (index < 0 || index >= categories.size()) {
            System.out.println("Некорректный номер категории.");
            return;
        }

        Category category = categories.get(index);

        System.out.print("Введите новое название: ");
        String newName = scanner.nextLine();

        financeService.updateCategory(
                currentUser,
                category.getId(),
                newName
        );

        System.out.println("Категория обновлена!");
    }

    private void deleteCategory() {
        List<Category> categories =
                financeService.getCategories(currentUser);

        if (categories.isEmpty()) {
            System.out.println("Категорий пока нет.");
            return;
        }

        showCategories();

        System.out.print("Введите номер категории: ");

        int index;

        try {
            index = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Введите номер числом.");
            return;
        }

        if (index < 0 || index >= categories.size()) {
            System.out.println("Некорректный номер категории.");
            return;
        }

        Category category = categories.get(index);

        financeService.deleteCategory(
                currentUser,
                category.getId()
        );

        System.out.println("Категория удалена!");
    }


    private void addIncome() {
        addTransaction(TransactionType.INCOME);
    }

    private void addExpense() {
        addTransaction(TransactionType.EXPENSE);
    }

    private void addTransaction(TransactionType type) {
        List<Category> categories =
                financeService.getCategories(currentUser);

        List<Category> availableCategories = categories.stream()
                .filter(category ->
                        category.getType().name().equals(type.name())
                )
                .toList();

        if (availableCategories.isEmpty()) {
            System.out.println(
                    "Сначала создайте категорию для этой операции"
            );
            return;
        }

        System.out.print("Введите сумму: ");
        BigDecimal amount;

        try {
            amount = new BigDecimal(scanner.nextLine());

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Сумма должна быть больше нуля");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Введите корректную сумму");
        }

        System.out.println("Выберите категорию:");

        for (int i = 0; i < availableCategories.size(); i++) {
            System.out.printf(
                    "%d. %s%n",
                    i + 1,
                    availableCategories.get(i).getName()
            );
        }

        System.out.print("Номер категории: ");
        int categoryIndex;

        try {
            categoryIndex = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Введите номер категории числом.");
            return;
        }

        if (categoryIndex < 0
                || categoryIndex >= availableCategories.size()) {
            System.out.println("Некорректный номер категории.");
            return;
        }

        Category category = availableCategories.get(categoryIndex);

        System.out.print("Введите описание: ");
        String description = scanner.nextLine();

        if (type == TransactionType.INCOME) {
            financeService.addIncome(
                    currentUser,
                    amount,
                    category.getId(),
                    description
            );
        } else {
            financeService.addExpense(
                    currentUser,
                    amount,
                    category.getId(),
                    description
            );
        }

        System.out.println("Операция успешно добавлена!");

        if (type == TransactionType.EXPENSE) {
            notifyBudgetStatus(category);
        }
    }

    private void showTransactions() {
        List<Transaction> transactions =
                financeService.getTransactions(currentUser);

        if (transactions.isEmpty()) {
            System.out.println("Операций пока нет");
            return;
        }

        System.out.println("\n========== ОПЕРАЦИИ ==========");

        for (Transaction transaction : transactions) {
            String categoryName = financeService
                    .getCategories(currentUser)
                    .stream()
                    .filter(category ->
                            category.getId().equals(
                                    transaction.getCategoryId()
                            )
                    )
                    .map(Category::getName)
                    .findFirst()
                    .orElse("Неизвестная категория");

            System.out.println("----------------------------");
            System.out.println("Тип: " + transaction.getType());
            System.out.println("Сумма: " + transaction.getAmount());
            System.out.println("Категория: " + categoryName);
            System.out.println("Описание: "
                    + transaction.getDescription());
            System.out.println("Дата: " + transaction.getDate());
        }

        System.out.println("----------------------------");
    }



    private void updateTransaction() {
        List<Transaction> transactions =
                financeService.getTransactions(currentUser);

        if (transactions.isEmpty()) {
            System.out.println("Операций пока нет.");
            return;
        }

        showTransactions();

        System.out.print("Введите номер операции: ");

        int index;

        try {
            index = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Введите номер числом.");
            return;
        }

        if (index < 0 || index >= transactions.size()) {
            System.out.println("Некорректный номер операции.");
            return;
        }

        Transaction transaction = transactions.get(index);

        List<Category> categories =
                financeService.getCategories(currentUser).stream()
                        .filter(category ->
                                category.getType().name().equals(transaction.getType().name())
                        )
                        .toList();

        if (categories.isEmpty()) {
            System.out.println("Нет подходящих категорий.");
            return;
        }

        System.out.print("Введите новую сумму: ");

        BigDecimal amount;

        try {
            amount = new BigDecimal(scanner.nextLine());

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("Сумма должна быть больше нуля.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Некорректная сумма.");
            return;
        }

        System.out.println("Выберите новую категорию:");

        for (int i = 0; i < categories.size(); i++) {
            System.out.printf(
                    "%d. %s%n",
                    i + 1,
                    categories.get(i).getName()
            );
        }

        System.out.print("Номер категории: ");

        int categoryIndex;

        try {
            categoryIndex = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Введите номер числом.");
            return;
        }

        if (categoryIndex < 0 || categoryIndex >= categories.size()) {
            System.out.println("Некорректный номер категории.");
            return;
        }

        System.out.print("Введите новое описание: ");
        String description = scanner.nextLine();

        financeService.updateTransaction(
                currentUser,
                transaction.getId(),
                amount,
                categories.get(categoryIndex).getId(),
                description
        );

        System.out.println("Операция успешно обновлена!");
    }

    private void deleteTransaction() {
        List<Transaction> transactions =
                financeService.getTransactions(currentUser);

        if (transactions.isEmpty()) {
            System.out.println("Операций пока нет.");
            return;
        }

        showTransactions();

        System.out.print("Введите номер операции для удаления: ");

        int index;

        try {
            index = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Введите номер числом.");
            return;
        }

        if (index < 0 || index >= transactions.size()) {
            System.out.println("Некорректный номер операции.");
            return;
        }

        financeService.deleteTransaction(
                currentUser,
                transactions.get(index).getId()
        );

        System.out.println("Операция удалена!");
    }




    private void filterTransactionsByPeriod() {
        try {
            System.out.println(
                    "Введите начальную дату (например, 2026-01-01T00:00)"
            );
            LocalDateTime from = LocalDateTime.parse(scanner.nextLine());

            System.out.println(
                    "Введите конечную дату (например, 2026-12-31T23:59)"
            );
            LocalDateTime to = LocalDateTime.parse(scanner.nextLine());

            if (from.isAfter(to)) {
                System.out.println(
                        "Начальная дата не может быть позже конечной."
                );
                return;
            }

            List<Transaction> transactions =
                    financeService.getTransactionsByPeriod(
                            currentUser,
                            from,
                            to
                    );

            if (transactions.isEmpty()) {
                System.out.println("Операций за этот период нет.");
                return;
            }

            System.out.println("\n========== ОПЕРАЦИИ ==========");

            for (Transaction transaction : transactions) {
                String categoryName = financeService
                        .getCategories(currentUser)
                        .stream()
                        .filter(category ->
                                category.getId().equals(
                                        transaction.getCategoryId()
                                )
                        )
                        .map(Category::getName)
                        .findFirst()
                        .orElse("Неизвестная категория");

                System.out.println("----------------------------");
                System.out.println("Тип: " + transaction.getType());
                System.out.println("Сумма: " + transaction.getAmount());
                System.out.println("Категория: " + categoryName);
                System.out.println("Описание: " + transaction.getDescription());
                System.out.println("Дата: " + transaction.getDate());
            }

            System.out.println("----------------------------");

        } catch (java.time.format.DateTimeParseException e) {
            System.out.println(
                    "Некорректный формат даты. Используйте yyyy-MM-ddTHH:mm"
            );
        }
    }




    private void filterTransactionsByCategories() {
        List<Category> categories =
                financeService.getCategories(currentUser);

        if (categories.isEmpty()) {
            System.out.println("Категорий пока нет.");
            return;
        }

        System.out.println("Выберите категорию:");

        for (int i = 0; i < categories.size(); i++) {
            System.out.printf(
                    "%d. %s (%s)%n",
                    i + 1,
                    categories.get(i).getName(),
                    categories.get(i).getType()
            );
        }

        System.out.print("Номер категории: ");

        int index;

        try {
            index = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Введите номер категории числом.");
            return;
        }

        if (index < 0 || index >= categories.size()) {
            System.out.println("Некорректный номер категории.");
            return;
        }

        List<Transaction> transactions =
                financeService.getTransactionsByCategories(
                        currentUser,
                        List.of(categories.get(index).getId())
                );

        printFilteredTransactions(transactions);
    }


    private void printFilteredTransactions(
            List<Transaction> transactions
    ) {
        if (transactions.isEmpty()) {
            System.out.println("Операций не найдено");
            return;
        }

        System.out.println("\n========== РЕЗУЛЬТАТЫ ==========");

        for (Transaction transaction : transactions) {
            System.out.println("----------------------------");
            System.out.println("Тип: " + transaction.getType());
            System.out.println("Сумма: " + transaction.getAmount());
            System.out.println("Описание: " + transaction.getDescription());
            System.out.println("Дата: " + transaction.getDate());
        }
    }


    private void createBudget() {
        List<Category> categories =
                financeService.getCategories(currentUser)
                        .stream()
                        .filter(category ->
                                category.getType() == CategoryType.EXPENSE
                        )
                        .toList();

        if (categories.isEmpty()) {
            System.out.println("Сначала создайте категорию расходов.");
            return;
        }

        System.out.println("\n========== СОЗДАНИЕ БЮДЖЕТА ==========");

        for (int i = 0; i < categories.size(); i++) {
            System.out.printf(
                    "%d. %s%n",
                    i + 1,
                    categories.get(i).getName()
            );
        }

        System.out.print("Выберите категорию: ");

        int index;

        try {
            index = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Введите номер числом.");
            return;
        }

        if (index < 0 || index >= categories.size()) {
            System.out.println("Некорректный номер категории.");
            return;
        }

        Category category = categories.get(index);

        System.out.print("Введите месячный лимит: ");

        BigDecimal limit;

        try {
            limit = new BigDecimal(scanner.nextLine());

            if (limit.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("Лимит должен быть больше нуля.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Некорректная сумма.");
            return;
        }

        financeService.createBudget(
                currentUser,
                category.getId(),
                limit
        );

        System.out.println("Бюджет успешно создан!");
    }


    private void showBudgets() {
        List<Budget> budgets = financeService.getBudgets(currentUser);

        if (budgets.isEmpty()) {
            System.out.println("Бюджетов пока нет");
            return;
        }

        System.out.println("\n========== МОИ БЮДЖЕТЫ ==========");

        for (Budget budget : budgets) {
            Category category = financeService.getCategories(currentUser)
                    .stream()
                    .filter(item -> item.getId().equals(budget.getCategoryId()))
                    .findFirst()
                    .orElseThrow();

            BigDecimal spent = financeService.getBudgetSpent(
                    currentUser,
                    budget.getCategoryId()
            );

            BigDecimal remaining = financeService.getBudgetRemaining(
                    currentUser,
                    budget.getCategoryId()
            );

            System.out.println("----------------------------");
            System.out.println("Категория: " + category.getName());
            System.out.println("Лимит: " + budget.getLimit());
            System.out.println("Потрачено: " + spent);
            System.out.println("Остаток: " + remaining);

            if (spent.compareTo(budget.getLimit()) > 0) {
                System.out.println("ВНИМАНИЕ: Бюджет превышен!");
            } else if (spent.compareTo(
                    budget.getLimit().multiply(new BigDecimal("0.8"))
            ) >= 0) {
                System.out.println("ВНИМАНИЕ: Использовано 80% бюджета!");
            }
        }
    }


    private void updateBudget() {
        List<Budget> budgets = financeService.getBudgets(currentUser);

        if (budgets.isEmpty()) {
            System.out.println("Нет бюджетов для изменения.");
            return;
        }

        System.out.println("\n========== ИЗМЕНЕНИЕ БЮДЖЕТА ==========");

        List<Category> categories =
                financeService.getCategories(currentUser);

        for (int i = 0; i < budgets.size(); i++) {
            Budget budget = budgets.get(i);

            Category category = categories.stream()
                    .filter(item ->
                            item.getId().equals(budget.getCategoryId())
                    )
                    .findFirst()
                    .orElseThrow();

            System.out.printf(
                    "%d. %s — %s%n",
                    i + 1,
                    category.getName(),
                    budget.getLimit()
            );
        }

        System.out.print("Выберите бюджет: ");

        int index;

        try {
            index = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Введите номер числом.");
            return;
        }

        if (index < 0 || index >= budgets.size()) {
            System.out.println("Некорректный номер бюджета.");
            return;
        }

        Budget budget = budgets.get(index);

        System.out.print("Введите новый лимит: ");

        BigDecimal newLimit;

        try {
            newLimit = new BigDecimal(scanner.nextLine());

            if (newLimit.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("Лимит должен быть больше нуля.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Некорректная сумма.");
            return;
        }

        financeService.updateBudgetLimit(
                currentUser,
                budget.getCategoryId(),
                newLimit
        );

        System.out.println("Бюджет успешно обновлён!");
    }


    private void notifyBudgetStatus(Category category) {
        List<Budget> budgets = financeService.getBudgets(currentUser);

        Budget budget = budgets.stream()
                .filter(item ->
                        item.getCategoryId().equals(category.getId())
                )
                .findFirst()
                .orElse(null);

        if (budget == null) {
            return;
        }

        BigDecimal spent = financeService.getBudgetSpent(
                currentUser,
                category.getId()
        );

        BigDecimal limit = budget.getLimit();

        BigDecimal remaining = limit.subtract(spent);

        BigDecimal warningThreshold = limit.multiply(
                new BigDecimal("0.8")
        );

        System.out.println();

        if (spent.compareTo(limit) > 0) {
            System.out.println("!!! ВНИМАНИЕ: БЮДЖЕТ ПРЕВЫШЕН !!!");
            System.out.println("Категория: " + category.getName());
            System.out.println("Лимит: " + limit);
            System.out.println("Потрачено: " + spent);
            System.out.println("Превышение: " + remaining.abs());

        } else if (spent.compareTo(limit) == 0) {
            System.out.println("!!! БЮДЖЕТ ИЗРАСХОДОВАН !!!");
            System.out.println("Категория: " + category.getName());

        } else if (spent.compareTo(warningThreshold) >= 0) {
            System.out.println("!!! ВНИМАНИЕ: ИСПОЛЬЗОВАНО 80% БЮДЖЕТА !!!");
            System.out.println("Категория: " + category.getName());
            System.out.println("Потрачено: " + spent);
            System.out.println("Остаток: " + remaining);
        }
    }

    private void exportTransactionsToCsv() {
        List<Transaction> transactions =
                financeService.getTransactions(currentUser);

        if (transactions.isEmpty()) {
            System.out.println("Нет операций для экспорта");
            return;
        }

        List<Category> categories =
                financeService.getCategories(currentUser);

        StringBuilder csv = new StringBuilder();

        csv.append("Тип;Сумма;Категория;Описание;Дата\n");

        for (Transaction transaction : transactions) {
            String categoryName = categories.stream()
                    .filter(category ->
                            category.getId().equals(
                                    transaction.getCategoryId()
                            )
                    )
                    .map(Category::getName)
                    .findFirst()
                    .orElse("Неизвестная категория");

            csv.append(csvEscape(transaction.getType().name()))
                    .append(";")
                    .append(csvEscape(
                            transaction.getAmount().toPlainString()
                    ))
                    .append(";")
                    .append(csvEscape(categoryName))
                    .append(";")
                    .append(csvEscape(transaction.getDescription()))
                    .append(";")
                    .append(csvEscape(
                            transaction.getDate().toString()
                    ))
                    .append("\n");
        }

        String safeLogin = currentUser.getLogin()
                .replaceAll("[^a-zA-Z0-9_-]", "_");

        Path exportDirectory = Path.of("data", "exports");

        Path filePath = exportDirectory.resolve(
                safeLogin + "_transactions.csv"
        );

        try {
            Files.createDirectories(exportDirectory);

            Files.writeString(
                    filePath,
                    csv.toString(),
                    StandardCharsets.UTF_8
            );

            System.out.println("Экспорт успешно выполнен!");
            System.out.println("Файл: " + filePath.toAbsolutePath());

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Не удалось экспортировать операции",
                    exception
            );
        }
    }

    private String csvEscape(String value) {
        if (value == null) {
            return "";
        }

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private void importTransactionsFromCsv() {
        System.out.print("Введите путь к CSV-файлу: ");
        String fileName = scanner.nextLine().trim();

        if (fileName.isEmpty()) {
            throw new IllegalArgumentException("Путь к файлу не может быть пустым");
        }

        Path filePath = Path.of(fileName);

        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("Файл не найден: " + fileName);
        }

        try {
            List<String> lines = Files.readAllLines(
                    filePath,
                    StandardCharsets.UTF_8
            );

            if (lines.size() < 2) {
                throw new IllegalArgumentException(
                        "Файл не содержит операций"
                );
            }

            List<Category> categories =
                    financeService.getCategories(currentUser);

            int imported = 0;

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);

                if (line.isBlank()) {
                    continue;
                }

                List<String> fields = parseCsvLine(line);

                if (fields.size() != 5) {
                    throw new IllegalArgumentException(
                            "Некорректное количество полей в строке " + (i + 1)
                    );
                }

                TransactionType type;

                try {
                    type = TransactionType.valueOf(
                            fields.get(0).trim().toUpperCase()
                    );
                } catch (IllegalArgumentException exception) {
                    throw new IllegalArgumentException(
                            "Неизвестный тип операции в строке " + (i + 1)
                    );
                }

                BigDecimal amount;

                try {
                    amount = new BigDecimal(fields.get(1).trim());
                } catch (NumberFormatException exception) {
                    throw new IllegalArgumentException(
                            "Некорректная сумма в строке " + (i + 1)
                    );
                }

                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException(
                            "Сумма должна быть больше нуля. Строка: " + (i + 1)
                    );
                }

                String categoryName = fields.get(2).trim();
                String description = fields.get(3);

                LocalDateTime date;
                try {
                    date = LocalDateTime.parse(fields.get(4).trim());
                } catch (java.time.format.DateTimeParseException exception) {
                    throw new IllegalArgumentException(
                            "Некорректная дата в строке " + (i + 1)
                    );
                }

                Category category = categories.stream()
                        .filter(item ->
                                item.getName().equals(categoryName)
                                        && item.getType().name().equals(type.name())
                        )
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Категория не найдена: " + categoryName
                        ));

                financeService.importTransaction(
                        currentUser,
                        amount,
                        type,
                        category.getId(),
                        description,
                        date
                );
                imported++;
            }

            System.out.println(
                    "Импорт завершён. Добавлено операций: " + imported
            );

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Не удалось прочитать CSV-файл",
                    exception
            );
        }
    }

    private List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();

        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char current = line.charAt(i);

            if (current == '"') {
                if (insideQuotes
                        && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (current == ';' && !insideQuotes) {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(current);
            }
        }

        if (insideQuotes) {
            throw new IllegalArgumentException(
                    "Незакрытые кавычки в CSV-строке"
            );
        }

        fields.add(field.toString());

        return fields;
    }

    private void exportTransactionsToJson() {
        List<Transaction> transactions =
                financeService.getTransactions(currentUser);

        Path path = Path.of(
                "data",
                "exports",
                currentUser.getLogin() + "_transactions.json"
        );

        JsonStorage storage = new JsonStorage();
        storage.write(path, transactions);

        System.out.println("Экспорт JSON успешно завершён!");
        System.out.println("Файл: " + path);
    }


    private void importTransactionsFromJson() {
        Path path = Path.of(
                "data",
                "exports",
                currentUser.getLogin() + "_transactions.json"
        );

        if (!Files.exists(path)) {
            throw new IllegalArgumentException(
                    "JSON-файл не найден: " + path
            );
        }

        Type type = new TypeToken<List<Transaction>>() {
        }.getType();

        JsonStorage storage = new JsonStorage();

        List<Transaction> transactions = storage.<List<Transaction>>read(path, type)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Не удалось прочитать JSON-файл"
                ));

        if (transactions == null || transactions.isEmpty()) {
            throw new IllegalArgumentException(
                    "JSON-файл не содержит операций"
            );
        }

        int imported = 0;

        for (int i = 0; i < transactions.size(); i++) {
            Transaction transaction = transactions.get(i);

            if (transaction == null
                    || transaction.getAmount() == null
                    || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0
                    || transaction.getType() == null
                    || transaction.getCategoryId() == null
                    || transaction.getDate() == null) {
                throw new IllegalArgumentException(
                        "Некорректные данные операции в JSON, номер: " + (i + 1)
                );
            }

            financeService.importTransaction(
                    currentUser,
                    transaction.getAmount(),
                    transaction.getType(),
                    transaction.getCategoryId(),
                    transaction.getDescription(),
                    transaction.getDate()
            );

            imported++;
        }

        System.out.println("Импортировано операций: " + imported);
    }

}