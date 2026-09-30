package ru.edu.pr01;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Автоматическая проверка проекта.
 *
 * <p>Сохраняет исходную проверку задания и добавляет граничные проверки п. 9:
 * успешный результат, нижние и верхние границы диапазонов, пустое значение,
 * значение неверного формата и повторный ввод. В конце прогоняется открытый
 * набор {@code data/sample.csv} с проверкой ожидаемых итогов.
 */
public class SelfCheck {

    private static final double EPS = 1e-9;
    private static final String SAMPLE_FILE = "data" + java.io.File.separator + "sample.csv";

    private static int passed;
    private static final List<String> failures = new ArrayList<String>();

    public static void main(String[] args) {
        checkOriginalTaskAssertion();
        checkTypicalValue();
        checkLowerBounds();
        checkUpperBounds();
        checkDiscountBounds();
        checkMissingValues();
        checkWrongFormat();
        checkRepeatedInput();
        checkOutputFormat();
        runSampleData();

        System.out.println();
        System.out.println("Проверок пройдено: " + passed + ", провалено: " + failures.size());
        if (failures.isEmpty()) {
            System.out.println("PASS");
        } else {
            for (String failure : failures) {
                System.out.println("  " + failure);
            }
            System.out.println("FAIL  не пройдено проверок: " + failures.size());
        }
    }

    /* ---------- Исходная проверка из задания ---------- */

    private static void checkOriginalTaskAssertion() {
        section("Исходная проверка задания");
        check("calculateBase(2, 100.0) == 200.0",
                near(OrderCalculator.calculateBase(2, 100.0), 200.0));
        check("calculateTotal(2, 100.0, 10.0, 20.0) == 216.0",
                near(OrderCalculator.calculateTotal(2, 100.0, 10.0, 20.0), 216.0));
    }

    /* ---------- 1. Корректное типовое значение ---------- */

    private static void checkTypicalValue() {
        section("Корректное типовое значение");
        check("isValid(12, 2490.50, 5) == true", OrderCalculator.isValid(12, 2490.50, 5.0));
        check("итог 12 x 2490.50 со скидкой 5 % и НДС 20 % == 34070.04",
                near(OrderCalculator.calculateTotal(12, 2490.50, 5.0, OrderCalculator.VAT_RATE),
                        34070.04));
        check("база 12 x 2490.50 == 29886.00",
                near(OrderCalculator.calculateBase(12, 2490.50), 29886.00));
        check("после скидки 5 % == 28391.70",
                near(OrderCalculator.applyDiscount(29886.00, 5.0), 28391.70));
        check("НДС 20 % от 28391.70 == 5678.34",
                near(OrderCalculator.calculateVat(28391.70, 20.0), 5678.34));
    }

    /* ---------- 2. Нижние границы (допустимые минимальные значения) ---------- */

    private static void checkLowerBounds() {
        section("Нижние границы");
        check("количество 1 допустимо", OrderCalculator.isValid(1, 1.00, 0.0));
        check("цена 0.01 допустима", OrderCalculator.isValid(1, 0.01, 0.0));
        check("скидка 0 % допустима", OrderCalculator.isValid(1, 1.00, 0.0));
        check("итог 1 x 1.00 без скидки == 1.20",
                near(OrderCalculator.calculateTotal(1, 1.00, 0.0, OrderCalculator.VAT_RATE), 1.20));
        check("нулевой НДС даёт цену без налога",
                near(OrderCalculator.calculateTotal(2, 100.0, 10.0, 0.0), 180.00));
    }

    /* ---------- 3. Верхние границы (максимальные значения) ---------- */

    private static void checkUpperBounds() {
        section("Верхние границы");
        check("количество 10000 допустимо", OrderCalculator.isValid(10_000, 1.00, 0.0));
        check("цена 5000000.00 допустима", OrderCalculator.isValid(1, 5_000_000.00, 0.0));
        check("сочетание максимумов допустимо",
                OrderCalculator.isValid(10_000, 5_000_000.00, OrderCalculator.MAX_DISCOUNT_PERCENT));
        check("база 10000 x 5000000 == 50000000000.00",
                near(OrderCalculator.calculateBase(10_000, 5_000_000.00), 50_000_000_000.00));
        check("итог на максимумах == 33000000000.00",
                near(OrderCalculator.calculateTotal(10_000, 5_000_000.00, 45.0,
                        OrderCalculator.VAT_RATE), 33_000_000_000.00));
    }

    /* ---------- 4. Границы скидки (вариант 8: 0..45 %) ---------- */

    private static void checkDiscountBounds() {
        section("Границы скидки, вариант 8");
        check("скидка 45 % допустима", OrderCalculator.isValid(1, 100.0, 45.0));
        check("скидка 35 % допустима (в базовом правиле была бы отказом)",
                OrderCalculator.isValid(1, 100.0, 35.0));
        check("скидка 45.01 отклоняется", !OrderCalculator.isValid(1, 100.0, 45.01));
        check("скидка -0.01 отклоняется", !OrderCalculator.isValid(1, 100.0, -0.01));
        check("для 45 % итог 100.00 -> 55.00 + НДС 11.00 = 66.00",
                near(OrderCalculator.calculateTotal(1, 100.0, 45.0, OrderCalculator.VAT_RATE), 66.00));
    }

    /* ---------- 5. Пустое или отсутствующее значение ---------- */

    private static void checkMissingValues() {
        section("Пустое или отсутствующее значение");
        check("parseInteger(null) == null", OrderCalculator.parseInteger(null) == null);
        check("parseInteger(\"\") == null", OrderCalculator.parseInteger("") == null);
        check("parseInteger(\"   \") == null", OrderCalculator.parseInteger("   ") == null);
        check("parseDecimal(null) == null", OrderCalculator.parseDecimal(null) == null);
        check("parseDecimal(\"\") == null", OrderCalculator.parseDecimal("") == null);
        check("количество 0 отклоняется", !OrderCalculator.isValid(0, 100.0, 0.0));
        check("для количества 0 есть причина ошибки",
                OrderCalculator.describeInvalid(0, 100.0, 0.0) != null);
        check("для количества 0 расчёт не выполняется",
                !OrderCalculator.isValid(0, 100.0, 0.0));
    }

    /* ---------- 6. Значение неверного формата ---------- */

    private static void checkWrongFormat() {
        section("Значение неверного формата");
        check("parseInteger(\"abc\") == null", OrderCalculator.parseInteger("abc") == null);
        check("parseInteger(\"2.5\") == null", OrderCalculator.parseInteger("2.5") == null);
        check("parseInteger(\"1e3\") == null", OrderCalculator.parseInteger("1e3") == null);
        check("parseDecimal(\"abc\") == null", OrderCalculator.parseDecimal("abc") == null);
        check("parseDecimal(\"1,5\") == 1.5", near(OrderCalculator.parseDecimal("1,5"), 1.5));
        check("parseDecimal(\"2490.50\") == 2490.50",
                near(OrderCalculator.parseDecimal("2490.50"), 2490.50));
        check("parseDecimal(\"NaN\") == null", OrderCalculator.parseDecimal("NaN") == null);
        check("parseDecimal(\"1e400\") == null", OrderCalculator.parseDecimal("1e400") == null);
        check("NaN в цене отклоняется", !OrderCalculator.isValid(1, Double.NaN, 0.0));
        check("бесконечность в скидке отклоняется",
                !OrderCalculator.isValid(1, 100.0, Double.POSITIVE_INFINITY));
    }

    /* ---------- 7. Повтор или конфликт данных ---------- */

    private static void checkRepeatedInput() {
        section("Повторный и конфликтный ввод");
        double first = OrderCalculator.calculateTotal(12, 2490.50, 5.0, OrderCalculator.VAT_RATE);
        double second = OrderCalculator.calculateTotal(12, 2490.50, 5.0, OrderCalculator.VAT_RATE);
        check("повторный расчёт даёт тот же итог (состояние не накапливается)",
                near(first, second));
        OrderCalculator.isValid(5, -20.0, 5.0);
        double afterError = OrderCalculator.calculateTotal(12, 2490.50, 5.0, OrderCalculator.VAT_RATE);
        check("после ошибочного расчёта результат не искажается", near(afterError, first));
        check("конфликтные значения не молча усредняются",
                !OrderCalculator.isValid(5, -20.0, 5.0)
                        && OrderCalculator.isValid(5, 20.0, 5.0));
    }

    /* ---------- 8. Формат вывода ---------- */

    private static void checkOutputFormat() {
        section("Формат вывода");
        check("formatMoney(2160.0) == \"2160.00\"",
                "2160.00".equals(OrderCalculator.formatMoney(2160.0)));
        check("formatMoney(0.0) == \"0.00\"",
                "0.00".equals(OrderCalculator.formatMoney(0.0)));
        check("formatMoney(1234.5) == \"1234.50\"",
                "1234.50".equals(OrderCalculator.formatMoney(1234.5)));
        check("formatMoney(-20.0) == \"-20.00\"",
                "-20.00".equals(OrderCalculator.formatMoney(-20.0)));
        check("roundMoney(28391.700000000003) == 28391.70",
                near(OrderCalculator.roundMoney(28391.700000000003), 28391.70));
    }

    /* ---------- 9. Открытый набор sample.csv ---------- */

    private static void runSampleData() {
        section("Открытый набор data/sample.csv");
        Path path = Paths.get(SAMPLE_FILE);
        if (!Files.isReadable(path)) {
            System.out.println("  пропуск: файл " + SAMPLE_FILE + " не найден (запустите из папки starter_project)");
            return;
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("  пропуск: не удалось прочитать " + SAMPLE_FILE + " (" + e.getMessage() + ")");
            return;
        }

        int index = 0;
        for (String line : lines) {
            String text = line.trim();
            if (text.isEmpty() || text.startsWith("#")) {
                continue;
            }
            if (index == 0) {
                index++;
                continue;
            }
            String[] parts = text.split(";");
            index++;
            String name = "строка " + index + " (" + text + ")";

            Integer quantity = parts.length > 0 ? OrderCalculator.parseInteger(parts[0]) : null;
            Double unitPrice = parts.length > 1 ? OrderCalculator.parseDecimal(parts[1]) : null;
            Double discount = parts.length > 2 ? OrderCalculator.parseDecimal(parts[2]) : null;
            if (quantity == null || unitPrice == null || discount == null) {
                check(name + ": строка разобрана", false);
                continue;
            }

            boolean valid = OrderCalculator.isValid(quantity, unitPrice, discount);
            double expected = expectedTotal(parts);
            if (expected < 0) {
                check(name + ": отказ по правилам проверки ввода", !valid);
            } else {
                check(name + ": принята и даёт " + OrderCalculator.formatMoney(expected),
                        valid && near(OrderCalculator.calculateTotal(quantity, unitPrice, discount,
                                OrderCalculator.VAT_RATE), expected));
            }
        }
    }

    /** Ожидаемые итоги открытого набора; отрицательное значение — ожидается отказ. */
    private static double expectedTotal(String[] parts) {
        String quantity = parts[0].trim();
        if ("0".equals(quantity)) {
            return -1;
        }
        if (parts.length > 1 && parts[1].trim().startsWith("-")) {
            return -1;
        }
        switch (quantity + "|" + parts[1].trim() + "|" + parts[2].trim()) {
            case "12|2490.50|5":
                return 34070.04;
            case "100|150.00|0":
                return 18000.00;
            case "3|999.99|35":
                return 2339.98;
            default:
                return -1;
        }
    }

    /* ---------------- Служебное ---------------- */

    private static void section(String title) {
        System.out.println();
        System.out.println("== " + title + " ==");
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  OK   " + name);
        } else {
            String message = "FAIL " + name;
            failures.add(message);
            System.out.println("  " + message);
        }
    }

    private static boolean near(Double expected, double actual) {
        return expected != null && Math.abs(expected.doubleValue() - actual) < EPS;
    }

    private static boolean near(double expected, double actual) {
        return Math.abs(expected - actual) < EPS;
    }
}
