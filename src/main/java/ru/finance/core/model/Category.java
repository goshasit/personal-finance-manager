
package ru.finance.core.model;

import java.util.Objects;
import java.util.UUID;

public class Category {

    private final UUID id;
    private String name;
    private final CategoryType type;

    public Category(String name, CategoryType type) {
        this.id = UUID.randomUUID();
        setName(name);
        this.type = Objects.requireNonNull(type, "Category type cannot be null");
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CategoryType getType() {
        return type;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }

        this.name = name.trim();
    }
}
