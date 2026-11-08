package ru.edu.pr04;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class Order {
    private final Employee employee;
    private final List<OrderAmountRule> amountRules;
    private final List<OrderItem> items = new ArrayList<>();
    private OrderStatus status = OrderStatus.DRAFT;

    public Order(Employee employee) {
        this(employee, List.of());
    }

    Order(Employee employee, List<OrderAmountRule> additionalRules) {
        if (employee == null) {
            throw new IllegalArgumentException("Сотрудник не задан");
        }
        this.employee = employee;
        List<OrderAmountRule> rules = new ArrayList<>();
        rules.add(MaximumOrderTotal.VARIANT_8);
        rules.addAll(additionalRules);
        this.amountRules = List.copyOf(rules);
    }

    public void addProduct(Product product, int quantity) {
        requireDraft();
        OrderItem candidate = new OrderItem(product, quantity);
        for (OrderItem item : items) {
            Product existing = item.getProduct();
            if (existing.getId().equals(product.getId())) {
                if (!existing.getName().equals(product.getName())
                        || existing.getExactPrice().compareTo(product.getExactPrice()) != 0) {
                    throw new IllegalArgumentException("Одинаковый id должен обозначать товар с одинаковыми названием и ценой");
                }
                item.increase(quantity);
                return;
            }
        }
        items.add(candidate);
    }

    public void removeProduct(String productId) {
        requireDraft();
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Код удаляемого товара обязателен");
        }
        if (!items.removeIf(item -> item.getProduct().getId().equals(productId.strip()))) {
            throw new IllegalArgumentException("Товар с таким кодом отсутствует в заказе");
        }
    }

    public double total() {
        return exactTotal().doubleValue();
    }

    public BigDecimal exactTotal() {
        BigDecimal result = BigDecimal.ZERO;
        for (OrderItem item : items) {
            result = result.add(item.getExactLineTotal());
        }
        return result;
    }

    public void confirm() {
        validateForConfirm();
        status = OrderStatus.CONFIRMED;
    }

    void validateForConfirm() {
        requireDraft();
        if (items.isEmpty()) {
            throw new IllegalStateException("Пустой заказ подтвердить нельзя");
        }
        BigDecimal total = exactTotal();
        for (OrderAmountRule rule : amountRules) {
            rule.validate(total);
        }
    }

    private void requireDraft() {
        if (status != OrderStatus.DRAFT) {
            throw new IllegalStateException("Подтверждённый заказ изменять нельзя");
        }
    }

    public List<OrderItem> getItems() {
        // Копируем и список, и изменяемые позиции, чтобы не раскрывать состояние заказа.
        List<OrderItem> snapshot = new ArrayList<>();
        for (OrderItem item : items) {
            snapshot.add(new OrderItem(item.getProduct(), item.getQuantity()));
        }
        return List.copyOf(snapshot);
    }

    public Employee getEmployee() { return employee; }
    public OrderStatus getStatus() { return status; }
}
