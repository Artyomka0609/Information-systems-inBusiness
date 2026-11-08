package ru.edu.pr04;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public final class OrderConsole {
    private OrderConsole() {
    }

    private static void printOrder(Order order) {
        System.out.println("Сотрудник: " + order.getEmployee().id() + "; " + order.getEmployee().fullName());
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            System.out.println(product.getId() + "; " + product.getName() + "; "
                    + product.getExactPrice() + " x " + item.getQuantity() + " = " + item.getExactLineTotal());
        }
        System.out.println("Итого: " + order.exactTotal() + "; статус: " + order.getStatus());
    }

    public static void main(String[] args) {
        try (Scanner input = new Scanner(System.in, StandardCharsets.UTF_8)) {
            System.out.println("Введите код сотрудника:");
            if (!input.hasNextLine()) return;
            String id = input.nextLine();
            System.out.println("Введите ФИО сотрудника:");
            if (!input.hasNextLine()) return;
            Order order = new OrderService().createOrder(new Employee(id, input.nextLine()));
            System.out.println("Команды: add;id;название;цена;количество | remove;id | show | confirm | exit");
            while (input.hasNextLine()) {
                String command = input.nextLine().strip();
                try {
                    if (command.equals("exit")) return;
                    if (command.startsWith("add;")) {
                        OrderService.addRow(order, command.substring(4));
                    } else if (command.startsWith("remove;")) {
                        order.removeProduct(command.substring(7));
                    } else if (command.equals("show")) {
                        printOrder(order);
                    } else if (command.equals("confirm")) {
                        order.confirm();
                        System.out.println("Заказ подтверждён");
                    } else {
                        throw new IllegalArgumentException("Неизвестная команда");
                    }
                } catch (IllegalArgumentException | IllegalStateException exception) {
                    System.out.println("Ошибка: " + exception.getMessage());
                }
            }
        } catch (IllegalArgumentException exception) {
            System.out.println("Ошибка: " + exception.getMessage());
        }
    }
}
