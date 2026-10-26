package ru.edu.pr02;

public class TimesheetAnalyzer {
    public static final int DAYS = 5;
    public static final int MAX_HOURS = 45; // Вариант 8; базовое ограничение было 16.
    public static final int DAILY_NORM = 8;
    public static final int WEEKLY_NORM = 40;

    public static int[] parseHours(String line) {
        if (line == null || line.trim().isEmpty()) {
            throw new IllegalArgumentException("Позиция 1: строка табеля отсутствует или пуста.");
        }
        // -1 сохраняет пустые поля, в том числе после последнего разделителя.
        String[] parts = line.split(";", -1);
        if (parts.length != DAYS) {
            int position = parts.length < DAYS ? parts.length + 1 : DAYS + 1;
            throw new IllegalArgumentException("Позиция " + position
                    + ": ожидалось ровно " + DAYS + " значений, получено " + parts.length + ".");
        }
        int[] hours = new int[DAYS];
        for (int i = 0; i < parts.length; i++) {
            String value = parts[i].trim();
            if (value.isEmpty()) {
                throw new IllegalArgumentException("Позиция " + (i + 1) + ": значение пустое.");
            }
            try {
                hours[i] = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Позиция " + (i + 1)
                        + ": ожидается целое число в диапазоне int, получено «" + value + "».");
            }
            checkHour(hours[i], i + 1);
        }
        return hours;
    }
    public static int total(int[] hours) {
        validateHours(hours);
        int sum = 0;
        for (int i = 0; i < hours.length; i++) {
            sum += hours[i];
        }
        return sum;
    }
    public static double average(int[] hours) {
        // Приведение до деления сохраняет дробную часть результата.
        return (double) total(hours) / hours.length;
    }
    public static int overtimeDays(int[] hours) {
        validateHours(hours);
        int count = 0;
        int i = 0;
        while (i < hours.length) {
            if (hours[i] > DAILY_NORM) {
                count++;
            }
            i++;
        }
        return count;
    }
    public static int weeklyOvertime(int[] hours) {
        return Math.max(0, total(hours) - WEEKLY_NORM);
    }

    private static void validateHours(int[] hours) {
        if (hours == null || hours.length != DAYS) {
            throw new IllegalArgumentException("Массив должен содержать ровно " + DAYS + " значений.");
        }
        for (int i = 0; i < hours.length; i++) {
            checkHour(hours[i], i + 1);
        }
    }

    private static void checkHour(int value, int position) {
        if (value < 0 || value > MAX_HOURS) {
            throw new IllegalArgumentException("Позиция " + position + ": часы должны быть от 0 до "
                    + MAX_HOURS + ", получено " + value + ".");
        }
    }
}
