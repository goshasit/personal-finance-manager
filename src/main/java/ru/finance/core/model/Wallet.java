
package ru.finance.core.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Wallet {

    private List<Transaction> transactions;
    private List<Category> categories;
    private List<Budget> budgets;

    public Wallet() {
        this.transactions = new ArrayList<>();
        this.categories = new ArrayList<>();
        this.budgets = new ArrayList<>();
    }

    public void addCategory(Category category) {
        Objects.requireNonNull(category, "Category cannot be null");

        boolean exists = categories.stream()
                .anyMatch(existing ->
                        existing.getType() == category.getType()
                                && existing.getName().equalsIgnoreCase(
                                category.getName()
                        )
                );

        if (exists) {
            throw new IllegalArgumentException(
                    "Category already exists: " + category.getName()
            );
        }

        categories.add(category);
    }

    public void addTransaction(Transaction transaction) {
        Objects.requireNonNull(transaction, "Transaction cannot be null");

        Category category = findCategoryById(transaction.getCategoryId());

        if (category.getType().name().equals(transaction.getType().name())) {
            transactions.add(transaction);
        } else {
            throw new IllegalArgumentException(
                    "Transaction type does not match category type"
            );
        }
    }

    public void addBudget(Budget budget) {
        Objects.requireNonNull(budget, "Budget cannot be null");

        Category category = findCategoryById(budget.getCategoryId());

        if (category.getType() != CategoryType.EXPENSE) {
            throw new IllegalArgumentException(
                    "Budget can only be assigned to expense categories"
            );
        }

        boolean exists = budgets.stream()
                .anyMatch(existing ->
                        existing.getCategoryId().equals(budget.getCategoryId())
                );

        if (exists) {
            throw new IllegalArgumentException(
                    "Budget already exists for this category"
            );
        }

        budgets.add(budget);
    }

    public Category findCategoryById(UUID categoryId) {
        return categories.stream()
                .filter(category -> category.getId().equals(categoryId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Category not found: " + categoryId
                ));
    }

    public BigDecimal getTotalIncome() {
        return transactions.stream()
                .filter(transaction ->
                        transaction.getType() == TransactionType.INCOME
                )
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalExpense() {
        return transactions.stream()
                .filter(transaction ->
                        transaction.getType() == TransactionType.EXPENSE
                )
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getBalance() {
        return getTotalIncome().subtract(getTotalExpense());
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public List<Category> getCategories() {
        return Collections.unmodifiableList(categories);
    }

    public List<Budget> getBudgets() {
        return Collections.unmodifiableList(budgets);
    }

    public void updateTransaction(
            UUID transactionId,
            BigDecimal amount,
            UUID categoryId,
            String description
    ) {
        Transaction transaction = transactions.stream()
                .filter(item -> item.getId().equals(transactionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transaction not found"
                ));

        Category category = findCategoryById(categoryId);

        if (!category.getType().name().equals(
                transaction.getType().name()
        )) {
            throw new IllegalArgumentException(
                    "Category type does not match transaction type"
            );
        }

        transaction.updateDetails(amount, categoryId, description);
    }

    public void deleteTransaction(UUID transactionId) {
        boolean removed = transactions.removeIf(
                transaction -> transaction.getId().equals(transactionId)
        );

        if (!removed) {
            throw new IllegalArgumentException(
                    "Transaction not found"
            );
        }
    }

    public void updateCategory(UUID categoryId, String newName) {
        Category category = findCategoryById(categoryId);

        boolean exists = categories.stream()
                .anyMatch(item ->
                        !item.getId().equals(categoryId)
                                && item.getType() == category.getType()
                                && item.getName().equalsIgnoreCase(newName.trim())
                );

        if (exists) {
            throw new IllegalArgumentException("Category already exists");
        }

        category.setName(newName);
    }

    public void deleteCategory(UUID categoryId) {
        boolean hasTransactions = transactions.stream()
                .anyMatch(t -> t.getCategoryId().equals(categoryId));

        boolean hasBudget = budgets.stream()
                .anyMatch(b -> b.getCategoryId().equals(categoryId));

        if (hasTransactions || hasBudget) {
            throw new IllegalArgumentException(
                    "Cannot delete category with transactions or budget"
            );
        }

        categories.removeIf(c -> c.getId().equals(categoryId));
    }

    public void updateBudgetLimit(UUID categoryId, BigDecimal newLimit) {
        Budget budget = budgets.stream()
                .filter(item -> item.getCategoryId().equals(categoryId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Budget not found for category: " + categoryId
                ));

        budget.setLimit(newLimit);
    }
}
