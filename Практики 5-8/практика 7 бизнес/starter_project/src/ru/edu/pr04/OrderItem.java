package ru.edu.pr04;

import java.math.BigDecimal;

public final class OrderItem {
    public static final int MAX_QUANTITY = 100;
    private final Product product;
    private int quantity;

    public OrderItem(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Товар не задан");
        }
        this.product = product;
        setQuantity(quantity);
    }

    private void setQuantity(int quantity) {
        if (quantity < 1 || quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException("Количество должно быть от 1 до " + MAX_QUANTITY);
        }
        this.quantity = quantity;
    }

    public void increase(int delta) {
        if (delta < 1 || delta > MAX_QUANTITY - quantity) {
            throw new IllegalArgumentException("Суммарное количество должно быть от 1 до " + MAX_QUANTITY);
        }
        setQuantity(quantity + delta);
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }

    public double getLineTotal() {
        return getExactLineTotal().doubleValue();
    }

    public BigDecimal getExactLineTotal() {
        return product.getExactPrice().multiply(BigDecimal.valueOf(quantity));
    }
}
