
package ru.finance.core.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class WalletTest {

    private Wallet wallet;
    private Category incomeCategory;
    private Category expenseCategory;

    @BeforeEach
    void setUp() {
        wallet = new Wallet();

        incomeCategory = new Category("Зарплата", CategoryType.INCOME);
        expenseCategory = new Category("Еда", CategoryType.EXPENSE);

        wallet.addCategory(incomeCategory);
        wallet.addCategory(expenseCategory);
    }

    @Test
    void shouldCalculateBalanceCorrectly() {
        wallet.addTransaction(new Transaction(
                new BigDecimal("10000"),
                TransactionType.INCOME,
                incomeCategory.getId(),
                "Зарплата"
        ));

        wallet.addTransaction(new Transaction(
                new BigDecimal("2000"),
                TransactionType.EXPENSE,
                expenseCategory.getId(),
                "Покупки"
        ));

        assertEquals(
                0,
                new BigDecimal("8000").compareTo(wallet.getBalance())
        );
    }

    @Test
    void shouldCalculateTotalIncome() {
        wallet.addTransaction(new Transaction(
                new BigDecimal("5000"),
                TransactionType.INCOME,
                incomeCategory.getId(),
                "Первая зарплата"
        ));

        wallet.addTransaction(new Transaction(
                new BigDecimal("3000"),
                TransactionType.INCOME,
                incomeCategory.getId(),
                "Премия"
        ));

        assertEquals(
                0,
                new BigDecimal("8000").compareTo(wallet.getTotalIncome())
        );
    }

    @Test
    void shouldCalculateTotalExpense() {
        wallet.addTransaction(new Transaction(
                new BigDecimal("1500"),
                TransactionType.EXPENSE,
                expenseCategory.getId(),
                "Продукты"
        ));

        assertEquals(
                0,
                new BigDecimal("1500").compareTo(wallet.getTotalExpense())
        );
    }

    @Test
    void shouldNotAllowDuplicateCategories() {
        Category duplicate = new Category(
                "Еда",
                CategoryType.EXPENSE
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> wallet.addCategory(duplicate)
        );
    }

    @Test
    void shouldNotAllowIncomeInExpenseCategory() {
        Transaction transaction = new Transaction(
                new BigDecimal("1000"),
                TransactionType.INCOME,
                expenseCategory.getId(),
                "Некорректная операция"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> wallet.addTransaction(transaction)
        );
    }

    @Test
    void shouldNotAllowBudgetForIncomeCategory() {
        Budget budget = new Budget(
                incomeCategory.getId(),
                new BigDecimal("5000")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> wallet.addBudget(budget)
        );
    }
}
