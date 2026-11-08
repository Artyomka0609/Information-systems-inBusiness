package ru.edu.pr02;

import java.util.Objects;

/** Преобразует строку с разделителем ';' в корректный объект табеля. */
public final class TimesheetParser {
    private final TimesheetConstraints constraints;

    public TimesheetParser(TimesheetConstraints constraints) {
        this.constraints = Objects.requireNonNull(constraints, "Ограничения табеля не заданы.");
    }

    public Timesheet parse(String line) {
        if (line == null || line.trim().isEmpty()) {
            throw new IllegalArgumentException("Позиция 1: строка табеля отсутствует или пуста.");
        }
        String[] parts = line.split(";", -1);
        constraints.validateLength(parts.length);
        int[] hours = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            String value = parts[i].trim();
            if (value.isEmpty()) {
                throw new IllegalArgumentException("Позиция " + (i + 1) + ": значение пустое.");
            }
            try {
                hours[i] = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Позиция " + (i + 1)
                        + ": ожидается целое число в диапазоне int, получено «" + value + "».", e);
            }
            // Проверяем сразу, чтобы сообщать о первой ошибочной позиции.
            constraints.validateHour(hours[i], i + 1);
        }
        return new Timesheet(hours, constraints);
    }
}
