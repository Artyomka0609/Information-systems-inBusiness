package ru.edu.pr02;

import java.util.Locale;
import java.util.Scanner;


public final class Main {
    private Main() {
    }

    public static void main(String[] args) {

        TimesheetConstraints constraints = new TimesheetConstraints(5, 45);
        TimesheetParser parser = new TimesheetParser(constraints);
        TimesheetAnalyzer analyzer = new TimesheetAnalyzer(new NormOvertimePolicy(8, 40));

        if (args.length > 1) {
            System.out.println("Ошибка: передайте строку табеля одним аргументом в кавычках.");
            return;
        }
        String line;
        if (args.length == 1) {
            line = args[0];
        } else {
            System.out.println("Введите часы за " + constraints.getDayCount() + " дней через ; (от 0 до "
                    + constraints.getMaxHours() + "): ");
            Scanner scanner = new Scanner(System.in, "UTF-8");
            line = scanner.hasNextLine() ? scanner.nextLine() : null;
        }
        try {
            Timesheet timesheet = parser.parse(line);
            System.out.println("Сумма: " + analyzer.total(timesheet) + " ч.");
            System.out.printf(Locale.forLanguageTag("ru-RU"), "Среднее: %.2f ч.%n",
                    analyzer.average(timesheet));
            System.out.println("Дней с переработкой: " + analyzer.overtimeDays(timesheet));
            System.out.println("Недельная переработка: " + analyzer.weeklyOvertime(timesheet) + " ч.");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}
