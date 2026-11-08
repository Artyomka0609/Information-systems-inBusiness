package ru.edu.pr02;


public final class SelfCheck {
    private final TimesheetConstraints constraints = new TimesheetConstraints(5, 45);
    private final TimesheetParser parser = new TimesheetParser(constraints);
    private final TimesheetAnalyzer analyzer = new TimesheetAnalyzer(new NormOvertimePolicy(8, 40));
    private int passed;
    private int failed;

    public static void main(String[] args) {
        new SelfCheck().run();
    }

    private void run() {
        check("Исходная проверка суммы",
                analyzer.total(new Timesheet(new int[]{8, 8, 8, 8, 8}, constraints)) == 40);
        check("Исходная проверка дней переработки",
                analyzer.overtimeDays(new Timesheet(new int[]{9, 8, 7, 10, 8}, constraints)) == 2);
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

        checkEncapsulation();
        checkExtension();
        checkPolicyContract("NormOvertimePolicy", new NormOvertimePolicy(8, 40));
        checkPolicyContract("NoOvertimePolicy", new NoOvertimePolicy());
        invalid("Первая ошибка имеет приоритет", "46;abc;8;8;8", "Позиция 1");
        rejects("Нулевая длина недели запрещена", () -> new TimesheetConstraints(0, 45));
        rejects("Отрицательная длина недели запрещена", () -> new TimesheetConstraints(-1, 45));
        rejects("Отрицательный максимум запрещён", () -> new TimesheetConstraints(5, -1));
        rejects("Настройки не допускают переполнение суммы",
                () -> new TimesheetConstraints(5, Integer.MAX_VALUE));
        rejects("Отрицательная дневная норма запрещена", () -> new NormOvertimePolicy(-1, 40));
        rejects("Отрицательная недельная норма запрещена", () -> new NormOvertimePolicy(8, -1));
        System.out.println("Результат: PASS=" + passed + ", FAIL=" + failed);
        if (failed != 0) {
            throw new AssertionError("Проверки не пройдены: " + failed);
        }
    }

    private void checkEncapsulation() {
        int[] original = {8, 8, 8, 8, 8};
        Timesheet timesheet = new Timesheet(original, constraints);
        original[0] = 999;
        check("Изменение исходного массива не меняет табель",
                timesheet.getHours(0) == 8 && analyzer.total(timesheet) == 40);
        analyzer.average(timesheet);
        analyzer.overtimeDays(timesheet);
        analyzer.weeklyOvertime(timesheet);
        check("Расчёты не изменяют табель", timesheet.getHours(0) == 8 && analyzer.total(timesheet) == 40);
    }

    private void checkExtension() {
        Timesheet timesheet = parser.parse("9;8;7;10;8");
        TimesheetAnalyzer changedNorms = new TimesheetAnalyzer(new NormOvertimePolicy(10, 50));
        check("Другие нормы подключаются без изменения анализатора",
                changedNorms.total(timesheet) == 42 && changedNorms.overtimeDays(timesheet) == 0
                        && changedNorms.weeklyOvertime(timesheet) == 0);
        TimesheetAnalyzer noOvertime = new TimesheetAnalyzer(new NoOvertimePolicy());
        check("Другая реализация интерфейса работает в том же анализаторе",
                noOvertime.total(timesheet) == 42 && Math.abs(noOvertime.average(timesheet) - 8.4) < 0.000001
                        && noOvertime.overtimeDays(timesheet) == 0 && noOvertime.weeklyOvertime(timesheet) == 0);
        check("Подмена правила не меняет исходный анализатор",
                analyzer.overtimeDays(timesheet) == 2 && analyzer.weeklyOvertime(timesheet) == 2);
        TimesheetParser baseParser = new TimesheetParser(new TimesheetConstraints(5, 16));
        check("Базовое ограничение 16 включительно", analyzer.total(baseParser.parse("16;0;0;0;0")) == 16);
        rejects("При максимуме 16 значение 17 отклоняется", () -> baseParser.parse("8;8;17;8;8"));
        TimesheetParser shortWeek = new TimesheetParser(new TimesheetConstraints(3, 16));
        check("Число дней берётся из объекта", Math.abs(analyzer.average(shortWeek.parse("8;9;10")) - 9.0) < 0.000001);
        TimesheetParser zeroLimit = new TimesheetParser(new TimesheetConstraints(1, 0));
        check("Нулевой максимум допускает нулевые часы", analyzer.total(zeroLimit.parse("0")) == 0);
        OvertimePolicy zeroNorms = new NormOvertimePolicy(0, 0);
        check("Нулевые нормы допустимы", !zeroNorms.isOvertimeDay(0)
                && zeroNorms.isOvertimeDay(1) && zeroNorms.weeklyOvertime(1) == 1);
    }

    /** Один и тот же набор проверок контракта для разных реализаций (LSP). */
    private void checkPolicyContract(String name, OvertimePolicy policy) {
        int[] samples = {0, 1, 8, 9, 40, 41, 45, 225, Integer.MAX_VALUE};
        boolean holds = true;
        try {
            for (int hours : samples) {
                int overtime = policy.weeklyOvertime(hours);
                boolean day = policy.isOvertimeDay(hours);
                holds &= overtime >= 0 && overtime <= hours;
                holds &= overtime == policy.weeklyOvertime(hours);
                holds &= day == policy.isOvertimeDay(hours);
            }
            check(name + ": допустимые аргументы, границы результата и повторяемость", holds);
        } catch (RuntimeException e) {
            check(name + ": исключение для допустимого аргумента", false);
        }
        rejects(name + ": отрицательные часы дня", () -> policy.isOvertimeDay(-1));
        rejects(name + ": отрицательная сумма", () -> policy.weeklyOvertime(-1));
    }

    private void valid(String name, String line, int sum, double average, int days, int overtime) {
        try {
            Timesheet timesheet = parser.parse(line);
            check(name, timesheet.getDayCount() == 5
                    && analyzer.total(timesheet) == sum
                    && Math.abs(analyzer.average(timesheet) - average) < 0.000001
                    && analyzer.overtimeDays(timesheet) == days
                    && analyzer.weeklyOvertime(timesheet) == overtime);
        } catch (Exception e) {
            check(name + " (неожиданная ошибка: " + e.getMessage() + ")", false);
        }
    }

    private void invalid(String name, String line, String expectedPosition) {
        try {
            parser.parse(line);
            check(name + " (ошибка не обнаружена)", false);
        } catch (IllegalArgumentException e) {
            check(name, e.getMessage().contains(expectedPosition));
        }
    }

    private void invalidArray(String name, int[] hours) {
        rejects(name, () -> new Timesheet(hours, constraints));
    }

    private void rejects(String name, Runnable action) {
        try {
            action.run();
            check(name + " (ошибка не обнаружена)", false);
        } catch (IllegalArgumentException e) {
            check(name, true);
        }
    }

    private void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS " + name);
        } else {
            failed++;
            System.out.println("FAIL " + name);
        }
    }
}
