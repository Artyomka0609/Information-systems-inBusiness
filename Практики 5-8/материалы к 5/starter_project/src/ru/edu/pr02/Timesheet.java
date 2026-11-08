package ru.edu.pr02;

import java.util.Objects;

/** Неизменяемый табель: после создания содержит только допустимые часы. */
public final class Timesheet {
    private final int[] hours;

    public Timesheet(int[] hours, TimesheetConstraints constraints) {
        Objects.requireNonNull(constraints, "Ограничения табеля не заданы.");
        if (hours == null) {
            throw new IllegalArgumentException("Позиция 1: массив часов отсутствует.");
        }
        // Копируем до проверки, чтобы проверять именно собственное состояние.
        int[] copy = hours.clone();
        constraints.validateLength(copy.length);
        for (int i = 0; i < copy.length; i++) {
            constraints.validateHour(copy[i], i + 1);
        }
        this.hours = copy;
    }

    public int getDayCount() {
        return hours.length;
    }

    /** Индекс начинается с нуля. Внутренний массив наружу не передаётся. */
    public int getHours(int dayIndex) {
        return hours[dayIndex];
    }
}
