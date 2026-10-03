package ru.edu.pr01;


public class SelfCheck {

    private static int failures = 0;
    private static int boundaryCount = 0;

    public static void main(String[] args) {
        /** ИСХОДНАЯ ПРОВЕРКА ИЗ СТАРТЕРА - НЕ УДАЛЯТЬ */
        boolean ok = false;
        try { ok = (OrderCalculator.calculateBase(2, 100.0) == 200.0 && Math.abs(OrderCalculator.calculateTotal(2,100.0,10.0,20.0)-216.0)<0.001); } catch (Exception e) { System.out.println("FAIL exception " + e.getMessage()); }
        System.out.println(ok ? "PASS" : "FAIL  complete TODO methods");
        /**  КОНЕЦ ИСХОДНОЙ ПРОВЕРКИ; НИЖЕ (раздел 9) */

        if (!ok) { System.out.println("Сначала завершите TODO в OrderCalculator."); System.exit(1); }

        // 1. Корректное типовое значение → успешный результат
        check("1. типовое: заказ (100 шт, 1000 руб., 10%) проходит проверку",
                OrderCalculator.isValid(100, 1000.0, 10));
        check("   типовое: итог со скидкой 10% и НДС 20% = 108000.00",
                OrderCalculator.calculateTotal(100, 1000.0, 10, 20) == 108000.00);

        // 2. Нижняя граница → точное включение/исключение
        boundary("2. нижняя: количество 1 — принято",        OrderCalculator.isValid(1, 100, 10));
        boundary("   нижняя: количество 0 — отклонено",      !OrderCalculator.isValid(0, 100, 10));
        boundary("   нижняя: цена 0.01 — принято",           OrderCalculator.isValid(1, 0.01, 10));
        boundary("   нижняя: цена 0 — отклонено (0 исключён)", !OrderCalculator.isValid(1, 0, 10));
        boundary("   нижняя: скидка 0 — принято",            OrderCalculator.isValid(1, 100, 0));
        boundary("   нижняя: скидка -0.01 — отклонено",      !OrderCalculator.isValid(1, 100, -0.01));

        // 3. Верхняя граница (пороги — из констант [A], а не литералов)
        int    maxQ = OrderCalculator.MAX_QUANTITY;
        double maxP = OrderCalculator.MAX_UNIT_PRICE;
        double maxD = OrderCalculator.MAX_DISCOUNT_PERCENT;
        boundary("3. верхняя: количество " + maxQ + " — принято",        OrderCalculator.isValid(maxQ, 100, 10));
        boundary("   верхняя: количество " + (maxQ + 1) + " — отклонено", !OrderCalculator.isValid(maxQ + 1, 100, 10));
        boundary("   верхняя: цена " + (long) maxP + " — принято",        OrderCalculator.isValid(1, maxP, 10));
        boundary("   верхняя: цена " + (long) maxP + "+0.01 — отклонено", !OrderCalculator.isValid(1, maxP + 0.01, 10));
        boundary("   верхняя: скидка " + (long) maxD + " (вариант 8) — принято",   OrderCalculator.isValid(1, 100, maxD));
        boundary("   верхняя: скидка " + (long) maxD + "+0.01 — отклонено",         !OrderCalculator.isValid(1, 100, maxD + 0.01));

        // 4. Пустое/отсутствующее значение - контролируемая ошибка
        check("4. пустое: количество \"\" — контролируемая ошибка",
                OrderCalculator.parseInt("") == null);
        check("   пустое: цена \"   \" (пробелы) — контролируемая ошибка",
                OrderCalculator.parseDouble("   ") == null);

        // 5. Значение неверного формата - контролируемая ошибка
        check("5. формат: количество «3.5» (не целое) — контролируемая ошибка",
                OrderCalculator.parseInt("3.5") == null);
        check("   формат: цена «abc» — контролируемая ошибка",
                OrderCalculator.parseDouble("abc") == null);
        check("   формат: цена «12,34» (с запятой) — принята как 12.34",
                OrderCalculator.parseDouble("12,34") == 12.34);

        // 6. Повтор/конфликт данных - поведение согласно правилам работы:
        //    ставка НДС, конфликтующая с актуальной 20%, - не отказ,
        //    а предупреждение в main (раздел 9: «поведение согласно правилам»);
        //    сама ставка при этом валидна.
        check("6. конфликт: НДС 25% != актуальной 20% - предупреждение в main, ставка принята",
                OrderCalculator.calculateVat(100.0, 25) == 25.0);

        // Дополнительно: обязательные правила из разделов 3, 8 и 7 п.6
        check("7. порядок: НДС от суммы ПОСЛЕ скидки (900 -> 180, а не 200 от базы)",
                OrderCalculator.calculateVat(OrderCalculator.applyDiscount(1000.0, 10), 20) == 180.0);
        check("8. раздел 8: скидка 10% от 2000 = 1800.00, а не 1990 (процент != рубли)",
                OrderCalculator.applyDiscount(2000.0, 10) == 1800.00);
        check("9. раздел 7 п.6: double неточен (0.1 + 0.2 != 0.3)",
                0.1 + 0.2 != 0.3);
        check("   но калькулятор даёт ровно 0.36 через округление к копейкам",
                OrderCalculator.calculateTotal(3, 0.1, 0, 20) == 0.36);

        System.out.println();
        System.out.println("Граничных проверок: " + boundaryCount + " (требуется минимум 5)");
        if (boundaryCount < 5) failures++;
        System.out.println(failures == 0 ? "ВСЕ ПРОВЕРКИ ПРОЙДЕНЫ" : "ПРОВАЛОВ: " + failures);
        if (failures > 0) System.exit(1);
    }

    private static void check(String name, boolean condition) {
        System.out.println("  " + (condition ? "PASS " : "FAIL ") + name);
        if (!condition) failures++;
    }

    private static void boundary(String name, boolean condition) {
        boundaryCount++;
        check(name, condition);
    }
}
