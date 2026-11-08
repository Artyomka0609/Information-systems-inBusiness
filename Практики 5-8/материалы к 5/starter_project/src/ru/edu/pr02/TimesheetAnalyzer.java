package ru.edu.pr02;

import java.util.Objects;

/** Считает показатели готового табеля, используя переданное правило переработки. */
public final class TimesheetAnalyzer {
    private final OvertimePolicy overtimePolicy;

    public TimesheetAnalyzer(OvertimePolicy overtimePolicy) {
        this.overtimePolicy = Objects.requireNonNull(overtimePolicy, "Правило переработки не задано.");
    }

    public int total(Timesheet timesheet) {
        Objects.requireNonNull(timesheet, "Табель не задан.");
        int sum = 0;
        for (int i = 0; i < timesheet.getDayCount(); i++) {
            sum += timesheet.getHours(i);
        }
        return sum;
    }

    public double average(Timesheet timesheet) {
        return (double) total(timesheet) / timesheet.getDayCount();
    }

    public int overtimeDays(Timesheet timesheet) {
        Objects.requireNonNull(timesheet, "Табель не задан.");
        int count = 0;
        int i = 0;
        while (i < timesheet.getDayCount()) {
            if (overtimePolicy.isOvertimeDay(timesheet.getHours(i))) {
                count++;
            }
            i++;
        }
        return count;
    }

    public int weeklyOvertime(Timesheet timesheet) {
        return overtimePolicy.weeklyOvertime(total(timesheet));
    }
}
