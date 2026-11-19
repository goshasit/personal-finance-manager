package ru.finance.core.service;

import ru.finance.core.model.Category;
import ru.finance.core.model.CategoryType;
import ru.finance.core.model.Transaction;
import ru.finance.core.model.TransactionType;
import ru.finance.core.model.User;
import ru.finance.core.model.Wallet;
import ru.finance.core.repository.WalletRepository;
import ru.finance.core.model.Budget;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.time.YearMonth;


public class FinanceService {

    private final WalletRepository walletRepository;

    public FinanceService(WalletRepository walletRepository) {
        if (walletRepository == null) {
            throw new IllegalArgumentException(
                    "Wallet repository cannot be null"
            );
        }
        this.walletRepository = walletRepository;
    }

    public Category createCategory(
            User user,
            String name,
            CategoryType type
    ) {
        Wallet wallet = getWallet(user);

        Category category = new Category(name, type);
        wallet.addCategory(category);

        saveWallet(user);
        return category;
    }

    public void updateCategory(User user, UUID categoryId, String newName) {
        getWallet(user).updateCategory(categoryId, newName);
        saveWallet(user);
    }

    public void deleteCategory(User user, UUID categoryId) {
        getWallet(user).deleteCategory(categoryId);
        saveWallet(user);
    }

    public Transaction addIncome(
            User user,
            BigDecimal amount,
            UUID categoryId,
            String description
    ) {
        return addTransaction(
                user, amount, TransactionType.INCOME,
                categoryId, description
        );
    }

    public Transaction addExpense(
            User user,
            BigDecimal amount,
            UUID categoryId,
            String description
    ) {
        return addTransaction(
                user, amount, TransactionType.EXPENSE,
                categoryId, description
        );
    }

    private Transaction addTransaction(
            User user,
            BigDecimal amount,
            TransactionType type,
            UUID categoryId,
            String description
    ) {
        Wallet wallet = getWallet(user);

        Transaction transaction = new Transaction(
                amount, type, categoryId, description
        );

        wallet.addTransaction(transaction);
        saveWallet(user);

        return transaction;
    }

    public BigDecimal getBalance(User user) {
        return getWallet(user).getBalance();
    }

    public BigDecimal getTotalIncome(User user) {
        return getWallet(user).getTotalIncome();
    }

    public BigDecimal getTotalExpense(User user) {
        return getWallet(user).getTotalExpense();
    }

    public List<Category> getCategories(User user) {
        return getWallet(user).getCategories();
    }

    public List<Transaction> getTransactions(User user) {
        return getWallet(user).getTransactions();
    }

    public void updateTransaction(
            User user,
            UUID transactionId,
            BigDecimal amount,
            UUID categoryId,
            String description
    ) {
        Wallet wallet = getWallet(user);

        wallet.updateTransaction(
                transactionId,
                amount,
                categoryId,
                description
        );

        saveWallet(user);
    }

    public void deleteTransaction(User user, UUID transactionId) {
        Wallet wallet = getWallet(user);

        wallet.deleteTransaction(transactionId);

        saveWallet(user);
    }

    public List<Transaction> getTransactionsByPeriod(
            User user,
            LocalDateTime from,
            LocalDateTime to
    ) {
        if (from == null || to == null || from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "Invalid date period"
            );
        }

        return getWallet(user).getTransactions().stream()
                .filter(transaction ->
                        !transaction.getDate().isBefore(from)
                                && !transaction.getDate().isAfter(to)
                )
                .toList();
    }

    public List<Transaction> getTransactionsByCategories(
            User user,
            List<UUID> categoryIds
    ) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Category IDs cannot be empty"
            );
        }

        return getWallet(user).getTransactions().stream()
                .filter(transaction ->
                        categoryIds.contains(transaction.getCategoryId())
                )
                .toList();
    }

    public BigDecimal getCategoryTotal(User user, UUID categoryId) {
        return getWallet(user).getTransactions().stream()
                .filter(t -> t.getCategoryId().equals(categoryId))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Budget createBudget(
            User user,
            UUID categoryId,
            BigDecimal limit
    ) {
        Wallet wallet = getWallet(user);

        Budget budget = new Budget(categoryId, limit);

        wallet.addBudget(budget);
        saveWallet(user);

        return budget;
    }

    public void updateBudgetLimit(
            User user,
            UUID categoryId,
            BigDecimal newLimit
    ) {
        Wallet wallet = getWallet(user);

        wallet.updateBudgetLimit(categoryId, newLimit);

        saveWallet(user);
    }

    public List<Budget> getBudgets(User user) {
        return getWallet(user).getBudgets();
    }

    public BigDecimal getBudgetSpent(
            User user,
            UUID categoryId
    ) {
        YearMonth currentMonth = YearMonth.now();

        return getWallet(user).getTransactions().stream()
                .filter(transaction ->
                        transaction.getType() == TransactionType.EXPENSE
                )
                .filter(transaction ->
                        transaction.getCategoryId().equals(categoryId)
                )
                .filter(transaction ->
                        YearMonth.from(transaction.getDate())
                                .equals(currentMonth)
                )
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getBudgetRemaining(
            User user,
            UUID categoryId
    ) {
        Wallet wallet = getWallet(user);

        Budget budget = wallet.getBudgets().stream()
                .filter(item ->
                        item.getCategoryId().equals(categoryId)
                )
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Budget not found"
                ));

        return budget.getLimit().subtract(
                getBudgetSpent(user, categoryId)
        );
    }


    public Transaction importTransaction(
            User user,
            BigDecimal amount,
            TransactionType type,
            UUID categoryId,
            String description,
            LocalDateTime date
    ) {
        Wallet wallet = getWallet(user);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        if (type == null || categoryId == null || date == null) {
            throw new IllegalArgumentException("Transaction data is incomplete");
        }

        Category category = wallet.getCategories().stream()
                .filter(item -> item.getId().equals(categoryId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Category does not exist: " + categoryId
                ));

        if (!category.getType().name().equals(type.name())) {
            throw new IllegalArgumentException(
                    "Category type does not match transaction type"
            );
        }

        Transaction transaction = new Transaction(
                amount,
                type,
                categoryId,
                description,
                date
        );

        wallet.addTransaction(transaction);
        saveWallet(user);

        return transaction;
    }


    private Wallet getWallet(User user) {
        if (user == null) {
            throw new IllegalArgumentException(
                    "User cannot be null"
            );
        }
        return user.getWallet();
    }

    private void saveWallet(User user) {
        walletRepository.save(user.getId(), user.getWallet());
    }
}
