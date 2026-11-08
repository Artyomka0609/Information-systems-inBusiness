package ru.edu.pr05;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

final class TripDates {
    private TripDates() {
    }

    static LocalDate parse(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Дата обязательна");
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Неверная календарная дата: " + value, ex);
        }
    }
}
