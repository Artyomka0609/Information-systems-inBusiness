package ru.edu.pr04;

import java.math.BigDecimal;

final class OrderRowParser {
    private OrderRowParser() {
    }

    static OrderItem parse(String row) {
        if (row == null || row.isBlank()) {
            throw new IllegalArgumentException("Строка товара пуста");
        }
        String[] fields = row.split(";", -1);
        if (fields.length != 4) {
            throw new IllegalArgumentException("Ожидаются id;название;цена;количество");
        }
        try {
            BigDecimal price = new BigDecimal(fields[2].strip());
            int quantity = Integer.parseInt(fields[3].strip());
            return new OrderItem(new Product(fields[0], fields[1], price), quantity);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Цена должна быть числом, количество — целым числом", exception);
        }
    }
}
