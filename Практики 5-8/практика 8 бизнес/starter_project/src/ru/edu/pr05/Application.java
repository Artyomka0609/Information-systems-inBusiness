package ru.edu.pr05;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Scanner;

public final class Application {
    private final BusinessTripService service = new BusinessTripService();

    public static LocalDate parseDate(String value) {
        return TripDates.parse(value);
    }

    public void readSample(Path path) throws IOException {
        TripCommands commands = new TripCommands(() -> null, LocalDate::now);
        new CsvTripReader(service, commands, System.out).read(path);
    }

    private static String prompt(Scanner input, String prompt) {
        System.out.println(prompt);
        if (!input.hasNextLine()) {
            throw new NoSuchElementException("Ввод прерван");
        }
        return input.nextLine();
    }

    public void run(Scanner input) {
        String id = prompt(input, "Идентификатор:");
        LocalDate start = parseDate(prompt(input, "Дата начала (ГГГГ-ММ-ДД):"));
        LocalDate end = parseDate(prompt(input, "Дата окончания (ГГГГ-ММ-ДД):"));
        BusinessTrip trip = service.create(id, start, end);
        TripCommands commands = new TripCommands(
                () -> prompt(input, "Причина отклонения:"),
                () -> parseDate(prompt(input, "Текущая дата (ГГГГ-ММ-ДД):")));
        System.out.println(trip.getStatus());
        while (true) {
            System.out.println("Команда " + commands.names() + "; пустая строка — выход:");
            if (!input.hasNextLine()) {
                return;
            }
            String name = input.nextLine();
            if (name.isEmpty()) {
                return;
            }
            try {
                commands.execute(name, trip);
                System.out.println(trip.getStatus());
            } catch (IllegalArgumentException | IllegalStateException ex) {
                System.out.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        Application app = new Application();
        try {
            if (args.length == 0) {
                try (Scanner input = new Scanner(System.in, "UTF-8")) {
                    app.run(input);
                }
            } else if (args.length == 1) {
                app.readSample(Paths.get(args[0]));
            } else {
                throw new IllegalArgumentException("Передайте один путь к CSV или запускайте без аргументов");
            }
        } catch (IOException | IllegalArgumentException | NoSuchElementException ex) {
            System.err.println("Ошибка: " + ex.getMessage());
            System.exit(1);
        }
    }
}
