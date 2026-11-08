package ru.edu.pr02;

/** Переработка относительно заданных дневной и недельной норм. */
public final class NormOvertimePolicy implements OvertimePolicy {
    private final int dailyNorm;
    private final int weeklyNorm;

    public NormOvertimePolicy(int dailyNorm, int weeklyNorm) {
        if (dailyNorm < 0 || weeklyNorm < 0) {
            throw new IllegalArgumentException("Нормы часов не могут быть отрицательными.");
        }
        this.dailyNorm = dailyNorm;
        this.weeklyNorm = weeklyNorm;
    }

    @Override
    public boolean isOvertimeDay(int dailyHours) {
        validateNonNegative(dailyHours);
        return dailyHours > dailyNorm;
    }

    @Override
    public int weeklyOvertime(int totalHours) {
        validateNonNegative(totalHours);
        return Math.max(0, totalHours - weeklyNorm);
    }

    private void validateNonNegative(int hours) {
        if (hours < 0) {
            throw new IllegalArgumentException("Количество часов не может быть отрицательным.");
        }
    }
}
