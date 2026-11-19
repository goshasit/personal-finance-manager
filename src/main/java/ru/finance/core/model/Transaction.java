
package ru.finance.core.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Transaction {

    private final UUID id;
    private BigDecimal amount;
    private final TransactionType type;
    private UUID categoryId;
    private String description;
    private final LocalDateTime date;

    // Обычное создание операции с текущей датой
    public Transaction(
            BigDecimal amount,
            TransactionType type,
            UUID categoryId,
            String description
    ) {
        this(
                amount,
                type,
                categoryId,
                description,
                LocalDateTime.now()
        );
    }

    // Создание операции с указанной датой (например, при импорте CSV)
    public Transaction(
            BigDecimal amount,
            TransactionType type,
            UUID categoryId,
            String description,
            LocalDateTime date
    ) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Transaction amount must be greater than zero"
            );
        }

        this.id = UUID.randomUUID();
        this.amount = amount;
        this.type = Objects.requireNonNull(
                type,
                "Transaction type cannot be null"
        );
        this.categoryId = Objects.requireNonNull(
                categoryId,
                "Category ID cannot be null"
        );
        this.description = description == null
                ? ""
                : description.trim();
        this.date = Objects.requireNonNull(
                date,
                "Transaction date cannot be null"
        );
    }

    public UUID getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void updateDetails(
            BigDecimal amount,
            UUID categoryId,
            String description
    ) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero"
            );
        }

        if (categoryId == null) {
            throw new IllegalArgumentException(
                    "Category ID cannot be null"
            );
        }

        this.amount = amount;
        this.categoryId = categoryId;
        this.description = description == null
                ? ""
                : description.trim();
    }
}
