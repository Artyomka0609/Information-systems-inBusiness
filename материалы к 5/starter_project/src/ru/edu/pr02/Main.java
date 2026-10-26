package ru.edu.pr02;

import java.util.Locale;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String line;
        if (args.length > 1) {
            System.out.println("Ошибка: передайте строку табеля одним аргументом в кавычках.");
            return;
        }
        if (args.length == 1) {
            line = args[0];
        } else {
            System.out.println("Введите часы за " + TimesheetAnalyzer.DAYS + " дней через (от 0 до " + TimesheetAnalyzer.MAX_HOURS + "): (вводить через ;)");
            Scanner scanner = new Scanner(System.in, "UTF-8");
            line = scanner.hasNextLine() ? scanner.nextLine() : null;
        }
        try {
            int[] hours = TimesheetAnalyzer.parseHours(line);
            System.out.println("Сумма: " + TimesheetAnalyzer.total(hours) + " ч.");
            System.out.printf(Locale.forLanguageTag("ru-RU"), "Среднее: %.2f ч.%n",
                    TimesheetAnalyzer.average(hours));
            System.out.println("Дней с переработкой: " + TimesheetAnalyzer.overtimeDays(hours));
            System.out.println("Недельная переработка: " + TimesheetAnalyzer.weeklyOvertime(hours) + " ч.");
        } catch (IllegalArgumentException e) {
            // При ошибке parseHours не возвращает массив отчёт не вычисляется.
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}
