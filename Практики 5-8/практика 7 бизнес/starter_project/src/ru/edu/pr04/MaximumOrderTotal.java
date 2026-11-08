package ru.edu.pr04;

import java.math.BigDecimal;

public final class MaximumOrderTotal implements OrderAmountRule {
    public static final double VARIANT_8_LIMIT = 45.0;
    static final MaximumOrderTotal VARIANT_8 = new MaximumOrderTotal(BigDecimal.valueOf(VARIANT_8_LIMIT));

    private final BigDecimal limit;

    public MaximumOrderTotal(BigDecimal limit) {
        if (limit == null || limit.signum() <= 0) {
            throw new IllegalArgumentException("Лимит должен быть положительным");
        }
        this.limit = limit;
    }

    @Override
    public void validate(BigDecimal total) {
        if (total.compareTo(limit) > 0) {
            throw new IllegalStateException("Сумма заказа превышает лимит " + limit);
        }
    }
}
