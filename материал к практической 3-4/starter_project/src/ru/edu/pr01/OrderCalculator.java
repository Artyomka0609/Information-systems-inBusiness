package ru.edu.pr01;

import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Расчёт предварительной стоимости заказа отдела закупок.
 *
 * <p>Бизнес-правила (п. 3 задания и индивидуальное правило варианта 8):
 * <ul>
 *   <li>количество единиц: от 1 до 10 000 включительно;</li>
 *   <li>цена одной единицы: строго больше 0 и не больше 5 000 000 руб.;</li>
 *   <li>скидка: от 0 % до 45 % включительно (в базовом правиле верхняя граница 30 %,
 *       вариант 8 меняет её на 45 %);</li>
 *   <li>НДС 20 % начисляется на сумму ПОСЛЕ применения скидки;</li>
 *   <li>при неверном входном значении расчёт не выполняется, выводится причина ошибки.</li>
 * </ul>
 *
 * <p>Денежные суммы считаются в {@code double}, но каждый промежуточный результат
 * округляется до копеек, чтобы накопление ошибки двоичного представления не влияло
 * на печать итога с двумя знаками после запятой.
 */
public class OrderCalculator {

    /** Ставка НДС, %. */
    public static final double VAT_RATE = 20.0;

    /** Нижняя граница количества единиц. */
    public static final int MIN_QUANTITY = 1;

    /** Верхняя граница количества единиц. */
    public static final int MAX_QUANTITY = 10_000;

    /** Цена одной единицы должна быть строго больше этого значения. */
    public static final double MIN_UNIT_PRICE = 0.0;

    /** Верхняя граница цены одной единицы, руб. */
    public static final double MAX_UNIT_PRICE = 5_000_000.0;

    /** Нижняя граница скидки, %. */
    public static final double MIN_DISCOUNT_PERCENT = 0.0;

    /**
     * Верхняя граница скидки, %.
     *
     * <p>Индивидуальное правило варианта 8: 45 % вместо 30 % из п. 3 задания.
     * Правило хранится в одной константе, поэтому меняется локально и не дублируется
     * в других классах.
     */
    public static final double MAX_DISCOUNT_PERCENT = 45.0;

    /** Количество знаков после запятой в денежных суммах. */
    public static final int MONEY_SCALE = 2;

    private static final double KOPECKS = 100.0;
    private static final Pattern DECIMAL_PATTERN =
            Pattern.compile("[+-]?(?:\\d+(?:\\.\\d*)?|\\.\\d+)");

    private OrderCalculator() {
    }

    /* ---------------- Проверка входных данных (п. 3 задания) ---------------- */

    /**
     * Проверяет диапазоны всех входных значений.
     *
     * @return {@code true}, если значения допустимы.
     */
    public static boolean isValid(int quantity, double unitPrice, double discountPercent) {
        return describeInvalid(quantity, unitPrice, discountPercent) == null;
    }

    /**
     * Возвращает описание первой найденной ошибки.
     *
     * <p>Записи построены так, чтобы неверный формат не мог пройти проверку:
     * условия диапазонов записаны через {@code !(в диапазоне)}, поэтому
     * {@code NaN} и бесконечности отбрасываются как некорректные значения.
     *
     * @return текст ошибки или {@code null}, если все значения корректны.
     */
    public static String describeInvalid(int quantity, double unitPrice, double discountPercent) {
        if (quantity < MIN_QUANTITY) {
            return "количество должно быть больше нуля, получено " + quantity;
        }
        if (quantity > MAX_QUANTITY) {
            return "количество не должно превышать " + MAX_QUANTITY + ", получено " + quantity;
        }
        if (!(unitPrice > MIN_UNIT_PRICE) || !(unitPrice <= MAX_UNIT_PRICE)) {
            return "цена единицы должна быть в пределах ("
                    + formatMoney(MIN_UNIT_PRICE) + "; " + formatMoney(MAX_UNIT_PRICE)
                    + "], получено " + formatMoney(unitPrice);
        }
        if (!(discountPercent >= MIN_DISCOUNT_PERCENT && discountPercent <= MAX_DISCOUNT_PERCENT)) {
            return "скидка должна быть в пределах [" + formatMoney(MIN_DISCOUNT_PERCENT)
                    + "; " + formatMoney(MAX_DISCOUNT_PERCENT) + "], получено "
                    + formatMoney(discountPercent);
        }
        return null;
    }

    /* ---------------- Разбор строки ввода ---------------- */

    /**
     * Разбирает строку в целое число.
     *
     * @return значение или {@code null}, если строка пуста либо имеет другой формат.
     */
    public static Integer parseInteger(String raw) {
        String text = normalize(raw);
        if (text.isEmpty() || !Pattern.matches("[+-]?\\d+", text)) {
            return null;
        }
        try {
            return Integer.valueOf(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Разбирает строку в десятичное число. Запятая допускается как знак дробной части.
     *
     * @return значение или {@code null}, если строка пуста либо имеет другой формат.
     */
    public static Double parseDecimal(String raw) {
        String text = normalize(raw);
        if (text.isEmpty() || !DECIMAL_PATTERN.matcher(text).matches()) {
            return null;
        }
        try {
            double value = Double.parseDouble(text);
            return Double.isFinite(value) ? Double.valueOf(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String normalize(String raw) {
        return raw == null ? "" : raw.trim().replace(',', '.').replace(" ", "");
    }

    private static boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    /* ---------------- Расчёты (п. 3 задания) ---------------- */

    /** Базовая стоимость: количество умножить на цену единицы. */
    public static double calculateBase(int quantity, double unitPrice) {
        return roundMoney(quantity * unitPrice);
    }

    /**
     * Стоимость после скидки.
     *
     * <p>Скидка задана в процентах, поэтому берётся часть от базовой стоимости,
     * а не само процентное число из входа.
     */
    public static double applyDiscount(double base, double discountPercent) {
        return roundMoney(base * (100.0 - discountPercent) / 100.0);
    }

    /** НДС начисляется на сумму после скидки. */
    public static double calculateVat(double discounted, double vatPercent) {
        return roundMoney(discounted * vatPercent / 100.0);
    }

    /**
     * Полная стоимость заказа. Формулы берутся из методов выше и не повторяются.
     */
    public static double calculateTotal(int quantity, double unitPrice,
                                        double discountPercent, double vatPercent) {
        double base = calculateBase(quantity, unitPrice);
        double discounted = applyDiscount(base, discountPercent);
        double vat = calculateVat(discounted, vatPercent);
        return roundMoney(discounted + vat);
    }

    /* ---------------- Округление и вывод ---------------- */

    /** Округляет денежную сумму до копеек. */
    public static double roundMoney(double value) {
        return Math.round(value * KOPECKS) / KOPECKS;
    }

    /** Печатает денежную сумму с двумя знаками после запятой. */
    public static String formatMoney(double value) {
        return String.format(Locale.ROOT, "%." + MONEY_SCALE + "f", value);
    }

    /* ---------------- Консольный сценарий ---------------- */

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Расчёт предварительной стоимости заказа.");
        System.out.println("Правила: количество " + MIN_QUANTITY + ".." + MAX_QUANTITY
                + "; цена (0; " + formatMoney(MAX_UNIT_PRICE) + "] руб.; скидка "
                + formatMoney(MIN_DISCOUNT_PERCENT) + ".." + formatMoney(MAX_DISCOUNT_PERCENT)
                + " %; НДС " + formatMoney(VAT_RATE) + " % поверх суммы после скидки.");

        String rawQuantity = readLine(scanner, "Количество единиц: ");
        if (isBlank(rawQuantity)) {
            abort("количество единиц не введено");
            return;
        }
        Integer quantity = parseInteger(rawQuantity);
        if (quantity == null) {
            abort("количество единиц должно быть целым числом");
            return;
        }

        String rawPrice = readLine(scanner, "Цена одной единицы, руб.: ");
        if (isBlank(rawPrice)) {
            abort("цена единицы не введена");
            return;
        }
        Double unitPrice = parseDecimal(rawPrice);
        if (unitPrice == null) {
            abort("цена единицы должна быть числом");
            return;
        }

        String rawDiscount = readLine(scanner, "Скидка, %: ");
        if (isBlank(rawDiscount)) {
            abort("скидка не введена");
            return;
        }
        Double discountPercent = parseDecimal(rawDiscount);
        if (discountPercent == null) {
            abort("скидка должна быть числом");
            return;
        }

        String problem = describeInvalid(quantity.intValue(), unitPrice.doubleValue(),
                discountPercent.doubleValue());
        if (problem != null) {
            abort(problem);
            return;
        }

        printReport(quantity.intValue(), unitPrice.doubleValue(), discountPercent.doubleValue());
        scanner.close();
    }

    private static String readLine(Scanner scanner, String prompt) {
        System.out.print(prompt);
        System.out.flush();
        return scanner.hasNextLine() ? scanner.nextLine() : null;
    }

    private static void abort(String reason) {
        System.out.println("Ошибка входных данных: " + reason + ". Расчёт не выполняется.");
    }

    private static void printReport(int quantity, double unitPrice, double discountPercent) {
        double base = calculateBase(quantity, unitPrice);
        double discounted = applyDiscount(base, discountPercent);
        double discountAmount = roundMoney(base - discounted);
        double vat = calculateVat(discounted, VAT_RATE);
        double total = roundMoney(discounted + vat);

        System.out.println("Базовая стоимость (количество x цена)  : " + formatMoney(base) + " руб.");
        System.out.println("Скидка " + formatMoney(discountPercent) + " %"
                + "                                  : " + formatMoney(discountAmount) + " руб.");
        System.out.println("Стоимость после скидки               : " + formatMoney(discounted) + " руб.");
        System.out.println("НДС " + formatMoney(VAT_RATE) + " %"
                + "                                 : " + formatMoney(vat) + " руб.");
        System.out.println("Итого                                : " + formatMoney(total) + " руб.");
    }
}
