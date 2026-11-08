package ru.edu.pr04;

import java.util.List;

public final class OrderService {
    public static final double MAX_TOTAL = MaximumOrderTotal.VARIANT_8_LIMIT;
    private final List<OrderAmountRule> additionalRules;

    public OrderService() {
        this(List.of());
    }

    public OrderService(List<OrderAmountRule> additionalRules) {
        this.additionalRules = List.copyOf(additionalRules);
    }

    public Order createOrder(Employee employee) {
        return new Order(employee, additionalRules);
    }

    public static void validateForConfirm(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Заказ не задан");
        }
        order.validateForConfirm();
    }

    static void addRow(Order order, String row) {
        OrderItem item = OrderRowParser.parse(row);
        order.addProduct(item.getProduct(), item.getQuantity());
    }


    public static void main(String[] args) {
        OrderConsole.main(args);
    }
}
