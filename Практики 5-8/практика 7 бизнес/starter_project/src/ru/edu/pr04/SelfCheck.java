package ru.edu.pr04;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SelfCheck {
    private static int passed;
    private static int failed;

    private static Order order() {
        return new OrderService().createOrder(new Employee("E1", "Иван Иванов"));
    }

    private static Product product(double price) {
        return new Product("P1", "Товар", price);
    }

    private static void require(boolean condition) {
        if (!condition) throw new AssertionError("Неожиданный результат");
    }

    private static void expect(Class<? extends RuntimeException> type, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException exception) {
            if (type.isInstance(exception)) return;
            throw new AssertionError("Неверный тип ошибки", exception);
        }
        throw new AssertionError("Ожидалась ошибка " + type.getSimpleName());
    }

    private static void check(String name, Runnable action) {
        try {
            action.run();
            passed++;
            System.out.println("PASS " + name);
        } catch (RuntimeException | AssertionError exception) {
            failed++;
            System.out.println("FAIL " + name + ": " + exception.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        check("Типовой товар", () -> require(new Product("P1", "Mouse", 1000).getPrice() == 1000));
        check("Пустые поля товара", () -> {
            expect(IllegalArgumentException.class, () -> new Product(null, "Товар", 1));
            expect(IllegalArgumentException.class, () -> new Product(" ", "Товар", 1));
            expect(IllegalArgumentException.class, () -> new Product("P1", null, 1));
            expect(IllegalArgumentException.class, () -> new Product("P1", " ", 1));
        });
        check("Недопустимые цены", () -> {
            for (double price : new double[]{0, -100, Double.NaN, Double.POSITIVE_INFINITY}) {
                expect(IllegalArgumentException.class, () -> product(price));
            }
            require(product(Double.MIN_VALUE).getPrice() > 0);
        });
        check("Некорректный сотрудник", () -> {
            expect(IllegalArgumentException.class, () -> new Employee(null, "Иван"));
            expect(IllegalArgumentException.class, () -> new Employee("E1", " "));
            expect(IllegalArgumentException.class, () -> new Order(null));
        });
        check("Количество 1 и сумма строки", () -> {
            require(new OrderItem(product(2), 1).getQuantity() == 1);
            require(new OrderItem(product(2.5), 2).getLineTotal() == 5);
        });
        check("Количество 100", () -> require(new OrderItem(product(0.1), 100).getQuantity() == 100));
        check("Количество вне границ и отсутствующий товар", () -> {
            for (int quantity : new int[]{-1, 0, 101, Integer.MAX_VALUE}) {
                expect(IllegalArgumentException.class, () -> order().addProduct(product(1), quantity));
            }
            expect(IllegalArgumentException.class, () -> order().addProduct(null, 1));
        });
        check("Некорректное увеличение количества", () -> {
            OrderItem item = new OrderItem(product(1), 1);
            for (int delta : new int[]{-1, 0, Integer.MAX_VALUE}) {
                expect(IllegalArgumentException.class, () -> item.increase(delta));
            }
            require(item.getQuantity() == 1);
        });
        check("Повтор товара объединяется", () -> {
            Order o = order();
            o.addProduct(product(2), 2);
            o.addProduct(product(2), 3);
            require(o.getItems().size() == 1 && o.getItems().get(0).getQuantity() == 5 && o.total() == 10);
        });
        check("Граница объединения 100", () -> {
            Order o = order();
            o.addProduct(product(0.1), 99);
            o.addProduct(product(0.1), 1);
            require(o.getItems().get(0).getQuantity() == 100);
            expect(IllegalArgumentException.class, () -> o.addProduct(product(0.1), 1));
            require(o.getItems().size() == 1 && o.getItems().get(0).getQuantity() == 100 && o.total() == 10);
        });
        check("Конфликт одинакового id", () -> {
            Order o = order();
            o.addProduct(product(2), 1);
            expect(IllegalArgumentException.class, () -> o.addProduct(new Product("P1", "Другой", 2), 1));
            expect(IllegalArgumentException.class, () -> o.addProduct(product(3), 1));
            require(o.total() == 2 && o.getItems().size() == 1);
        });
        check("Удаление пересчитывает сумму", () -> {
            Order o = order();
            o.addProduct(product(2), 2);
            o.addProduct(new Product("P2", "Кабель", 3), 1);
            require(o.total() == 7);
            o.removeProduct("P1");
            require(o.total() == 3 && o.getItems().size() == 1);
            o.removeProduct("P2");
            require(o.total() == 0 && o.getItems().isEmpty());
        });
        check("Некорректный id удаления", () -> {
            Order o = order();
            o.addProduct(product(1), 1);
            expect(IllegalArgumentException.class, () -> o.removeProduct(null));
            expect(IllegalArgumentException.class, () -> o.removeProduct(" "));
            expect(IllegalArgumentException.class, () -> o.removeProduct("UNKNOWN"));
            require(o.total() == 1);
        });
        check("Пустой заказ не подтверждается", () -> {
            Order o = order();
            expect(IllegalStateException.class, o::confirm);
            require(o.getStatus() == OrderStatus.DRAFT);
        });
        check("Сумма 44.99", () -> {
            Order o = order();
            o.addProduct(product(44.99), 1);
            o.confirm();
            require(o.getStatus() == OrderStatus.CONFIRMED);
        });
        check("Сумма ровно 45", () -> {
            Order o = order();
            o.addProduct(product(0.1), 3);
            o.addProduct(new Product("P2", "Кабель", 44.7), 1);
            require(o.total() == 45);
            o.confirm();
            require(o.getStatus() == OrderStatus.CONFIRMED);
        });
        check("Сумма 45.01 и исправление заказа", () -> {
            Order o = order();
            o.addProduct(product(45.01), 1);
            expect(IllegalStateException.class, o::confirm);
            require(o.getStatus() == OrderStatus.DRAFT);
            o.removeProduct("P1");
            o.addProduct(product(45), 1);
            o.confirm();
            require(o.getStatus() == OrderStatus.CONFIRMED);
        });
        check("Запрет изменений после confirm", () -> {
            Order o = order();
            o.addProduct(product(1), 1);
            o.confirm();
            expect(IllegalStateException.class, () -> o.addProduct(product(1), 1));
            expect(IllegalStateException.class, () -> o.addProduct(new Product("P2", "Кабель", 1), 1));
            expect(IllegalStateException.class, () -> o.removeProduct("P1"));
            expect(IllegalStateException.class, o::confirm);
            require(o.total() == 1 && o.getItems().size() == 1);
        });
        check("Внешний список нельзя изменять", () -> {
            Order o = order();
            o.addProduct(product(1), 1);
            expect(UnsupportedOperationException.class, () -> o.getItems().clear());
            expect(UnsupportedOperationException.class, () -> o.getItems().add(new OrderItem(product(2), 1)));
            require(o.total() == 1);
        });
        check("Копии позиций не меняют заказ", () -> {
            Order o = order();
            o.addProduct(product(1), 1);
            List<OrderItem> snapshot = o.getItems();
            snapshot.get(0).increase(1);
            require(o.total() == 1);
            o.confirm();
            snapshot.get(0).increase(1);
            o.getItems().get(0).increase(1);
            require(o.total() == 1 && o.getItems().get(0).getQuantity() == 1);
        });
        check("Формат входных данных", () -> {
            Order o = order();
            for (String row : new String[]{null, "", "P1;Товар;abc;1", "P1;Товар;1;1.5",
                    "P1;Товар;1;2147483648", "P1;Товар;1;", "P1;Товар;1", "P1;;1;1", "P1;Товар;1;1;лишнее"}) {
                expect(IllegalArgumentException.class, () -> OrderService.addRow(o, row));
            }
            require(o.getItems().isEmpty());
            OrderService.addRow(o, "P1;Товар;2.5;2");
            require(o.total() == 5);
        });
        List<String> sample = Files.readAllLines(Path.of("data", "sample.csv"), StandardCharsets.UTF_8);
        check("Открытый набор sample.csv", () -> {
            require(sample.size() == 5 && sample.get(0).equals("productId;name;price;quantity"));
            Order o = order();
            for (int i = 1; i <= 3; i++) OrderService.addRow(o, sample.get(i));
            expect(IllegalArgumentException.class, () -> OrderService.addRow(o, sample.get(4)));
            require(o.getItems().size() == 2 && o.getItems().get(0).getQuantity() == 3 && o.total() == 112440);
            expect(IllegalStateException.class, o::confirm);
        });
        check("Переполнение общей суммы", () -> {
            Order o = order();
            o.addProduct(product(Double.MAX_VALUE), 2);
            expect(IllegalStateException.class, o::confirm);
            require(o.getStatus() == OrderStatus.DRAFT);
        });
        check("Десятичная граница 10.8 × 3 + 12.6 = 45", () -> {
            Order o = order();
            OrderService.addRow(o, "A;Кабель;10.8;3");
            OrderService.addRow(o, "B;Мышь;12.6;1");
            require(o.exactTotal().compareTo(new BigDecimal("45")) == 0);
            o.confirm();
            require(o.getStatus() == OrderStatus.CONFIRMED);
        });
        check("Десятичная сумма чуть выше лимита", () -> {
            Order o = order();
            OrderService.addRow(o, "A;Мышь;45.000000000000000001;1");
            expect(IllegalStateException.class, o::confirm);
            require(o.getStatus() == OrderStatus.DRAFT);
        });
        check("Цены 0.10 и 0.1 совпадают, более точная цена отличается", () -> {
            Order o = order();
            OrderService.addRow(o, "A;Кабель;0.10;1");
            OrderService.addRow(o, "A;Кабель;0.1;1");
            expect(IllegalArgumentException.class, () -> OrderService.addRow(o, "A;Кабель;0.100000000000000001;1"));
            require(o.getItems().size() == 1 && o.getItems().get(0).getQuantity() == 2);
        });
        check("Единый контракт для двух правил суммы", () -> {
            OrderAmountRule minimum = total -> {
                if (total.compareTo(BigDecimal.TEN) < 0) {
                    throw new IllegalStateException("Сумма меньше минимума");
                }
            };
            for (OrderAmountRule rule : List.of(minimum, new MaximumOrderTotal(BigDecimal.TEN))) {
                Order o = new OrderService(List.of(rule)).createOrder(new Employee("E1", "Иван"));
                o.addProduct(product(10), 1);
                OrderService.validateForConfirm(o);
                require(o.getStatus() == OrderStatus.DRAFT);
                o.confirm();
                require(o.getStatus() == OrderStatus.CONFIRMED);
            }
        });
        check("Отказ дополнительного правила сохраняет черновик", () -> {
            OrderService service = new OrderService(List.of(new MaximumOrderTotal(BigDecimal.TEN)));
            Order o = service.createOrder(new Employee("E1", "Иван"));
            o.addProduct(product(11), 1);
            expect(IllegalStateException.class, () -> OrderService.validateForConfirm(o));
            expect(IllegalStateException.class, o::confirm);
            require(o.getStatus() == OrderStatus.DRAFT && o.total() == 11);
            o.removeProduct("P1");
            o.addProduct(product(10), 1);
            o.confirm();
            require(o.getStatus() == OrderStatus.CONFIRMED);
        });
        System.out.println("Результат: " + passed + " PASS, " + failed + " FAIL");
        if (failed != 0) throw new AssertionError("Проверки не пройдены");
    }
}
