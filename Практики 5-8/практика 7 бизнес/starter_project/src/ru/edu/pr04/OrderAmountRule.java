package ru.edu.pr04;

import java.math.BigDecimal;

@FunctionalInterface
public interface OrderAmountRule {
    void validate(BigDecimal total);
}
