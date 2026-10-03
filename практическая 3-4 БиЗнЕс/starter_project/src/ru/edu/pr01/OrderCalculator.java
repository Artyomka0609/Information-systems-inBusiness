package ru.edu.pr01;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Scanner;

/**
 *
 *   1 константы правил ... разделы 3, 10 
 *   2 isValid / errorOf .. раздел 7 п.1 
 *   3 calculate* ......... раздел 7 п.2–3 (НДС после скидки;)
 *   4 main (Scanner) ..... раздел 7; при ошибке — сообщение, расчёт НЕ выполняется
 *   5 parse/rub/round2 ... раздел 9; раздел 7 п.4, п.6 (double)
 */
public class OrderCalculator {

    // 1 КОНСТАНТЫ (раздел 10) 
    public static final int    MAX_QUANTITY         = 10_000;
    public static final double MAX_UNIT_PRICE       = 5_000_000.0;
    public static final double MAX_DISCOUNT_PERCENT = 45.0;
    public static final double DEFAULT_VAT_PERCENT  = 20.0; // актуальная ставка НДС

    /** 2 TODO 1 — проверка количества, цены и скидки (раздел 7 п.1) */

    /** 2 isValid / errorOf .. раздел 7 п.1  */
    public static boolean isValid(int quantity, double unitPrice, double discountPercent) {
        return errorOf(quantity, unitPrice, discountPercent) == null;
    }

    /** раздел 3  */
    public static String errorOf(int quantity, double unitPrice, double discountPercent) {
        if (quantity <= 0 || quantity > MAX_QUANTITY)
            return "количество должно быть от 1 до " + MAX_QUANTITY + ", получено: " + quantity;
        if (!(unitPrice > 0) || unitPrice > MAX_UNIT_PRICE)      // !(...) отсекает и NaN
            return "цена должна быть больше 0 и не больше " + (long) MAX_UNIT_PRICE
                    + " руб, получено: " + unitPrice;
        if (discountPercent < 0 || discountPercent > MAX_DISCOUNT_PERCENT)
            return "скидка должна быть от 0 до " + (long) MAX_DISCOUNT_PERCENT
                    + " % , получено: " + discountPercent;
        return null;
    }

    /** 3 calculate* ......... раздел 7 п.2–3 (НДС после скидки;) */
    public static double calculateBase(int quantity, double unitPrice) {
        return round2(quantity * unitPrice);
    }

    /** TODO 3. Раздел 8, исправлено (процент не в рублях). */
    public static double applyDiscount(double base, double discountPercent) {
        return round2(base * (100 - discountPercent) / 100.0);
    }

    /**  раздел 3......................... */
    public static double calculateVat(double discounted, double vatPercent) {
        if (!(vatPercent >= 0 && vatPercent <= 100))
            throw new IllegalArgumentException("ставка НДС должна быть 0..100 %, получено: " + vatPercent);
        return round2(discounted * vatPercent / 100.0);
    }

    /** TODO 5................. */
    public static double calculateTotal(int quantity, double unitPrice,
                                        double discountPercent, double vatPercent) {
        String error = errorOf(quantity, unitPrice, discountPercent);
        if (error != null)
            throw new IllegalArgumentException(error + " — расчёт не выполнен"); // раздел 3
        double discounted = applyDiscount(calculateBase(quantity, unitPrice), discountPercent);
        return round2(discounted + calculateVat(discounted, vatPercent));        // НДС ПОСЛЕ скидки
    }

    /**  4 (раздел 7) (п.3) */

    public static void main(String[] args) {
        System.out.println("Правила: количество 1.." + MAX_QUANTITY + "; цена > 0 и <= "
                + (long) MAX_UNIT_PRICE + "; скидка 0.." + (long) MAX_DISCOUNT_PERCENT
                + " % ; НДС после скидки, по умолчанию " + (long) DEFAULT_VAT_PERCENT + " %.");

        Scanner in = new Scanner(System.in);
        System.out.print("Количество: ");
        Integer quantity = parseInt(readLine(in));
        System.out.print("Цена за единицу: ");
        Double price = parseDouble(readLine(in));
        System.out.print("Скидка, %: ");
        Double discount = parseDouble(readLine(in));
        System.out.print("Ставка НДС, % [Enter = " + (long) DEFAULT_VAT_PERCENT + "]: ");
        String vatLine = readLine(in).trim();
        Double vat = vatLine.isEmpty() ? DEFAULT_VAT_PERCENT : parseDouble(vatLine);

        if (quantity == null || price == null || discount == null || vat == null) {
            System.out.println("Ошибка ввода: пустое значение или неверный формат. Расчёт не выполнен."); // раздел 9
            return;
        }
        String error = errorOf(quantity, price, discount);            // 2
        if (error != null) {
            System.out.println("Ошибка: " + error + ". Расчёт не выполнен.");  // раздел 3
            return;
        }
        if (vat != DEFAULT_VAT_PERCENT)                               // «актуальность НДС» 
            System.out.println("Внимание: ставка НДС отличается от актуальной "
                    + (long) DEFAULT_VAT_PERCENT + " %.");

        double base       = calculateBase(quantity, price);           // 3
        double discounted = applyDiscount(base, discount);
        double vatSum     = calculateVat(discounted, vat);
        double total      = calculateTotal(quantity, price, discount, vat);

        System.out.println("Базовая сумма:      " + rub(base));
        System.out.println("Скидка:             " + rub(base - discounted));
        System.out.println("Сумма после скидки: " + rub(discounted));
        System.out.println("НДС " + percent(vat) + ":           " + rub(vatSum));
        System.out.println("ИТОГО К ОПЛАТЕ:     " + rub(total));
    }

    /**  5 Разбор строк, вывод, округление */

    /** Читает строку; при обрыве ввода (Ctrl+D)  */
    private static String readLine(Scanner in) {
        return in.hasNextLine() ? in.nextLine() : "";
    }

    /** Раздел 9 (контролируемая ошибка, расчёт не выполняется). */

    public static Integer parseInt(String raw) {
        try { return Integer.valueOf(raw.trim()); }
        catch (Exception e) { return null; }
    }

    /** Как parseInt; понимает русскую запись  */

    public static Double parseDouble(String raw) {
        try { return Double.valueOf(raw.trim().replace(',', '.')); }
        catch (Exception e) { return null; }
    }

    /** Раздел 7 п.4................... */

    public static String rub(double v) {
        return String.format(new Locale("ru"), "%.2f руб.", v);
    }

    private static String percent(double v) {   // 20.0 → «20», 1.5 → «1,5»
        return (v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(v)).replace('.', ',');
    }

    /** Раздел 7 п.6......................... */
    
    private static double round2(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
