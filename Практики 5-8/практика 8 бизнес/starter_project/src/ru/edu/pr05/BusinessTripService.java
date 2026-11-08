package ru.edu.pr05;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class BusinessTripService {
    private final List<BusinessTrip> trips = new ArrayList<>();
    private final CancellationPolicy cancellationPolicy;

    public BusinessTripService() {
        this(BusinessTrip.VARIANT_8);
    }

    public BusinessTripService(CancellationPolicy cancellationPolicy) {
        if (cancellationPolicy == null) {
            throw new IllegalArgumentException("Правило отмены обязательно");
        }
        this.cancellationPolicy = cancellationPolicy;
    }

    public BusinessTrip create(String id, LocalDate start, LocalDate end) {
        BusinessTrip trip = new BusinessTrip(id, start, end, cancellationPolicy);
        trips.add(trip);
        return trip;
    }

    BusinessTrip create(String id, LocalDate start, LocalDate end, Consumer<BusinessTrip> firstAction) {
        BusinessTrip trip = new BusinessTrip(id, start, end, cancellationPolicy);
        firstAction.accept(trip);
        trips.add(trip);
        return trip;
    }

    public List<BusinessTrip> findByStatus(TripStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Статус обязателен");
        }
        List<BusinessTrip> matches = new ArrayList<>();
        for (BusinessTrip trip : trips) {
            if (trip.getStatus() == status) {
                matches.add(trip);
            }
        }
        return matches;
    }
}
