package ru.edu.pr05;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class TripCommands {
    private final Map<String, Consumer<BusinessTrip>> commands = new LinkedHashMap<>();

    TripCommands(Supplier<String> reason, Supplier<LocalDate> today) {
        register("SUBMIT", BusinessTrip::submit);
        register("APPROVE", BusinessTrip::approve);
        register("REJECT", trip -> trip.reject(reason.get()));
        register("CANCEL", trip -> trip.cancel(today.get()));
        register("COMPLETE", trip -> trip.complete(today.get()));
    }

    void register(String name, Consumer<BusinessTrip> action) {
        if (name == null || name.trim().isEmpty() || action == null) {
            throw new IllegalArgumentException("Укажите имя и действие команды");
        }
        if (commands.putIfAbsent(name, action) != null) {
            throw new IllegalArgumentException("Команда уже зарегистрирована: " + name);
        }
    }

    void execute(String name, BusinessTrip trip) {
        Consumer<BusinessTrip> action = commands.get(name);
        if (action == null) {
            throw new IllegalArgumentException("Неизвестная команда: " + name);
        }
        action.accept(trip);
    }

    String names() {
        return String.join(", ", commands.keySet());
    }
}
