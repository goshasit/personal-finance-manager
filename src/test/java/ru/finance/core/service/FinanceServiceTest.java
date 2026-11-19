package ru.finance.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.finance.core.model.Category;
import ru.finance.core.model.CategoryType;
import ru.finance.core.model.User;
import ru.finance.core.model.Wallet;
import ru.finance.core.repository.WalletRepository;
import ru.finance.core.model.Transaction;
import ru.finance.core.model.Budget;
import ru.finance.core.model.TransactionType;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FinanceServiceTest {

    private FinanceService financeService;
    private InMemoryWalletRepository walletRepository;
    private User user;

    @BeforeEach
    void setUp() {
        walletRepository = new InMemoryWalletRepository();
        financeService = new FinanceService(walletRepository);
        user = new User("testuser", "passwordHash");
    }

    @Test
    void shouldCreateCategory() {
        Category category = financeService.createCategory(
                user, "Salary", CategoryType.INCOME
        );

        assertEquals("Salary", category.getName());
        assertEquals(1, financeService.getCategories(user).size());
    }

    @Test
    void shouldAddIncome() {
        Category category = financeService.createCategory(
                user, "Salary", CategoryType.INCOME
        );

        financeService.addIncome(
                user,
                new BigDecimal("50000"),
                category.getId(),
                "Monthly salary"
        );

        assertEquals(
                new BigDecimal("50000"),
                financeService.getTotalIncome(user)
        );
    }

    @Test
    void shouldAddExpense() {
        Category category = financeService.createCategory(
                user, "Food", CategoryType.EXPENSE
        );

        financeService.addExpense(
                user,
                new BigDecimal("1500"),
                category.getId(),
                "Groceries"
        );

        assertEquals(
                new BigDecimal("1500"),
                financeService.getTotalExpense(user)
        );
    }

    @Test
    void shouldCalculateBalance() {
        Category incomeCategory = financeService.createCategory(
                user, "Salary", CategoryType.INCOME
        );

        Category expenseCategory = financeService.createCategory(
                user, "Food", CategoryType.EXPENSE
        );

        financeService.addIncome(
                user,
                new BigDecimal("50000"),
                incomeCategory.getId(),
                "Salary"
        );

        financeService.addExpense(
                user,
                new BigDecimal("12000"),
                expenseCategory.getId(),
                "Food"
        );

        assertEquals(
                new BigDecimal("38000"),
                financeService.getBalance(user)
        );
    }

    @Test
    void shouldRejectIncomeWithExpenseCategory() {
        Category category = financeService.createCategory(
                user, "Food", CategoryType.EXPENSE
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> financeService.addIncome(
                        user,
                        new BigDecimal("1000"),
                        category.getId(),
                        "Invalid income"
                )
        );
    }

    @Test
    void shouldRejectNullUser() {
        assertThrows(
                IllegalArgumentException.class,
                () -> financeService.getBalance(null)
        );
    }

    private static class InMemoryWalletRepository
            implements WalletRepository {

        private final Map<UUID, Wallet> wallets = new HashMap<>();

        @Override
        public void save(UUID userId, Wallet wallet) {
            wallets.put(userId, wallet);
        }

        @Override
        public Optional<Wallet> findByUserId(UUID userId) {
            return Optional.ofNullable(wallets.get(userId));
        }
    }

    @Test
    void shouldUpdateTransaction() {
        Category category = financeService.createCategory(
                user, "Salary", CategoryType.INCOME
        );

        var transaction = financeService.addIncome(
                user,
                new BigDecimal("50000"),
                category.getId(),
                "Old description"
        );

        financeService.updateTransaction(
                user,
                transaction.getId(),
                new BigDecimal("60000"),
                category.getId(),
                "New description"
        );

        assertEquals(
                new BigDecimal("60000"),
                financeService.getTotalIncome(user)
        );

        assertEquals(
                "New description",
                financeService.getTransactions(user).get(0).getDescription()
        );
    }

    @Test
    void shouldDeleteTransaction() {
        Category category = financeService.createCategory(
                user, "Food", CategoryType.EXPENSE
        );

        var transaction = financeService.addExpense(
                user,
                new BigDecimal("1500"),
                category.getId(),
                "Groceries"
        );

        financeService.deleteTransaction(user, transaction.getId());

        assertEquals(0, financeService.getTransactions(user).size());
        assertEquals(
                BigDecimal.ZERO,
                financeService.getTotalExpense(user)
        );
    }

    @Test
    void shouldFilterTransactionsByPeriod() {
        Category category = financeService.createCategory(
                user, "Salary", CategoryType.INCOME
        );

        financeService.addIncome(
                user,
                new BigDecimal("50000"),
                category.getId(),
                "Salary"
        );

        List<Transaction> result =
                financeService.getTransactionsByPeriod(
                        user,
                        LocalDateTime.now().minusMinutes(1),
                        LocalDateTime.now().plusMinutes(1)
                );

        assertEquals(1, result.size());
    }

    @Test
    void shouldFilterTransactionsByCategories() {
        Category salary = financeService.createCategory(
                user, "Salary", CategoryType.INCOME
        );

        Category bonus = financeService.createCategory(
                user, "Bonus", CategoryType.INCOME
        );

        financeService.addIncome(
                user,
                new BigDecimal("50000"),
                salary.getId(),
                "Salary"
        );

        financeService.addIncome(
                user,
                new BigDecimal("10000"),
                bonus.getId(),
                "Bonus"
        );

        List<Transaction> result =
                financeService.getTransactionsByCategories(
                        user,
                        List.of(salary.getId())
                );

        assertEquals(1, result.size());
        assertEquals(
                salary.getId(),
                result.get(0).getCategoryId()
        );
    }

    @Test
    void shouldRejectInvalidDatePeriod() {
        assertThrows(
                IllegalArgumentException.class,
                () -> financeService.getTransactionsByPeriod(
                        user,
                        LocalDateTime.now(),
                        LocalDateTime.now().minusDays(1)
                )
        );
    }

    @Test
    void shouldCreateBudget() {
        Category category = financeService.createCategory(
                user, "Food", CategoryType.EXPENSE
        );

        Budget budget = financeService.createBudget(
                user,
                category.getId(),
                new BigDecimal("10000")
        );

        assertNotNull(budget.getId());
        assertEquals(category.getId(), budget.getCategoryId());
        assertEquals(new BigDecimal("10000"), budget.getLimit());

        assertEquals(1, financeService.getBudgets(user).size());
    }

    @Test
    void shouldRejectBudgetForIncomeCategory() {
        Category category = financeService.createCategory(
                user, "Salary", CategoryType.INCOME
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> financeService.createBudget(
                        user,
                        category.getId(),
                        new BigDecimal("10000")
                )
        );
    }

    @Test
    void shouldUpdateBudgetLimit() {
        Category category = financeService.createCategory(
                user, "Food", CategoryType.EXPENSE
        );

        financeService.createBudget(
                user,
                category.getId(),
                new BigDecimal("10000")
        );

        financeService.updateBudgetLimit(
                user,
                category.getId(),
                new BigDecimal("15000")
        );

        assertEquals(
                new BigDecimal("15000"),
                financeService.getBudgets(user).get(0).getLimit()
        );
    }

    @Test
    void shouldCalculateBudgetSpent() {
        Category category = financeService.createCategory(
                user, "Food", CategoryType.EXPENSE
        );

        financeService.createBudget(
                user,
                category.getId(),
                new BigDecimal("10000")
        );

        financeService.addExpense(
                user,
                new BigDecimal("2500"),
                category.getId(),
                "Groceries"
        );

        financeService.addExpense(
                user,
                new BigDecimal("1500"),
                category.getId(),
                "Lunch"
        );

        assertEquals(
                new BigDecimal("4000"),
                financeService.getBudgetSpent(user, category.getId())
        );
    }

    @Test
    void shouldCalculateBudgetRemaining() {
        Category category = financeService.createCategory(
                user, "Food", CategoryType.EXPENSE
        );

        financeService.createBudget(
                user,
                category.getId(),
                new BigDecimal("10000")
        );

        financeService.addExpense(
                user,
                new BigDecimal("3000"),
                category.getId(),
                "Groceries"
        );

        assertEquals(
                new BigDecimal("7000"),
                financeService.getBudgetRemaining(user, category.getId())
        );
    }

    @Test
    void shouldReturnNegativeRemainingWhenBudgetExceeded() {
        Category category = financeService.createCategory(
                user, "Food", CategoryType.EXPENSE
        );

        financeService.createBudget(
                user,
                category.getId(),
                new BigDecimal("5000")
        );

        financeService.addExpense(
                user,
                new BigDecimal("7000"),
                category.getId(),
                "Large purchase"
        );

        assertEquals(
                new BigDecimal("-2000"),
                financeService.getBudgetRemaining(user, category.getId())
        );
    }

    @Test
    void shouldRejectZeroBudgetLimit() {
        Category category = financeService.createCategory(
                user, "Food", CategoryType.EXPENSE
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> financeService.createBudget(
                        user,
                        category.getId(),
                        BigDecimal.ZERO
                )
        );
    }

    @Test
    void shouldImportTransactionWithOriginalDate() {
        Category category = financeService.createCategory(
                user, "Food", CategoryType.EXPENSE
        );

        LocalDateTime originalDate = LocalDateTime.of(
                2025, 5, 10, 14, 30
        );

        Transaction transaction = financeService.importTransaction(
                user,
                new BigDecimal("1500"),
                TransactionType.EXPENSE,
                category.getId(),
                "Imported groceries",
                originalDate
        );

        assertEquals(originalDate, transaction.getDate());
        assertEquals(
                new BigDecimal("1500"),
                transaction.getAmount()
        );
        assertEquals(
                "Imported groceries",
                transaction.getDescription()
        );
    }

    @Test
    void shouldRejectImportedTransactionWithInvalidAmount() {
        Category category = financeService.createCategory(
                user, "Food", CategoryType.EXPENSE
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> financeService.importTransaction(
                        user,
                        BigDecimal.ZERO,
                        TransactionType.EXPENSE,
                        category.getId(),
                        "Invalid transaction",
                        LocalDateTime.now()
                )
        );
    }

    @Test
    void shouldRejectImportedTransactionWithWrongCategoryType() {
        Category category = financeService.createCategory(
                user, "Salary", CategoryType.INCOME
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> financeService.importTransaction(
                        user,
                        new BigDecimal("1000"),
                        TransactionType.EXPENSE,
                        category.getId(),
                        "Wrong category",
                        LocalDateTime.now()
                )
        );
    }

    @Test
    void shouldRejectImportedTransactionWithUnknownCategory() {
        assertThrows(
                IllegalArgumentException.class,
                () -> financeService.importTransaction(
                        user,
                        new BigDecimal("1000"),
                        TransactionType.EXPENSE,
                        UUID.randomUUID(),
                        "Unknown category",
                        LocalDateTime.now()
                )
        );
    }
}
