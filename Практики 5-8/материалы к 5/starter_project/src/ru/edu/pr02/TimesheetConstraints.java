package ru.edu.pr02;

/** Ограничения, которым должен соответствовать любой создаваемый табель. */
public final class TimesheetConstraints {
    private final int dayCount;
    private final int maxHours;

    public TimesheetConstraints(int dayCount, int maxHours) {
        if (dayCount <= 0 || maxHours < 0) {
            throw new IllegalArgumentException("Число дней должно быть положительным, максимум часов — неотрицательным.");
        }
        if ((long) dayCount * maxHours > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Сумма допустимых часов должна помещаться в int.");
        }
        this.dayCount = dayCount;
        this.maxHours = maxHours;
    }

    public int getDayCount() {
        return dayCount;
    }

    public int getMaxHours() {
        return maxHours;
    }

    public void validateLength(int length) {
        if (length != dayCount) {
            long position = length < dayCount ? (long) length + 1 : (long) dayCount + 1;
            throw new IllegalArgumentException("Позиция " + position + ": ожидалось ровно "
                    + dayCount + " значений, получено " + length + ".");
        }
    }

    public void validateHour(int value, int position) {
        if (value < 0 || value > maxHours) {
            throw new IllegalArgumentException("Позиция " + position + ": часы должны быть от 0 до "
                    + maxHours + ", получено " + value + ".");
        }
    }
}
