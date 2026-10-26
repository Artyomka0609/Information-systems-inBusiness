package ru.edu.pr02;
public class SelfCheck {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        // Две исходные проверки сохранены, остальные добавлены.
        check("Исходная проверка суммы", TimesheetAnalyzer.total(new int[]{8, 8, 8, 8, 8}) == 40);
        check("Исходная проверка дней переработки",
                TimesheetAnalyzer.overtimeDays(new int[]{9, 8, 7, 10, 8}) == 2);
        valid("Норма недели", "8;8;8;8;8", 40, 8.0, 0, 0);
        valid("Типовой табель", "9;8;7;10;8", 42, 8.4, 2, 2);
        valid("Нижняя граница", "0;0;0;0;0", 0, 0.0, 0, 0);
        valid("Верхняя граница варианта 8", "45;45;45;45;45", 225, 45.0, 5, 185);
        valid("17 допустимо в варианте 8", "8;8;17;8;8", 49, 9.8, 1, 9);
        valid("Пробелы вокруг значений", " 8 ; 9;7 ; 10; 8 ", 42, 8.4, 2, 2);
        valid("Дробное среднее", "1;0;0;0;0", 1, 0.2, 0, 0);
        valid("День переработки без недельной", "9;0;0;0;0", 9, 1.8, 1, 0);
        valid("Ровно 8 не является переработкой", "8;0;0;0;0", 8, 1.6, 0, 0);
        valid("Повторяющиеся значения допустимы", "2;2;2;2;2", 10, 2.0, 0, 0);
        valid("Одно значение на верхней границе", "45;0;0;0;0", 45, 9.0, 1, 5);
        valid("Ниже недельной нормы", "7;8;8;8;8", 39, 7.8, 0, 0);
        valid("На час выше недельной нормы", "9;8;8;8;8", 41, 8.2, 1, 1);
        invalid("Пустая строка", "", "Позиция 1");
        invalid("Строка из пробелов", "   ", "Позиция 1");
        invalid("Отсутствующая строка", null, "Позиция 1");
        invalid("46 превышает максимум", "8;8;46;8;8", "Позиция 3");
        invalid("Отрицательное значение", "-1;8;8;8;8", "Позиция 1");
        invalid("Неверный формат", "8;abc;8;8;8", "Позиция 2");
        invalid("Дробное число", "8;8;8.5;8;8", "Позиция 3");
        invalid("Дробь с запятой", "8;8;8,5;8;8", "Позиция 3");
        invalid("Научная запись", "8;8;1e1;8;8", "Позиция 3");
        invalid("Слишком большое число для int", "8;8;999999999999999;8;8", "Позиция 3");
        invalid("Четыре значения", "8;8;8;8", "Позиция 5");
        invalid("Шесть значений", "8;8;8;8;8;8", "Позиция 6");
        invalid("Пустая позиция внутри", "8;;8;8;8", "Позиция 2");
        invalid("Пустая последняя позиция", "8;8;8;8;", "Позиция 5");
        invalid("Лишний разделитель в конце", "8;8;8;8;8;", "Позиция 6");
        invalid("Пустая первая позиция", ";8;8;8;8", "Позиция 1");
        invalid("Пять пустых полей", ";;;;", "Позиция 1");
        invalid("Пробел внутри числа", "8;8;1 0;8;8", "Позиция 3");
        invalidArray("null вместо массива", null);
        invalidArray("Пустой массив", new int[0]);
        invalidArray("Массив неправильной длины", new int[]{8, 8, 8, 8});
        invalidArray("Недопустимое значение в массиве", new int[]{8, 8, 46, 8, 8});
        invalidArray("Отрицательное значение в массиве", new int[]{8, -1, 8, 8, 8});
        System.out.println("Результат: PASS=" + passed + ", FAIL=" + failed);
        if (failed != 0) {
            throw new AssertionError("Проверки не пройдены: " + failed);
        }
    }

    private static void valid(String name, String line, int sum, double average, int days, int overtime) {
        try {
            int[] hours = TimesheetAnalyzer.parseHours(line);
            check(name, hours.length == 5
                    && TimesheetAnalyzer.total(hours) == sum
                    && Math.abs(TimesheetAnalyzer.average(hours) - average) < 0.000001
                    && TimesheetAnalyzer.overtimeDays(hours) == days
                    && TimesheetAnalyzer.weeklyOvertime(hours) == overtime);
        } catch (Exception e) {
            check(name + " (неожиданная ошибка: " + e.getMessage() + ")", false);
        }
    }

    private static void invalid(String name, String line, String expectedPosition) {
        try {
            TimesheetAnalyzer.parseHours(line);
            check(name + " (ошибка не обнаружена)", false);
        } catch (IllegalArgumentException e) {
            check(name + " -> " + e.getMessage(), e.getMessage().contains(expectedPosition));
        }
    }

    private static void invalidArray(String name, int[] hours) {
        int rejected = 0;
        try { TimesheetAnalyzer.total(hours); } catch (IllegalArgumentException e) { rejected++; }
        try { TimesheetAnalyzer.average(hours); } catch (IllegalArgumentException e) { rejected++; }
        try { TimesheetAnalyzer.overtimeDays(hours); } catch (IllegalArgumentException e) { rejected++; }
        try { TimesheetAnalyzer.weeklyOvertime(hours); } catch (IllegalArgumentException e) { rejected++; }
        check(name + " (четыре расчётных метода)", rejected == 4);
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS " + name);
        } else {
            failed++;
            System.out.println("FAIL " + name);
        }
    }
}
