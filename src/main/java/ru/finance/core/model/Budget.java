
package ru.finance.core.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class Budget {

    private final UUID id;
    private final UUID categoryId;
    private BigDecimal limit;

    public Budget(UUID categoryId, BigDecimal limit) {
        this.id = UUID.randomUUID();
        this.categoryId = Objects.requireNonNull(
                categoryId, "Category ID cannot be null"
        );
        setLimit(limit);
    }

    public UUID getId() {
        return id;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public BigDecimal getLimit() {
        return limit;
    }

    public void setLimit(BigDecimal limit) {
        if (limit == null || limit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Budget limit must be greater than zero"
            );
        }

        this.limit = limit;
    }
}
