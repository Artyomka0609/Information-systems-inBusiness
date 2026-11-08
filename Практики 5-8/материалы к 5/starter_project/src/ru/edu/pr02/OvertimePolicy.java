package ru.edu.pr02;


public interface OvertimePolicy {
    boolean isOvertimeDay(int dailyHours);

    int weeklyOvertime(int totalHours);
}
