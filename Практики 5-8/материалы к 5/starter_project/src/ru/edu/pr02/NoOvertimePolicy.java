package ru.edu.pr02;


public final class NoOvertimePolicy implements OvertimePolicy {
    @Override
    public boolean isOvertimeDay(int dailyHours) {
        validateNonNegative(dailyHours);
        return false;
    }

    @Override
    public int weeklyOvertime(int totalHours) {
        validateNonNegative(totalHours);
        return 0;
    }

    private void validateNonNegative(int hours) {
        if (hours < 0) {
            throw new IllegalArgumentException("Количество часов не может быть отрицательным.");
        }
    }
}
