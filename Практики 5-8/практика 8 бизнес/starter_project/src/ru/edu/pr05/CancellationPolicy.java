package ru.edu.pr05;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@FunctionalInterface
public interface CancellationPolicy {
checkDate(LocalDate today, LocalDate start);

    static CancellationPolicy minimumNotice(int days) {
        if (days < 0) {
            throw new IllegalArgumentException("Минимальный срок не может быть отрицательным");
        }
        return (today, start) -> {
            if (ChronoUnit.DAYS.between(today, start) < days) {
                throw new IllegalArgumentException("Для отмены до начала поездки должно оставаться не менее "
                        + days + " календарных дней");
            }
        };
    }
}
