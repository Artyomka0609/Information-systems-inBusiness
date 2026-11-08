package ru.edu.pr05;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

final class CsvTripReader {
    private final BusinessTripService service;
    private final TripCommands commands;
    private final PrintStream output;

    CsvTripReader(BusinessTripService service, TripCommands commands, PrintStream output) {
        this.service = service;
        this.commands = commands;
        this.output = output;
    }

    void read(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            if (!"id;start;end;action".equals(reader.readLine())) {
                throw new IllegalArgumentException("Ожидается заголовок id;start;end;action");
            }
            String line;
            int number = 1;
            while ((line = reader.readLine()) != null) {
                number++;
                try {
                    BusinessTrip trip = importRow(line);
                    output.println("Строка " + number + ": " + trip.getStatus());
                } catch (IllegalArgumentException | IllegalStateException ex) {
                    output.println("Строка " + number + ": " + ex.getClass().getSimpleName()
                            + ": " + ex.getMessage());
                }
            }
        }
    }

    BusinessTrip importRow(String line) {
        String[] fields = line.split(";", -1);
        if (fields.length != 4) {
            throw new IllegalArgumentException("Ожидаются четыре поля");
        }
        LocalDate start = TripDates.parse(fields[1]);
        LocalDate end = TripDates.parse(fields[2]);
        return service.create(fields[0], start, end, trip -> commands.execute(fields[3], trip));
    }
}
