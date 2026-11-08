package ru.edu.pr04;

import java.math.BigDecimal;

public final class Product {
    private final String id;
    private final String name;
    private final BigDecimal price;

    public Product(String id, String name, double price) {
        this(id, name, decimalPrice(price));
    }

    public Product(String id, String name, BigDecimal price) {
        if (id == null || id.isBlank() || name == null || name.isBlank()) {
            throw new IllegalArgumentException("Код и название товара обязательны");
        }
        if (price == null || price.doubleValue() <= 0 || !Double.isFinite(price.doubleValue())) {
            throw new IllegalArgumentException("Цена должна быть положительным конечным числом в диапазоне double");
        }
        this.id = id.strip();
        this.name = name.strip();
        this.price = price;
    }

    private static BigDecimal decimalPrice(double price) {
        if (!Double.isFinite(price)) {
            throw new IllegalArgumentException("Цена должна быть конечным числом");
        }
        return BigDecimal.valueOf(price);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price.doubleValue(); }
    public BigDecimal getExactPrice() { return price; }
}
